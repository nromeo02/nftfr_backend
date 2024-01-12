package org.nftfr.backend.rest.controller;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import org.nftfr.backend.persistence.DBManager;
import org.nftfr.backend.persistence.dao.NftDao;
import org.nftfr.backend.persistence.model.Nft;
import org.nftfr.backend.rest.model.AuthToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;

//create, delete, findByUser,
@RestController
@RequestMapping("/nft")
public class NftRest {
    private NftDao nftDao = DBManager.getInstance().getNftDao();

    private record CreateParams(String caption, String title, double value, ArrayList<String> tag){
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
    public void createNft(@RequestBody CreateParams createparams, HttpServletRequest request) {
        AuthToken authToken =  AuthToken.fromRequest(request);
        if(authToken==null){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token");
        }
            nftDao.create(createparams.asNft(authToken.username()));
    }
    @DeleteMapping(value = "delete/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteNft(@PathVariable String id) {

        Nft nft = nftDao.findByPrimaryKey(id);
        if (nft == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Nft not found");
        }
        nftDao.delete(id);
    }
}



