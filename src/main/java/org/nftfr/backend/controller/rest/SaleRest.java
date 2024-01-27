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
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

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

    public record OfferBody(String address, Double offer) {}

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

    @GetMapping("/get/updates/{nftId}")
    public SseEmitter getUpdates(@PathVariable String nftId) {
        Sale sale = saleDao.findByNftId(nftId);
        if (sale == null)
            throw new ClientErrorException(HttpStatus.NOT_FOUND, "Sale not found");

        // Set timeout to 1 minute.
        SseEmitter emitter = new SseEmitter(60 * 1000L);
        RealTimeService.registerEmitter(nftId, emitter);
        return emitter;
    }

    @GetMapping("/offer/{nftId}")
    @ResponseStatus(HttpStatus.OK)
    public void makeAnOffer(@PathVariable String nftId, @RequestBody OfferBody bodyParams, HttpServletRequest req) {
        Sale auction = saleDao.findByNftId(nftId);
        if (auction == null)
            throw new ClientErrorException(HttpStatus.NOT_FOUND, "Sale not found");

        if (auction.getEndTime() == null)
            throw new ClientErrorException(HttpStatus.FORBIDDEN, "Attempted to make an offer to a sale");

        // Make sure the auction is not finished.
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(auction.getEndTime()))
            throw new ClientErrorException(HttpStatus.NOT_FOUND, "Auction has ended");

        // Get offer maker.
        AuthToken authToken = AuthToken.fromRequest(req);
        User offerMaker = userDao.findByUsername(authToken.username());
        if (offerMaker == null)
            throw new ClientErrorException(HttpStatus.NOT_FOUND, "User not found");

        // Check if the buyer already owns the NFT.
        Nft nft = auction.getNft();
        if (nft.getOwner().getUsername().equals(offerMaker.getUsername()))
            throw new ClientErrorException(HttpStatus.FORBIDDEN, "You already own this NFT");

        // Get payment method.
        PaymentMethod offerMakerPM = paymentMethodDao.findByAddress(bodyParams.address());
        if (offerMakerPM == null)
            throw new ClientErrorException(HttpStatus.NOT_FOUND, "Payment method not found");

        if (!offerMakerPM.getUser().getUsername().equals(offerMaker.getUsername()))
            throw new ClientErrorException(HttpStatus.FORBIDDEN, "Invalid payment method");

        // Check if the user can send the offer.
        double offerValue = bodyParams.offer();
        if (offerValue < offerMakerPM.getBalance())
            throw new ClientErrorException(HttpStatus.FORBIDDEN, "Insufficient balance");

        // Convert balance and offer value if needed.
        MoneyConverter moneyConverter = MoneyConverter.getInstance();
        double offerMakerBalance = offerMakerPM.getBalance();
        final PaymentMethod sellerPM = auction.getSellerPaymentMethod();
        if (offerMakerPM.getType() != sellerPM.getType()) {
            if (sellerPM.getType() == PaymentMethod.TYPE_ETH) {
                offerMakerBalance = moneyConverter.convertUsdToEth(offerMakerBalance);
                offerValue = moneyConverter.convertUsdToEth(offerValue);
            } else {
                offerMakerBalance = moneyConverter.convertEthToUsd(offerMakerBalance);
                offerValue = moneyConverter.convertEthToUsd(offerValue);
            }
        }

        // Check if the offer is valid.
        if (offerValue < auction.getPrice())
            throw new ClientErrorException(HttpStatus.FORBIDDEN, "Insufficient balance");

        // Return the money to the previous offer maker.
        PaymentMethod prevOfferMakerPM = auction.getBuyerPaymentMethod();
        if (prevOfferMakerPM != null)
            prevOfferMakerPM.setBalance(prevOfferMakerPM.getBalance() + auction.getPrice());

        // Decrease offer maker balance.
        if (offerMakerPM.getType() != sellerPM.getType()) {
            if (sellerPM.getType() == PaymentMethod.TYPE_ETH) {
                offerMakerPM.setBalance(moneyConverter.convertEthToUsd(offerMakerBalance - offerValue));
            } else {
                offerMakerPM.setBalance(moneyConverter.convertUsdToEth(offerMakerBalance - offerValue));
            }
        } else {
            offerMakerPM.setBalance(offerMakerBalance - offerValue);
        }

        // If the auction end is less than 5 minutes then reset it.
        final LocalDateTime in5Minutes = now.plusMinutes(5);
        if (in5Minutes.isAfter(auction.getEndTime()))
            auction.setEndTime(in5Minutes);

        // Update auction.
        auction.setBuyerPaymentMethod(offerMakerPM);
        auction.setPrice(offerValue);
        auction.setOfferMaker(offerMaker.getUsername());

        // Make changes persistent.
        DBManager.getInstance().beginTransaction();
        paymentMethodDao.update(prevOfferMakerPM);
        paymentMethodDao.update(offerMakerPM);
        saleDao.update(auction);
        DBManager.getInstance().endTransaction();


        // Push update to all connected clients.
        RealTimeService.pushNewOffer(nftId, offerValue);
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

        if (sale.getEndTime() != null)
            throw new ClientErrorException(HttpStatus.FORBIDDEN, "Attempted to buy a NFT in auction");

        // Find user.
        User buyer = userDao.findByUsername(authToken.username());
        if (buyer == null)
            throw new ClientErrorException(HttpStatus.NOT_FOUND, "User not found");

        // Check if a user is trying to buy their own nft.
        Nft nft = sale.getNft();
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

        // Check if the buyer has enough money.
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


