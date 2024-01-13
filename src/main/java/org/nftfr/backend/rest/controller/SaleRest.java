package org.nftfr.backend.rest.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.nftfr.backend.persistence.DBManager;
import org.nftfr.backend.persistence.dao.NftDao;
import org.nftfr.backend.persistence.dao.SaleDao;
import org.nftfr.backend.persistence.model.Sale;
import org.nftfr.backend.persistence.model.Nft;
import org.nftfr.backend.persistence.model.User;
import org.nftfr.backend.rest.model.AuthToken;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.LocalDateTime;

//create delete (per amministratori) buy (se c'è endtime)
@RestController
@RequestMapping("/sale")
public class SaleRest {
    private final SaleDao saleDao = DBManager.getInstance().getSaleDao();
    private final NftDao nftDao = DBManager.getInstance().getNftDao();
    private record CreateParams (String idNft, double price, LocalDateTime creationDate, Duration duration){
        public Sale asSale(){
            Sale sale = new Sale();
            sale.setId(1);
            sale.setIdNft("prenderla dal database? o la passiamo direttamente come stringa?");
            sale.setPrice(price);
            sale.setCreationDate(creationDate);
            if(duration.isZero()){
                return sale;
            }
            sale.setEndTime(creationDate.plus(duration));
            return sale;
        }
    }
    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<String> createSale(@RequestBody CreateParams createParams, HttpServletRequest request) {
        AuthToken authToken =  AuthToken.fromRequest(request);
        if(authToken==null){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token");
        }
        saleDao.add(createParams.asSale());
        return ResponseEntity.ok("Sale created successfully");
    }
    @DeleteMapping("/delete/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<String> deleteSale(@PathVariable int id,HttpServletRequest request) {
        AuthToken authToken = AuthToken.fromRequest(request);
        if(!authToken.admin()){
            throw new RuntimeException("Non sei amministratore");
        }
        else{
            saleDao.remove(id);
            return ResponseEntity.ok("Sale deleted successfully");
        }
    }

    @GetMapping("/buy/{id}")//bisogna implementare il metodo di pagamento passare un address tramite stringa
    public ResponseEntity<String> buy(@PathVariable int id, HttpServletRequest request) {
        AuthToken authToken = AuthToken.fromRequest(request);
        if (authToken == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token");
        } else {

            Sale sale = saleDao.findById(id);

            if (sale == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Sale not found");
            }
//cosi controlla che non sia un'asta o che non sia finita
            if (sale.getEndTime() != null && sale.getEndTime().isBefore(LocalDateTime.now())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Auction has ended");
            }


            Nft nft = nftDao.findByPrimaryKey(sale.getIdNft());

            if (nft == null) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("NFT not found");
            }

            User buyer = DBManager.getInstance().getUserDao().findByUsername(authToken.username());

            if (buyer == null) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Buyer not found");
            }

            // Verifica se l'acquirente possiede già l'NFT
            if (!nft.getOwner().equals(authToken.username())) {
                // L'acquirente non possiede l'NFT, quindi procedi con l'acquisto
                // Rimuovi la vendita
                saleDao.remove(id);

                // Trasferisci l'NFT al nuovo proprietario (l'acquirente)
                nft.setOwner(authToken.username());
                nftDao.update(nft);

                // Aggiorna il rank dell'utente acquirente (ad esempio, aumenta il numero di NFT posseduti)
                buyer.setRank(buyer.getRank()+1);
                DBManager.getInstance().getUserDao().update(buyer);

                return ResponseEntity.ok("Purchase successful");
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Buyer already owns the NFT");
            }
        }
    }
}

