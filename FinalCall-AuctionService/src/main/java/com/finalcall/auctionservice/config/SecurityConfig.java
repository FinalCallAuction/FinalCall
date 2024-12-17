// src/main/java/com/finalcall/auctionservice/config/SecurityConfig.java

package com.finalcall.auctionservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configures security settings for the AuctionService.
 */
@Configuration
public class SecurityConfig {

    /**
     * Configures the security filter chain.
     *
     * @param http The HttpSecurity object.
     * @return The configured SecurityFilterChain.
     * @throws Exception In case of any configuration errors.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Enable CORS with default configuration
            .cors(Customizer.withDefaults())
            // Disable CSRF as we're using tokens
            .csrf(csrf -> csrf.disable())
            // Define authorization rules
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auctions/create").authenticated() // Require auth for creating auctions
                .requestMatchers("/api/auctions/*/decrement").authenticated() // Corrected pattern for decrements
                .requestMatchers("/ws/**").permitAll() // Allow WebSocket connections
                .anyRequest().permitAll() // Allow all other requests
            )
            // Configure as an OAuth2 Resource Server
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(Customizer.withDefaults())
            );

        return http.build();
    }
}
