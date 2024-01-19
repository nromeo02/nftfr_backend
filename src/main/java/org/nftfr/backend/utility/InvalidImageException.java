package org.nftfr.backend.utility;

public class InvalidImageException extends RuntimeException {
    InvalidImageException() {
        super("Invalid image");
    }
}
