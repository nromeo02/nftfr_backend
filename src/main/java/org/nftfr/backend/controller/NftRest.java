package org.nftfr.backend.controller;

import org.nftfr.backend.persistence.DBManager;
import org.nftfr.backend.persistence.dao.NftDao;
import org.nftfr.backend.persistence.model.Nft;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

//create, delete, findByUser,
@RestController
@RequestMapping("/nft")
public class NftRest {
    private NftDao nftDao = DBManager.getInstance().getNftDao();

    @PostMapping("/create")
    public ResponseEntity<String> createNft(@RequestBody Nft nft) {
        try {
            nftDao.create(nft);
            return new ResponseEntity<>("NFT creato con successo", HttpStatus.CREATED);
        } catch (Exception e) {
            e.printStackTrace();

        return new ResponseEntity<>("Errore durante la creazione dell'NFT: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @DeleteMapping(value = "/nft/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteNft(@PathVariable String id) {
        NftDao nftDao = DBManager.getInstance().getNftDao();
        Nft nft = nftDao.findByPrimaryKey(id);

        if (nft == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Nft not found");
        }

        nftDao.delete(id);
    }
}



