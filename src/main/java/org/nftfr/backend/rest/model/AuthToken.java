package org.nftfr.backend.rest.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.nftfr.backend.persistence.model.User;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;

public record AuthToken(@JsonIgnore String username, @JsonIgnore boolean admin, String token) {
    private static final SecretKey SECRET = decodeSecret();

    private static SecretKey decodeSecret() {
        return Keys.hmacShaKeyFor(Base64.getDecoder().decode("odAh38us0qj7coVBSfrvAEyKxJ2ecgqa8oPAPwvZi/c="));
    }

    public static AuthToken generate(User user) {
        Calendar now = Calendar.getInstance();
        Calendar exp = Calendar.getInstance();

        // Set expiration after 1 day.
        exp.add(Calendar.DAY_OF_YEAR, 1);

        return new AuthToken(user.getUsername(), user.isAdmin(), Jwts.builder()
                .subject(user.getUsername())
                .issuedAt(now.getTime())
                .expiration(exp.getTime())
                .claim("admin", user.isAdmin())
                .signWith(SECRET)
                .compact());
    }

    public static AuthToken fromRequest(HttpServletRequest req) {
        // Read HTTP header.
        String authValue = req.getHeader("Authorization");
        if (authValue != null && authValue.contains("Bearer ")) {
            // Extract and verify token.
            String token = authValue.substring("Bearer ".length());

            try {
                Claims tokenData = Jwts.parser().verifyWith(SECRET).build().parseSignedClaims(token).getPayload();
                if (tokenData.getExpiration().before(new Date()))
                    return null;

                return new AuthToken(tokenData.getSubject(), tokenData.get("admin", Boolean.class), token);
            } catch (JwtException ex) {
                return null;
            }
        }

        return null;
    }
}
