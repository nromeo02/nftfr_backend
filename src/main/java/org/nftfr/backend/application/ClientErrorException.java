package org.nftfr.backend.application;

import org.springframework.http.HttpStatus;

public class ClientErrorException extends RuntimeException {
    private final HttpStatus status;
    private final String message;
    public ClientErrorException(HttpStatus status, String message) {
        if (!status.is4xxClientError())
            throw new RuntimeException("Invalid status code");

        this.status = status;
        this.message = message;
    }

    public HttpStatus getStatus() {
        return status;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
