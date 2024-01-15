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

//create delete (per amministratori) buy (se c'è endtime)
@RestController
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
        AuthToken authToken = AuthToken.fromRequest(request);
        if (authToken == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token");
        }
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
/*
    @PutMapping("/buy/{id}")//bisogna implementare il metodo di pagamento passare un address tramite stringa
    //questa funziona solo per i prezzi fissi
    public ResponseEntity<String> buy(@PathVariable int id, HttpServletRequest request, String username, String address) {
        AuthToken authToken = AuthToken.fromRequest(request);
        if (authToken == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token");
        } else {

            Sale sale = saleDao.findById(id);

            if (sale == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Sale not found");
            }
            Nft nft = nftDao.findByPrimaryKey(sale.getIdNft());

            if (nft == null) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("NFT not found");
            }

            User buyer = DBManager.getInstance().getUserDao().findByUsername(authToken.username());

            if (buyer == null) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Buyer not found");
            }
            PaymentMethod paymentMethod = paymentMethodDao.findByAddress(address);
            // Verifica se l'acquirente possiede già l'NFT
            if (!nft.getOwner().equals(authToken.username())) {
                // L'acquirente non possiede l'NFT, quindi procedi con l'acquisto
                // Rimuovi la vendita
                if (paymentMethod.getBalance() >= sale.getPrice()) {
                    saleDao.remove(id);
                    nft.setOwner(authToken.username());
                    nftDao.update(nft);
                    buyer.setRank(buyer.getRank() + 1);
                    DBManager.getInstance().getUserDao().update(buyer);
                    paymentMethod.setBalance(paymentMethod.getBalance() - sale.getPrice());
                    paymentMethodDao.update(paymentMethod);
                    return ResponseEntity.ok("Purchase successful");
                } else {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Not enough money in this payment method");
                }
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Buyer already owns the NFT");
            }
        }
    }
  */


    @PutMapping("/buy/{id}")
    public ResponseEntity<String> buy(@PathVariable int id, HttpServletRequest request, @RequestBody Map<String, String> requestBody) {
        AuthToken authToken = AuthToken.fromRequest(request);
        if (authToken == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token");
        } else {
            Sale sale = saleDao.findById(id);

            if (sale == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Sale not found");
            }

            Nft nft = nftDao.findByPrimaryKey(sale.getIdNft());

            if (nft == null) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("NFT not found");
            }

            User buyer = DBManager.getInstance().getUserDao().findByUsername(authToken.username());
            System.out.println("Buyer's username: " + authToken.username());
            if (buyer == null) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Buyer not found");
            }

            String address = requestBody.get("address");  // Estrai l'indirizzo dal corpo della richiesta JSON
            System.out.println("address: " + address);
            PaymentMethod paymentMethod = paymentMethodDao.findByAddress(address);
            System.out.println("Buyer's address: " + paymentMethod.getAddress());

            // Verifica se l'acquirente possiede già l'NFT
        if (!nft.getOwner().equals(authToken.username())) {
            // L'acquirente non possiede l'NFT, quindi procedi con l'acquisto
            // Rimuovi la vendita
            try {
                if (paymentMethod != null) {
                    System.out.println("Buyer's balance before purchase: " + paymentMethod.getBalance());
                    System.out.println("Buyer's address: " + paymentMethod.getAddress());

                    System.out.println("Buyer's username: " + paymentMethod.getUsername());

                    System.out.println("Sale price: " + sale.getPrice());

                    if (paymentMethod.getBalance() >= sale.getPrice()) {
                        saleDao.remove(id);
                        nft.setOwner(authToken.username());
                        nftDao.update(nft);
                        buyer.setRank(buyer.getRank() + 1);
                        DBManager.getInstance().getUserDao().update(buyer);
                        paymentMethod.setBalance(paymentMethod.getBalance() - sale.getPrice());
                        paymentMethodDao.update(paymentMethod);

                        System.out.println("Buyer's balance after purchase: " + paymentMethod.getBalance());
                        return ResponseEntity.ok("Purchase successful");
                    } else {
                        System.out.println("Insufficient balance");
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Insufficient balance");
                    }
                } else {
                    System.out.println("Payment method not found");
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Payment method not found");
                }
            } catch (Exception e) {
                // Aggiungi log per catturare l'eccezione
                e.printStackTrace();
                System.out.println("Exception details: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal Server Error");
            }
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Buyer already owns the NFT");
        }
    }
}

}


