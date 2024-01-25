package org.nftfr.backend.controller.rest;

import jakarta.servlet.http.HttpServletRequest;
import org.nftfr.backend.application.RealTimeService;
import org.nftfr.backend.persistence.DBManager;
import org.nftfr.backend.persistence.dao.NftDao;
import org.nftfr.backend.persistence.dao.PaymentMethodDao;
import org.nftfr.backend.persistence.dao.SaleDao;
import org.nftfr.backend.persistence.dao.UserDao;
import org.nftfr.backend.persistence.model.PaymentMethod;
import org.nftfr.backend.persistence.model.Sale;
import org.nftfr.backend.persistence.model.Nft;
import org.nftfr.backend.persistence.model.User;
import org.nftfr.backend.application.auth.AuthToken;
import org.nftfr.backend.application.http.ClientErrorException;
import org.nftfr.backend.application.MoneyConverter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(value = "http://localhost:4200", allowCredentials = "true")
@RequestMapping("/sale")
public class SaleRest {
    private final SaleDao saleDao = DBManager.getInstance().getSaleDao();
    private final UserDao userDao = DBManager.getInstance().getUserDao();
    private final NftDao nftDao = DBManager.getInstance().getNftDao();
    private final PaymentMethodDao paymentMethodDao = DBManager.getInstance().getPaymentMethodDao();

    public record CreateBody(String idNft, String sellerAddress, double price, Duration duration) {
        public Sale asSale(Nft nft, PaymentMethod paymentMethod) {
            Sale sale = new Sale();
            LocalDateTime now = LocalDateTime.now();
            sale.setNft(nft);
            sale.setSellerPaymentMethod(paymentMethod);
            sale.setPrice(price);
            sale.setCreationDate(now);

            if (duration != null && !duration.isZero())
                sale.setEndTime(now.plus(duration));

            return sale;
        }
    }

    @PutMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public void create(@RequestBody CreateBody bodyParams, HttpServletRequest req) {
        AuthToken authToken = AuthToken.fromRequest(req);
        Nft nft = nftDao.findById(bodyParams.idNft());

        if (nft == null)
            throw new ClientErrorException(HttpStatus.NOT_FOUND, "NFT not found");

        PaymentMethod paymentMethod = paymentMethodDao.findByAddress(bodyParams.sellerAddress());
        if (paymentMethod == null)
            throw new ClientErrorException(HttpStatus.NOT_FOUND, "Payment method not found");

        final boolean checkNftOwnership = nft.getOwner().getUsername().equals(authToken.username());
        final boolean checkPMOwnership = paymentMethod.getUser().getUsername().equals(authToken.username());
        if (!checkNftOwnership || !checkPMOwnership)
            throw new ClientErrorException(HttpStatus.FORBIDDEN, "You don't have the permissions for this action");

        saleDao.add(bodyParams.asSale(nft, paymentMethod));
    }

    @DeleteMapping("/delete/{nftId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String nftId, HttpServletRequest req) {
        AuthToken authToken = AuthToken.fromRequest(req);
        Sale sale = saleDao.findByNftId(nftId);
        if (sale == null)
            throw new ClientErrorException(HttpStatus.NOT_FOUND, "Sale not found");

        // Block the user from deleting other users NFTs.
        if (!sale.getNft().getOwner().getUsername().equals(authToken.username()))
            throw new ClientErrorException(HttpStatus.FORBIDDEN, "You don't have the permissions for this action");

        saleDao.remove(nftId);
    }

    @GetMapping("/get/sales")
    @ResponseStatus(HttpStatus.OK)
    public List<Sale> getSales() {
        return saleDao.getAllSales();
    }

    @GetMapping("/get/auctions")
    @ResponseStatus(HttpStatus.OK)
    public List<Sale> getAuctions() {
        return saleDao.getAllAuctions();
    }

    @GetMapping("/get/{nftId}")
    @ResponseStatus(HttpStatus.OK)
    public Sale get(@PathVariable String nftId) {
        Sale sale = saleDao.findByNftId(nftId);
        if (sale == null)
            throw new ClientErrorException(HttpStatus.NOT_FOUND, "Sale not found");

        return sale;
    }

