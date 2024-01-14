package org.nftfr.backend.rest.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.nftfr.backend.persistence.DBManager;
import org.nftfr.backend.persistence.dao.NftDao;
import org.nftfr.backend.persistence.model.Nft;
import org.nftfr.backend.rest.model.AuthToken;
import org.nftfr.backend.rest.model.ClientErrorException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Map;

//caricamento dell'immagine, le find, prende l'id e ritorna l'immagine
@RestController
@RequestMapping("/nft")
public class NftRest {
    private final  NftDao nftDao = DBManager.getInstance().getNftDao();

    public record CreateParams(String caption, String title, double value, ArrayList<String> tag, String data){
        private static String bytesToHexString(byte[] bytes) {
            final char[] hexArray = "0123456789ABCDEF".toCharArray();
            char[] hexChars = new char[bytes.length * 2];
            for (int j = 0; j < bytes.length; j++) {
                int v = bytes[j] & 0xFF;
                hexChars[j * 2] = hexArray[v >>> 4];
                hexChars[j * 2 + 1] = hexArray[v & 0x0F];
            }
            return new String(hexChars);
        }

        private String getNftId() {
            try {
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                byte[] hash = digest.digest(Base64.getUrlDecoder().decode(data));
                return bytesToHexString(hash);
            } catch (NoSuchAlgorithmException ex) {
                throw new RuntimeException(ex);
            }
        }

        public Nft asNft(String username){
            Nft nft = new Nft();
            nft.setCaption(caption);
            nft.setTitle(title);
            nft.setValue(value);
            nft.setTag(tag);
            nft.setAuthor(username);
            nft.setOwner(username);
            nft.setId(getNftId());
            return nft;
        }
    }

    public record UpdateParams(String title, String caption, double value, ArrayList<String> tag) {}

    @PutMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, String> create(@RequestBody CreateParams params, HttpServletRequest req) {
        AuthToken authToken = AuthToken.fromRequest(req);
        Nft nft = params.asNft(authToken.username());
        nftDao.create(nft);
        // TODO: save the image locally.
        return Collections.singletonMap("id", nft.getId());
    }

    @PutMapping("/update/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void update(@PathVariable String id, @RequestBody UpdateParams params, HttpServletRequest req) {
        AuthToken authToken = AuthToken.fromRequest(req);
        Nft nft = nftDao.findByPrimaryKey(id);
        if (nft == null)
            throw new ClientErrorException(HttpStatus.NOT_FOUND, "The nft does not exist");

        // Only owners can update their nfts.
        if (!nft.getOwner().equals(authToken.username()))
            throw new ClientErrorException(HttpStatus.FORBIDDEN, "You don't have the permissions for this action");

        nft.setTitle(params.title());
        nft.setCaption(params.caption());
        nft.setValue(params.value());
        nft.setTag(params.tag());
        nftDao.update(nft);
    }

    @DeleteMapping(value = "delete/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id, HttpServletRequest req) {
        AuthToken authToken = AuthToken.fromRequest(req);
        Nft nft = nftDao.findByPrimaryKey(id);
        if (nft == null)
            throw new ClientErrorException(HttpStatus.NOT_FOUND, "The nft does not exist");

        // Only admins and the owner can delete a nft.
        if (!nft.getOwner().equals(authToken.username()) && !authToken.admin())
            throw new ClientErrorException(HttpStatus.FORBIDDEN, "You don't have the permissions for this action");

        nftDao.delete(id);
        // TODO: delete image.
    }
}



