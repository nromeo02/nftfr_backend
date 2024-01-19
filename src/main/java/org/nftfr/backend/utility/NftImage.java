package org.nftfr.backend.utility;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class NftImage {
    private byte[] pngData;
    private String id;

    private static String bytesToHexString(byte[] bytes) throws NullPointerException {
        if (bytes == null)
            throw new NullPointerException();

        final char[] hexArray = "0123456789abcdef".toCharArray();
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }

        return new String(hexChars);
    }

    private static String makeId(byte[] imageData) {
        try {
            MessageDigest hasher = MessageDigest.getInstance("SHA-256");
            return bytesToHexString(hasher.digest(imageData));
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex);
        }
    }

    public NftImage() {}

    public byte[] getPngData() {
        return pngData;
    }

    public String getId() {
        if (id == null)
            id = makeId(pngData);

        return id;
    }

    public void loadFromEncodedData(String encodedData) throws InvalidImageException {
        // Decode data.
        byte[] imageData =  Base64.getDecoder().decode(encodedData);

        // Convert image to PNG.
        ByteArrayInputStream imageInputStream = new ByteArrayInputStream(imageData);
        ByteArrayOutputStream imageOutputStream = new ByteArrayOutputStream();
        try {
            BufferedImage bufferedImage = ImageIO.read(imageInputStream);
            if (bufferedImage == null)
                throw new InvalidImageException();

            ImageIO.write(bufferedImage, "png", imageOutputStream);
            pngData = imageOutputStream.toByteArray();
        } catch (IOException ex) {
            throw new InvalidImageException();
        }
    }

    public void saveToFile() throws IOException {
        final String imagePath = ConfigManager.getInstance().getNftImagePath() + '/' + id;
        try (FileOutputStream file = new FileOutputStream(imagePath)) {
            file.write(pngData);
        } catch (FileNotFoundException ex) {
            // Should not happen.
            throw new RuntimeException(ex);
        }
    }

    public static NftImage loadWithId(String id) throws FileNotFoundException {
        NftImage image = new NftImage();
        try (FileInputStream file = new FileInputStream(ConfigManager.getInstance().getNftImagePath() + '/' + id)) {
            image.pngData = file.readAllBytes();
        } catch (FileNotFoundException ex) {
            throw new FileNotFoundException(ex.getMessage());
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

        return image;
    }

    public static void deleteWithId(String id) {
        try {
            File file = new File(ConfigManager.getInstance().getNftImagePath() + '/' + id);
            Files.deleteIfExists(file.toPath());
        } catch (Exception ex) {
            // Should not happen.
            throw new RuntimeException(ex);
        }
    }
}