    @GetMapping("/offer/{nftId}")
    @ResponseStatus(HttpStatus.OK)
    public void makeAnOffer(@PathVariable String nftId, HttpServletRequest req, @RequestBody Map<String, String> bodyParams) {
        //verifica che la sale esiste
        Sale sale = saleDao.findByNftId(nftId);
        if (sale == null) {
            throw new ClientErrorException(HttpStatus.NOT_FOUND, "Sale not found");
        }
        //verifica che l'asta non sia finita
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(sale.getEndTime())) {
            throw new ClientErrorException(HttpStatus.NOT_FOUND, "Auction has ended");
        } else {
            MoneyConverter moneyConverter = MoneyConverter.getInstance();
            AuthToken authToken = AuthToken.fromRequest(req);
            //verifica che l'acquirente essista
            User buyer = userDao.findByUsername(authToken.username());
            if (buyer == null) {
                throw new ClientErrorException(HttpStatus.NOT_FOUND, "User not found");
            }
            //verifica che l'acquirente non possegga già nft
            Nft nft = sale.getNft();
            if (nft.getOwner().getUsername().equals(buyer.getUsername()))
                throw new ClientErrorException(HttpStatus.FORBIDDEN, "You already own this nft");

            //verifica che l'acquirente abbia un metodo di pagamento e che sia dello stesso tipo dell'asta
            PaymentMethod buyerPM = paymentMethodDao.findByAddress(bodyParams.get("address"));

            //verifica che il paymentMethodSiaValida
            if (buyerPM == null)
                throw new ClientErrorException(HttpStatus.NOT_FOUND, "Payment method not found");

            if (!buyerPM.getUser().getUsername().equals(buyer.getUsername()))
                throw new ClientErrorException(HttpStatus.FORBIDDEN, "Invalid payment method");

            //convertire il denaro se serve
            double offer = Double.parseDouble(bodyParams.get("offer"));
            PaymentMethod sellerPM = sale.getSellerPaymentMethod();       /* questo non deve cambiare mai, è il payment method di chi ha creato l'asta */
            if (buyerPM.getType() != sellerPM.getType()) {
                if (sellerPM.getType() == PaymentMethod.TYPE_ETH) {
                    offer = MoneyConverter.getInstance().convertUsdToEth(offer);
                } else {
                    offer = MoneyConverter.getInstance().convertEthToUsd(offer);
                }
            }

            if (offer < sale.getPrice())
                throw new ClientErrorException(HttpStatus.FORBIDDEN, "Insufficient balance");

            //fai ritornaare i soldi all'ultima offerta
            //bisogna usare il buyer PM non il seller
            sellerPM.setBalance(sellerPM.getBalance() + sale.getPrice());

            //trasferisci i soldi
            double buyerBalance = buyerPM.getBalance();
            if (buyerPM.getType() != sellerPM.getType()) {
                if (sellerPM.getType() == PaymentMethod.TYPE_ETH) {
                    buyerPM.setBalance(moneyConverter.convertEthToUsd(buyerBalance - offer));
                } else {
                    buyerPM.setBalance(moneyConverter.convertUsdToEth(buyerBalance - offer));
                }
            } else {
                buyerPM.setBalance(buyerBalance - sale.getPrice());
            }

            //update del prezzo e persona corrente
            sale.setBuyerPaymentMethod(buyerPM);
            sale.setPrice(offer);
            sale.setOfferMaker(authToken.username());

            //fai l'update di tutti i dao
            DBManager.getInstance().beginTransaction();
            paymentMethodDao.update(buyerPM);
            paymentMethodDao.update(sellerPM);
            saleDao.update(sale);
            DBManager.getInstance().endTransaction();

            //chiamare pushUpdate in real time manager
            RealTimeService.pushNewOffer(nftId, offer);
        }
    }
    @PutMapping("/buy/{nftId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void buy(@PathVariable String nftId, @RequestBody Map<String, String> bodyParams, HttpServletRequest req) {
        MoneyConverter moneyConverter = MoneyConverter.getInstance();
        AuthToken authToken = AuthToken.fromRequest(req);

        // Find sale.
        Sale sale = saleDao.findByNftId(nftId);
        if (sale == null)
            throw new ClientErrorException(HttpStatus.NOT_FOUND, "Sale not found");

        Nft nft = sale.getNft();

        // Find user.
        User buyer = userDao.findByUsername(authToken.username());
        if (buyer == null)
            throw new ClientErrorException(HttpStatus.NOT_FOUND, "User not found");

        // Check if a user is trying to buy their own nft.
        if (nft.getOwner().getUsername().equals(buyer.getUsername()))
            throw new ClientErrorException(HttpStatus.FORBIDDEN, "You already own this nft");

        // Find and verify the payment method.
        PaymentMethod buyerPM = paymentMethodDao.findByAddress(bodyParams.get("address"));
        if (buyerPM == null)
            throw new ClientErrorException(HttpStatus.NOT_FOUND, "Payment method not found");

        if (!buyerPM.getUser().getUsername().equals(buyer.getUsername()))
            throw new ClientErrorException(HttpStatus.FORBIDDEN, "Invalid payment method");

        // Convert money if required.
        double buyerBalance = buyerPM.getBalance();
        PaymentMethod sellerPM = sale.getSellerPaymentMethod();
        if (buyerPM.getType() != sellerPM.getType()) {
            if (sellerPM.getType() == PaymentMethod.TYPE_ETH) {
                buyerBalance = MoneyConverter.getInstance().convertUsdToEth(buyerBalance);
            } else {
                buyerBalance = MoneyConverter.getInstance().convertEthToUsd(buyerBalance);
            }
        }

        if (buyerBalance < sale.getPrice())
            throw new ClientErrorException(HttpStatus.FORBIDDEN, "Insufficient balance");

        // Transfer money.
        if (buyerPM.getType() != sellerPM.getType()) {
            if (sellerPM.getType() == PaymentMethod.TYPE_ETH) {
                buyerPM.setBalance(moneyConverter.convertEthToUsd(buyerBalance - sale.getPrice()));
            } else {
                buyerPM.setBalance(moneyConverter.convertUsdToEth(buyerBalance - sale.getPrice()));
            }
        } else {
            buyerPM.setBalance(buyerBalance - sale.getPrice());
        }

        sellerPM.setBalance(sellerPM.getBalance() + sale.getPrice());

        // Update value and transfer ownership.
        double nftValue = sale.getPrice();
        if (sellerPM.getType() == PaymentMethod.TYPE_USD)
            nftValue = moneyConverter.convertUsdToEth(nftValue);

        nft.setValue(nftValue);
        nft.setOwner(buyer);

        // Increase buyer rank.
        buyer.setRank(buyer.getRank() + 1);

        // Apply database changes.
        DBManager.getInstance().beginTransaction();
        paymentMethodDao.update(buyerPM);
        paymentMethodDao.update(sellerPM);
        nftDao.update(nft);
        userDao.update(buyer);
        saleDao.remove(nftId);
        DBManager.getInstance().endTransaction();
    }
}


