// src/main/java/com/finalcall/authenticationservice/config/JwtConfig.java

package com.finalcall.authenticationservice.config;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.Arrays;
import java.util.Base64;

import com.finalcall.authenticationservice.entity.User;

@Configuration
public class JwtConfig {

    @Value("${jwt.private.key.path}")
    private String privateKeyPath;

    @Value("${jwt.expiration}") // in seconds
    private Long jwtExpirationInSeconds;

    /**
     * Bean to load the RSA private key from the specified path.
     *
     * @return The RSAPrivateKey instance.
     * @throws Exception If an error occurs while reading or parsing the key.
     */
    @Bean
    public RSAPrivateKey privateKey() throws Exception {
        String key = new String(Files.readAllBytes(Paths.get(privateKeyPath)));
        key = key.replaceAll("\\n", "")
                 .replace("-----BEGIN PRIVATE KEY-----", "")
                 .replace("-----END PRIVATE KEY-----", "");
        byte[] decoded = Base64.getDecoder().decode(key);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return (RSAPrivateKey) kf.generatePrivate(spec);
    }

    /**
     * Generates a JWT token for the authenticated user.
     *
     * @param user The User object containing user details.
     * @return Signed JWT token as a String.
     */
    public String generateToken(User user) {
        try {
            // Initialize the signer with the private key
            JWSSigner signer = new RSASSASigner(privateKey());

            // Build the JWT claims set
            JWTClaimsSet.Builder claimsBuilder = new JWTClaimsSet.Builder()
                    .subject(user.getUsername())
                    .issueTime(Date.from(Instant.now()))
                    .expirationTime(Date.from(Instant.now().plusSeconds(jwtExpirationInSeconds)));

         // Add additional claims
            claimsBuilder.claim("id", user.getId());
            claimsBuilder.claim("email", user.getEmail());
            claimsBuilder.claim("isSeller", user.getIsSeller());
            if (user.getIsSeller()) {
                claimsBuilder.claim("roles", Arrays.asList("SELLER", "BUYER"));
            } else {
                claimsBuilder.claim("roles", Arrays.asList("BUYER"));
            }

            // Add other necessary claims as needed

            JWTClaimsSet claims = claimsBuilder.build();

            // Create the signed JWT
            SignedJWT signedJWT = new SignedJWT(
                    new JWSHeader.Builder(JWSAlgorithm.RS256).type(JOSEObjectType.JWT).build(),
                    claims
            );

            // Sign the JWT
            signedJWT.sign(signer);

            // Serialize the JWT to a compact form
            return signedJWT.serialize();
        } catch (Exception e) {
            // Wrap checked exceptions in a RuntimeException
            throw new RuntimeException("Error generating JWT token", e);
        }
    }
}
