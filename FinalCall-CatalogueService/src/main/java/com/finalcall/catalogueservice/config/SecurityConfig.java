package com.finalcall.catalogueservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import java.nio.charset.StandardCharsets;
import java.nio.file.NoSuchFileException;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import org.springframework.http.HttpMethod; // Add this import

@Configuration
public class SecurityConfig {

    @Value("${jwt.public.key.path}")
    private String jwtPublicKeyPath;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auctions/**").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/auctions/**").permitAll()
                .anyRequest().authenticated() 
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(Customizer.withDefaults())
            );

        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        try {
            // Load the public key from the classpath
            ClassPathResource resource = new ClassPathResource(jwtPublicKeyPath);
            if (!resource.exists()) {
                throw new NoSuchFileException("Public key file not found: " + jwtPublicKeyPath);
            }

            String publicKeyContent = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
            // Remove the PEM file header and footer, and decode properly
            publicKeyContent = publicKeyContent
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s", ""); 

            byte[] decoded = Base64.getDecoder().decode(publicKeyContent);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decoded);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            RSAPublicKey publicKey = (RSAPublicKey) kf.generatePublic(keySpec);

            return NimbusJwtDecoder.withPublicKey(publicKey).build();
        } catch (NoSuchFileException e) {
            throw new IllegalArgumentException("Public key file not found: " + jwtPublicKeyPath, e);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to decode public key for JWT", e);
        }
    }

}
