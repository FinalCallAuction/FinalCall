package com.finalcall.catalogueservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.http.HttpMethod;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.interfaces.RSAPublicKey;
import java.security.KeyFactory;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/items/create").authenticated()
                .anyRequest().permitAll()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(Customizer.withDefaults())
            );
        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        try {
            String publicKeyPath = "src/main/resources/finalcall_public_key.pem";
            byte[] keyBytes = Files.readAllBytes(Paths.get(publicKeyPath));

            String publicKeyContent = new String(keyBytes)
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s+", "");

            keyBytes = Base64.getDecoder().decode(publicKeyContent);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
            RSAPublicKey publicKey = (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(keySpec);

            return NimbusJwtDecoder.withPublicKey(publicKey).build();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to load public key", e);
        }
    }
}
