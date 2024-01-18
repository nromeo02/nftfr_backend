package org.nftfr.backend.rest.model;

public class InvalidImageException extends RuntimeException {
    InvalidImageException() {
        super("Invalid image");
    }
}
