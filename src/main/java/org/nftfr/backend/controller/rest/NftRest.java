package org.nftfr.backend.controller.rest;

import jakarta.servlet.http.HttpServletRequest;
import org.nftfr.backend.persistence.DBManager;
import org.nftfr.backend.persistence.dao.NftDao;
import org.nftfr.backend.persistence.dao.ReportDao;
import org.nftfr.backend.persistence.model.Nft;
import org.nftfr.backend.persistence.model.User;
import org.nftfr.backend.application.auth.AuthToken;
import org.nftfr.backend.application.http.ClientErrorException;
import org.nftfr.backend.application.image.InvalidImageException;
import org.nftfr.backend.application.image.NftImage;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.util.*;

@RestController
@CrossOrigin(value = "http://localhost:4200", allowCredentials = "true")
@RequestMapping("/nft")
public class NftRest {
    private final NftDao nftDao = DBManager.getInstance().getNftDao();
    private final ReportDao reportDao = DBManager.getInstance().getReportDao();

    public record CreateBody(String caption, String title, ArrayList<String> tags, String data) {
        public Nft asNft(User user, NftImage image) {
            Nft nft = new Nft();
            nft.setCaption(caption);
            nft.setTitle(title);
            nft.setTags(tags);
            nft.setAuthor(user);
            nft.setOwner(user);
            nft.setId(image.getId());
            return nft;
        }
    }

    public record UpdateBody(String title, String caption, ArrayList<String> tags) {}

    public record FindBody(String owner, String author, String query, Double minPrice, Double maxPrice,
                           Integer skip, Integer take) {
    }

    @PutMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, String> create(@RequestBody CreateBody bodyParams, HttpServletRequest req) {
        AuthToken authToken = AuthToken.fromRequest(req);
        User user = DBManager.getInstance().getUserDao().findByUsername(authToken.username());
        if (user == null)
            throw new ClientErrorException(HttpStatus.NOT_FOUND, "User not found");

        // Decode image.
        NftImage image = new NftImage();

        try {
            image.loadFromEncodedData(bodyParams.data);
        } catch (InvalidImageException ex) {
            throw new ClientErrorException(HttpStatus.BAD_REQUEST, "Invalid image");
        }

        // Create NFT record.
        Nft nft = bodyParams.asNft(user, image);
        nftDao.create(nft);

        // Save the image to file.
        try {
            image.saveToFile();
        } catch (IOException ex) {
            nftDao.delete(nft.getId());
            throw new RuntimeException(ex);
        }

        return Collections.singletonMap("id", nft.getId());
    }

    @PutMapping("/update/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void update(@PathVariable String id, @RequestBody UpdateBody bodyParams, HttpServletRequest req) {
        AuthToken authToken = AuthToken.fromRequest(req);
        Nft nft = nftDao.findById(id);
        if (nft == null)
            throw new ClientErrorException(HttpStatus.NOT_FOUND, "NFT not found");

        // Only owners can update their NFTs.
        if (!nft.getOwner().getUsername().equals(authToken.username()))
            throw new ClientErrorException(HttpStatus.FORBIDDEN, "You don't have the permissions for this action");

        nft.setTitle(bodyParams.title());
        nft.setCaption(bodyParams.caption());
        nft.setTags(bodyParams.tags());
        nftDao.update(nft);
    }

    @PostMapping(value = "/find")
    @ResponseStatus(HttpStatus.OK)
    public List<Nft> find(@RequestBody FindBody bodyParams) {
        // Filter by owner.
        if (bodyParams.owner() != null)
            return nftDao.findByOwner(bodyParams.owner());

        // Filter by author.
        if (bodyParams.author() != null)
            return nftDao.findByAuthor(bodyParams.author());

        // Filter by other parameters.
        double minPrice = bodyParams.minPrice() != null ? bodyParams.minPrice() : 0.0;
        double maxPrice = bodyParams.maxPrice() != null ? bodyParams.maxPrice() : Double.MAX_VALUE;
        int skip = bodyParams.skip() != null ? bodyParams.skip() : 0;
        int take = bodyParams.take() != null ? bodyParams.take() : 10;

        HashSet<String> queryTokens = new HashSet<>();
        if (bodyParams.query() != null)
            queryTokens.addAll(Arrays.asList(bodyParams.query().split(" ")));

        return nftDao.findByQuery(queryTokens, minPrice, maxPrice, skip, take);
    }

    @GetMapping("/get/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Nft get(@PathVariable String id) {
        Nft nft = nftDao.findById(id);
        if (nft == null)
            throw new ClientErrorException(HttpStatus.NOT_FOUND, "NFT not found");

        return nft;
    }

    @GetMapping(value = "/get/{id}/image", produces = MediaType.IMAGE_PNG_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public byte[] getImage(@PathVariable String id) {
        try {
            return NftImage.loadWithId(id).getPngData();
        } catch (FileNotFoundException ex) {
            throw new ClientErrorException(HttpStatus.NOT_FOUND, "NFT not found");
        }
    }

    @PutMapping("/report/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void report(@PathVariable String id, HttpServletRequest req, @RequestBody Map<String, String> reportData) {
        AuthToken authToken = AuthToken.fromRequest(req);
        User user = DBManager.getInstance().getUserDao().findByUsername(authToken.username());
        if (user == null)
            throw new ClientErrorException(HttpStatus.NOT_FOUND, "User not found");

        if (nftDao.findById(id) == null)
            throw new ClientErrorException(HttpStatus.NOT_FOUND, "NFT not found");

        reportDao.createOrUpdateReport(id, reportData.get("comment"));
    }
}