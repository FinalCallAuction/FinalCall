package com.finalcall.catalogueservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Configuration
public class SecurityConfig {
    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuerUri;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(Customizer.withDefaults())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/ws/internal").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/items/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/items/user/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/itemimages/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/items/create").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/items/{id}/upload-image").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/items/**").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/items/**").authenticated()
                .anyRequest().permitAll()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .decoder(jwtDecoder())
                )
            );

        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withJwkSetUri(issuerUri + "/.well-known/jwks.json")
            .jwsAlgorithm(SignatureAlgorithm.RS256)
            .build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            // Extract authorities from JWT claims if needed
            return Collections.emptyList();
        });
        return converter;
    }
}