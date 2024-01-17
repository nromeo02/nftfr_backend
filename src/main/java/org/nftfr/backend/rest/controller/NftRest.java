package org.nftfr.backend.rest.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.nftfr.backend.ConfigManager;
import org.nftfr.backend.persistence.DBManager;
import org.nftfr.backend.persistence.dao.NftDao;
import org.nftfr.backend.persistence.model.Nft;
import org.nftfr.backend.rest.model.AuthToken;
import org.nftfr.backend.rest.model.ClientErrorException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

@RestController
@CrossOrigin(value = "http://localhost:4200", allowCredentials = "true")
@RequestMapping("/nft")
public class NftRest {
    private final  NftDao nftDao = DBManager.getInstance().getNftDao();

    public record CreateParams(String caption, String title, Double value, ArrayList<String> tag, String data){
        private static String bytesToHexString(byte[] bytes) {
            final char[] hexArray = "0123456789abcdef".toCharArray();
            char[] hexChars = new char[bytes.length * 2];
            for (int j = 0; j < bytes.length; j++) {
                int v = bytes[j] & 0xFF;
                hexChars[j * 2] = hexArray[v >>> 4];
                hexChars[j * 2 + 1] = hexArray[v & 0x0F];
            }
            return new String(hexChars);
        }

        private static String makeNftId(byte[] imageData) {
            try {
                MessageDigest hasher = MessageDigest.getInstance("SHA-256");
                return bytesToHexString(hasher.digest(imageData));
            } catch (NoSuchAlgorithmException ex) {
                throw new RuntimeException(ex);
            }
        }

        private byte[] getDecodedImage() {
            return Base64.getDecoder().decode(data);
        }

        public Nft asNft(String username, byte[] imageData){
            Nft nft = new Nft();
            nft.setCaption(caption);
            nft.setTitle(title);
            nft.setValue(value);
            nft.setTag(tag);
            nft.setAuthor(username);
            nft.setOwner(username);
            nft.setId(makeNftId(imageData));
            return nft;
        }
    }

    public record UpdateParams(String title, String caption, Double value, ArrayList<String> tag) {}

    public record FindParams(String owner, String author, String query, Double minPrice, Double maxPrice) {}

    @PutMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, String> create(@RequestBody CreateParams params, HttpServletRequest req) {
        AuthToken authToken = AuthToken.fromRequest(req);

        // Convert image to PNG.
        byte[] imageData;
        ByteArrayInputStream imageInputStream = new ByteArrayInputStream(params.getDecodedImage());
        ByteArrayOutputStream imageOutputStream = new ByteArrayOutputStream();
        try {
            BufferedImage bufferedImage = ImageIO.read(imageInputStream);
            if (bufferedImage == null)
                throw new ClientErrorException(HttpStatus.BAD_REQUEST, "Invalid image type");

            ImageIO.write(bufferedImage, "png", imageOutputStream);
            imageData = imageOutputStream.toByteArray();
        } catch (IOException ex) {
            throw new ClientErrorException(HttpStatus.BAD_REQUEST, "Invalid image type");
        }

        // Create nft record.
        Nft nft = params.asNft(authToken.username(), imageData);
        nftDao.create(nft);

        // Save the image locally.
        final String imagePath = ConfigManager.getInstance().getNftImagePath() + nft.getId();
        try (FileOutputStream file = new FileOutputStream(imagePath)) {
            file.write(imageData);
        } catch (IOException ex) {
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
        if (!nft.getOwner().equals(authToken.username()))
            throw new ClientErrorException(HttpStatus.FORBIDDEN, "You don't have the permissions for this action");

        nft.setTitle(params.title());
        nft.setCaption(params.caption());
        nft.setValue(params.value());
        nft.setTag(params.tag());
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
        if (!nft.getOwner().equals(authToken.username()) && !authToken.admin())
            throw new ClientErrorException(HttpStatus.FORBIDDEN, "You don't have the permissions for this action");

        nftDao.delete(id);
        // TODO: delete image.
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
        if(nft == null)
            throw new ClientErrorException(HttpStatus.NOT_FOUND, "The nft does not exist");

        return nft;
    }

    @GetMapping(value = "/get/{id}/image", produces = MediaType.IMAGE_PNG_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public byte[] getImage(@PathVariable String id) {
        try (FileInputStream file = new FileInputStream(ConfigManager.getInstance().getNftImagePath() + id)) {
            return file.readAllBytes();
        } catch (FileNotFoundException ex) {
            throw new ClientErrorException(HttpStatus.NOT_FOUND, "The nft does not exist");
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}