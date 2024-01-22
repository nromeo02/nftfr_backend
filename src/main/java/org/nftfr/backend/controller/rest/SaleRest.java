package org.nftfr.backend.controller.rest;

import jakarta.servlet.http.HttpServletRequest;
import org.nftfr.backend.persistence.DBManager;
import org.nftfr.backend.persistence.dao.NftDao;
import org.nftfr.backend.persistence.dao.PaymentMethodDao;
import org.nftfr.backend.persistence.dao.SaleDao;
import org.nftfr.backend.persistence.dao.UserDao;
import org.nftfr.backend.persistence.model.PaymentMethod;
import org.nftfr.backend.persistence.model.Sale;
import org.nftfr.backend.persistence.model.Nft;
import org.nftfr.backend.persistence.model.User;
import org.nftfr.backend.utility.AuthToken;
import org.nftfr.backend.utility.ClientErrorException;
import org.nftfr.backend.utility.MoneyConverter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@CrossOrigin(value = "http://localhost:4200", allowCredentials = "true")
@RequestMapping("/sale")
public class SaleRest {
    private final SaleDao saleDao = DBManager.getInstance().getSaleDao();
    private final UserDao userDao = DBManager.getInstance().getUserDao();
    private final NftDao nftDao = DBManager.getInstance().getNftDao();
    private final PaymentMethodDao paymentMethodDao = DBManager.getInstance().getPaymentMethodDao();

    public record CreateBody(String idNft, String destinationAddress, double price, Duration duration) {
        public Sale asSale(Nft nft, PaymentMethod paymentMethod) {
            Sale sale = new Sale();
            LocalDateTime now = LocalDateTime.now();
            sale.setNft(nft);
            sale.setPaymentMethod(paymentMethod);
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

        PaymentMethod paymentMethod = paymentMethodDao.findByAddress(bodyParams.destinationAddress());
        if (paymentMethod == null)
            throw new ClientErrorException(HttpStatus.NOT_FOUND, "Payment method not found");

        final boolean checkNftOwnership = nft.getOwner().getUsername().equals(authToken.username());
        final boolean checkPMOwnership = paymentMethod.getUser().getUsername().equals(authToken.username());
        if (!checkNftOwnership || !checkPMOwnership)
            throw new ClientErrorException(HttpStatus.FORBIDDEN, "You don't have the permissions for this action");

        saleDao.add(bodyParams.asSale(nft, paymentMethod));
    }

    @DeleteMapping("/delete/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, HttpServletRequest req) {
        AuthToken authToken = AuthToken.fromRequest(req);
        Sale sale = saleDao.findById(id);
        if (sale == null)
            throw new ClientErrorException(HttpStatus.NOT_FOUND, "Sale not found");

        // Block the user from deleting other users NFTs.
        if (!sale.getNft().getOwner().getUsername().equals(authToken.username()))
            throw new ClientErrorException(HttpStatus.FORBIDDEN, "You don't have the permissions for this action");

        saleDao.remove(id);
    }

    @GetMapping("/get/{nftId}")
    @ResponseStatus(HttpStatus.OK)
    public Sale get(@PathVariable String nftId) {
        Sale sale = saleDao.findByNftId(nftId);
        if (sale == null)
            throw new ClientErrorException(HttpStatus.NOT_FOUND, "Sale not found");

        return sale;
    }

    @PutMapping("/buy/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void buy(@PathVariable Long id, @RequestBody Map<String, String> bodyParams, HttpServletRequest req) {
        MoneyConverter moneyConverter = MoneyConverter.getInstance();
        AuthToken authToken = AuthToken.fromRequest(req);

        // Find sale.
        Sale sale = saleDao.findById(id);
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
        PaymentMethod sellerPM = sale.getPaymentMethod();
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

        // Update price and transfer ownership.
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
        saleDao.remove(id);
        DBManager.getInstance().endTransaction();
    }
}


