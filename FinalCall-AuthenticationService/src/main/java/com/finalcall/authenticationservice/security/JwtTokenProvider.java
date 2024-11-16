// src/main/java/com/finalcall/authenticationservice/security/JwtTokenProvider.java

package com.finalcall.authenticationservice.security;

import org.springframework.stereotype.Component;
import com.finalcall.authenticationservice.entity.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.security.PrivateKey;

@Component
public class JwtTokenProvider {

    private final PrivateKey privateKey;

    public JwtTokenProvider(PrivateKey privateKey) {
        this.privateKey = privateKey;
    }

    public String generateToken(User user) {
        Instant now = Instant.now();
        Instant expiration = now.plus(2, ChronoUnit.HOURS);

        return Jwts.builder()
                .setSubject(user.getUsername())
                .claim("id", user.getId())
                .claim("email", user.getEmail())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiration))
                .signWith(privateKey, SignatureAlgorithm.RS256)
                .compact();
    }
}
