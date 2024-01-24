package org.nftfr.backend.application;

import org.nftfr.backend.application.ClientErrorException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Collections;
import java.util.Map;

@ControllerAdvice
public class ClientErrorExceptionHandler {
    @ExceptionHandler(ClientErrorException.class)
    public ResponseEntity<Map<String, String>> generateException(ClientErrorException ex) {
        return new ResponseEntity<>(Collections.singletonMap("message", ex.getMessage()), ex.getStatus());
    }
}
