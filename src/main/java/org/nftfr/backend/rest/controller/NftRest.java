package org.nftfr.backend.rest.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.nftfr.backend.persistence.DBManager;
import org.nftfr.backend.persistence.dao.NftDao;
import org.nftfr.backend.persistence.model.Nft;
import org.nftfr.backend.persistence.model.User;
import org.nftfr.backend.rest.model.AuthToken;
import org.nftfr.backend.rest.model.ClientErrorException;
import org.nftfr.backend.rest.model.InvalidImageException;
import org.nftfr.backend.rest.model.NftImage;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.*;
import java.util.*;

@RestController
@CrossOrigin(value = "http://localhost:4200", allowCredentials = "true")
@RequestMapping("/nft")
public class NftRest {
    private final  NftDao nftDao = DBManager.getInstance().getNftDao();

    public record CreateParams(String caption, String title, Double value, ArrayList<String> tags, String data){
        public Nft asNft(User user, NftImage image){
            Nft nft = new Nft();
            nft.setCaption(caption);
            nft.setTitle(title);
            nft.setValue(value);
            nft.setTags(tags);
            nft.setAuthor(user);
            nft.setOwner(user);
            nft.setId(image.getId());
            return nft;
        }
    }

    public record UpdateParams(String title, String caption, Double value, ArrayList<String> tags) {}

    public record FindParams(String owner, String author, String query, Double minPrice, Double maxPrice) {}

    @PutMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, String> create(@RequestBody CreateParams params, HttpServletRequest req) {
        AuthToken authToken = AuthToken.fromRequest(req);
        User user = DBManager.getInstance().getUserDao().findByUsername(authToken.username());
        if (user == null)
            throw new ClientErrorException(HttpStatus.NOT_FOUND, "The user does not exist");

        // Decode image.
        NftImage image = new NftImage();

        try {
            image.loadFromEncodedData(params.data);
        } catch (InvalidImageException ex) {
            throw new ClientErrorException(HttpStatus.BAD_REQUEST, "Invalid image");
        }

        // Create nft record.
        Nft nft = params.asNft(user, image);
        nftDao.create(nft);

        // Save the image to file.
        try {
            image.saveToFile();
        } catch (IOException ex) {
            // Should not happen.
            nftDao.delete(nft.getId());
            throw new RuntimeException(ex);
        }

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
        if (!nft.getOwner().getUsername().equals(authToken.username()))
            throw new ClientErrorException(HttpStatus.FORBIDDEN, "You don't have the permissions for this action");

        nft.setTitle(params.title());
        nft.setCaption(params.caption());
        nft.setValue(params.value());
        nft.setTags(params.tags());
        nftDao.update(nft);
    }

    @DeleteMapping(value = "/delete/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id, HttpServletRequest req) {
        AuthToken authToken = AuthToken.fromRequest(req);
        Nft nft = nftDao.findByPrimaryKey(id);
        if (nft == null)
            throw new ClientErrorException(HttpStatus.NOT_FOUND, "The nft does not exist");

        // Only admins and the owner can delete a nft.
        if (!nft.getOwner().getUsername().equals(authToken.username()) && !authToken.admin())
            throw new ClientErrorException(HttpStatus.FORBIDDEN, "You don't have the permissions for this action");

        nftDao.delete(id);
        NftImage.deleteWithId(id);
    }

    @PostMapping(value = "/find")
    @ResponseStatus(HttpStatus.OK)
    public List<Nft> find(@RequestBody FindParams params) {
        // Filter by owner.
        if (params.owner() != null)
            return nftDao.findByOwner(params.owner());

        // Filter by author.
        if (params.author() != null)
            return nftDao.findByAuthor(params.author());

        // Filter by other parameters.
        double minPrice = params.minPrice() != null ? params.minPrice() : 0.0;
        double maxPrice = params.maxPrice() != null ? params.maxPrice() : Double.MAX_VALUE;

        HashSet<String> queryTokens = new HashSet<>();
        if (params.query() != null)
            queryTokens.addAll(Arrays.asList(params.query().split(" ")));

        return nftDao.findByQuery(queryTokens, minPrice, maxPrice);
    }

    @GetMapping("/get/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Nft get(@PathVariable String id) {
        Nft nft = nftDao.findByPrimaryKey(id);
        if (nft == null)
            throw new ClientErrorException(HttpStatus.NOT_FOUND, "The nft does not exist");

        return nft;
    }

    @GetMapping(value = "/get/{id}/image", produces = MediaType.IMAGE_PNG_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public byte[] getImage(@PathVariable String id) {
        try {
            return NftImage.loadWithId(id).getPngData();
        } catch (FileNotFoundException ex) {
            throw new ClientErrorException(HttpStatus.NOT_FOUND, "The nft does not exist");
        }
    }
    //to do report
}