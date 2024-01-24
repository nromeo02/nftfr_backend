package org.nftfr.backend.application;

public class InvalidImageException extends RuntimeException {
    InvalidImageException() {
        super("Invalid image");
    }
}
