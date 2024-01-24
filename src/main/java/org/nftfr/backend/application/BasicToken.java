package org.nftfr.backend.application;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Base64;

public record BasicToken(@JsonIgnore String username, @JsonIgnore String password, String token) {
    public static BasicToken fromRequest(HttpServletRequest req) {
        // Read HTTP header.
        String authValue = req.getHeader("Authorization");
        if (authValue != null && authValue.contains("Basic ")) {
            // Decode and parse token.
            String token = authValue.substring("Basic ".length());
            String[] stringParts = new String(Base64.getDecoder().decode(token)).split(":");
            if (stringParts.length == 2) {
                String username = stringParts[0];
                String password = stringParts[1];
                return new BasicToken(username, password, token);
            }
        }

        return null;
    }
}
