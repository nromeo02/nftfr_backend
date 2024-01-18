package org.nftfr.backend.rest.controller;

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
import org.nftfr.backend.rest.model.AuthToken;
import org.nftfr.backend.rest.model.ClientErrorException;
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

    public record CreateBody(String idNft, String destinationAddress, double price, LocalDateTime creationDate, Duration duration) {
        public Sale asSale(Nft nft, PaymentMethod paymentMethod) {
            Sale sale = new Sale();
            sale.setNft(nft);
            sale.setPaymentMethod(paymentMethod);
            sale.setPrice(price);
            sale.setCreationDate(creationDate);

            if (!duration.isZero())
                sale.setEndTime(creationDate.plus(duration));

            return sale;
        }
    }

    @PutMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public void createSale(@RequestBody CreateBody bodyParams, HttpServletRequest req) {
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
    public void deleteSale(@PathVariable Long id, HttpServletRequest req) {
        AuthToken authToken = AuthToken.fromRequest(req);
        Sale sale = saleDao.findById(id);
        if (sale == null)
            throw new ClientErrorException(HttpStatus.NOT_FOUND, "Sale not found");

        // Block the user from deleting other users NFTs.
        if (!sale.getNft().getOwner().getUsername().equals(authToken.username()))
            throw new ClientErrorException(HttpStatus.FORBIDDEN, "You don't have the permissions for this action");

        saleDao.remove(id);
    }

    @PutMapping("/buy/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void buy(@PathVariable Long id, @RequestBody Map<String, String> bodyParams, HttpServletRequest req) {
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
        PaymentMethod paymentMethod = paymentMethodDao.findByAddress(bodyParams.get("address"));
        if (paymentMethod == null)
            throw new ClientErrorException(HttpStatus.NOT_FOUND, "Payment method not found");

        if (!paymentMethod.getUser().getUsername().equals(buyer.getUsername()))
            throw new ClientErrorException(HttpStatus.FORBIDDEN, "Invalid payment method");

        if (paymentMethod.getBalance() < sale.getPrice())
            throw new ClientErrorException(HttpStatus.FORBIDDEN, "Insufficient balance");

        // Transfer money.
        paymentMethod.setBalance(paymentMethod.getBalance() - sale.getPrice());
        sale.getPaymentMethod().setBalance(sale.getPaymentMethod().getBalance() + sale.getPrice());

        // Transfer ownership.
        nft.setOwner(buyer);

        // Increase buyer rank.
        buyer.setRank(buyer.getRank() + 1);

        // Apply database changes.
        DBManager.getInstance().beginTransaction();
        paymentMethodDao.update(paymentMethod);
        paymentMethodDao.update(sale.getPaymentMethod());
        nftDao.update(nft);
        saleDao.remove(id);
        DBManager.getInstance().endTransaction();
    }
}


