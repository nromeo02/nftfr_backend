package org.nftfr.backend.rest.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.nftfr.backend.persistence.DBManager;
import org.nftfr.backend.persistence.dao.NftDao;
import org.nftfr.backend.persistence.model.Nft;
import org.nftfr.backend.rest.model.AuthToken;
import org.nftfr.backend.rest.model.ClientErrorException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

//caricamento dell'immagine, le find, prende l'id e ritorna l'immagine
@RestController
@RequestMapping("/nft")
public class NftRest {
    private final  NftDao nftDao = DBManager.getInstance().getNftDao();

    public record CreateParams(String caption, String title, double value, ArrayList<String> tag){
        public Nft asNft(String username){
            Nft nft = new Nft();
            nft.setCaption(caption);
            nft.setTitle(title);
            nft.setValue(value);
            nft.setTag(tag);
            nft.setAuthor(username);
            nft.setOwner(username);
            nft.setId("jsifnwir");
            return nft;
        }
    }
    @PostMapping("/create")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void createNft(@RequestBody CreateParams params, HttpServletRequest request) {
        AuthToken authToken = AuthToken.fromRequest(request);
        nftDao.create(params.asNft(authToken.username()));
    }

    @DeleteMapping(value = "delete/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteNft(@PathVariable String id) {
        Nft nft = nftDao.findByPrimaryKey(id);
        if (nft == null)
            throw new ClientErrorException(HttpStatus.NOT_FOUND, "Nft not found");

        nftDao.delete(id);
    }
}



