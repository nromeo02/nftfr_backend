package org.nftfr.backend.rest.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.nftfr.backend.persistence.DBManager;
import org.nftfr.backend.persistence.dao.NftDao;
import org.nftfr.backend.persistence.dao.PaymentMethodDao;
import org.nftfr.backend.persistence.dao.SaleDao;
import org.nftfr.backend.persistence.model.PaymentMethod;
import org.nftfr.backend.persistence.model.Sale;
import org.nftfr.backend.persistence.model.Nft;
import org.nftfr.backend.persistence.model.User;
import org.nftfr.backend.rest.model.AuthToken;
import org.nftfr.backend.rest.model.ClientErrorException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
@RestController
@CrossOrigin(value = "http://localhost:4200", allowCredentials = "true")
@RequestMapping("/sale")
public class SaleRest {
    private final SaleDao saleDao = DBManager.getInstance().getSaleDao();
    private final NftDao nftDao = DBManager.getInstance().getNftDao();
    private final PaymentMethodDao paymentMethodDao = DBManager.getInstance().getPaymentMethodDao();

    private record CreateParams(String idNft, double price, LocalDateTime creationDate, Duration duration) {
        public Sale asSale() {
            Sale sale = new Sale();
            sale.setId(1);
            sale.setIdNft(idNft);
            sale.setPrice(price);
            sale.setCreationDate(creationDate);
            if (duration.isZero()) {
                return sale;
            }
            sale.setEndTime(creationDate.plus(duration));
            return sale;
        }
    }

    //funziona ma bisogna settare l'id con id broker e bisogna passare la giusta string idnft
    @PutMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public void createSale(@RequestBody CreateParams createParams, HttpServletRequest request) {
        saleDao.add(createParams.asSale());
    }

    //funziona
    @DeleteMapping("/delete/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSale(@PathVariable int id, HttpServletRequest request) {
        AuthToken authToken = AuthToken.fromRequest(request);
        if (!authToken.admin()) {
            throw new RuntimeException("Non sei amministratore");
        } else {
            Sale sale = saleDao.findById(id);
            if (sale == null) {
                throw new ClientErrorException(HttpStatus.NOT_FOUND, "Nft not found");
            } else {
                saleDao.remove(id);
            }
        }
    }

    @PutMapping("/buy/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void buy(@PathVariable int id, HttpServletRequest request, @RequestBody Map<String, String> requestBody) {
        AuthToken authToken = AuthToken.fromRequest(request);
            Sale sale = saleDao.findById(id);
            if (sale == null) {
                throw new ClientErrorException(HttpStatus.NOT_FOUND, "The sale doesn't exist");
            }
            Nft nft = nftDao.findByPrimaryKey(sale.getIdNft());
            if (nft == null) {
                throw new ClientErrorException(HttpStatus.NOT_FOUND, "Nft not found");
            }
            User buyer = DBManager.getInstance().getUserDao().findByUsername(authToken.username());
            if (buyer == null) {
                throw new ClientErrorException(HttpStatus.NOT_FOUND, "Buyer not found");
            }
            String address = requestBody.get("address");
            if (address == null || address.isEmpty()) {
                throw new ClientErrorException(HttpStatus.NOT_FOUND, "Address not found or empty");
            }
            PaymentMethod paymentMethod = paymentMethodDao.findByAddress(address);
            if (!nft.getOwner().getUsername().equals(authToken.username())) {
                try {
                    if (paymentMethod != null) {
                        if (paymentMethod.getBalance() >= sale.getPrice()) {
                            paymentMethod.setBalance(paymentMethod.getBalance() - sale.getPrice());
                            paymentMethodDao.update(paymentMethod);
                            nft.setOwner(buyer);
                            nftDao.update(nft);
                            buyer.setRank(buyer.getRank() + 1);
                            DBManager.getInstance().getUserDao().update(buyer);
                            saleDao.remove(id);
                        } else {
                            throw new ClientErrorException(HttpStatus.PAYMENT_REQUIRED, "Insufficient balance");
                        }
                    } else {
                        throw new ClientErrorException(HttpStatus.NOT_FOUND, "Payment method not found");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                throw new ClientErrorException(HttpStatus.FORBIDDEN, "Buyer already owns the NFT");
            }
    }
}


