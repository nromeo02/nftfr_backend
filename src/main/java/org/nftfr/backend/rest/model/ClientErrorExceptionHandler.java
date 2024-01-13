package org.nftfr.backend.rest.model;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ClientErrorExceptionHandler {
    public record ErrorMessage(String message) {}

    @ExceptionHandler(ClientErrorException.class)
    public ResponseEntity<ErrorMessage> generateException(ClientErrorException ex) {
        return new ResponseEntity<>(new ErrorMessage(ex.getMessage()), ex.getStatus());
    }
}
