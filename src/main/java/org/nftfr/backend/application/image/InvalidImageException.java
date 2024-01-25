package org.nftfr.backend.application.image;

public class InvalidImageException extends RuntimeException {
    InvalidImageException() {
        super("Invalid image");
    }
}
