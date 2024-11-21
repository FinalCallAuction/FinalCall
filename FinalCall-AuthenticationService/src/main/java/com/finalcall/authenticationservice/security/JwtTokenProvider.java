package com.finalcall.authenticationservice.security;

import org.springframework.stereotype.Component;
import com.finalcall.authenticationservice.entity.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.security.PrivateKey;

@Component
public class JwtTokenProvider {

    private final PrivateKey privateKey;

    public JwtTokenProvider(PrivateKey privateKey) {
        this.privateKey = privateKey;
    }

    /**
     * Generates a JWT token for the authenticated user.
     *
     * @param user The authenticated user.
     * @return The JWT token as a String.
     */
    public String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", user.getId());
        claims.put("email", user.getEmail());
        claims.put("isSeller", user.getIsSeller());

        // Add 'roles' claim based on user's role
        if (user.getIsSeller()) {
            claims.put("roles", Arrays.asList("SELLER"));
        } else {
            claims.put("roles", Arrays.asList("BUYER"));
        }

        Instant now = Instant.now();
        Instant expiryDate = now.plus(1, ChronoUnit.DAYS);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getUsername())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiryDate))
                .signWith(privateKey, SignatureAlgorithm.RS256)
                .compact();
    }


}
