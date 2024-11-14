package com.finalcall.backend.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.web.cors.*;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;

import java.util.Arrays;
import java.util.Collections;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(Customizer.withDefaults()) // Enable CORS
            .csrf(AbstractHttpConfigurer::disable) // Disable CSRF for simplicity (use in production with caution)
            .headers(headers -> headers.frameOptions().sameOrigin()) // For H2 console frames
            .authorizeHttpRequests(authz -> authz
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()  // Allow all OPTIONS requests (for CORS preflight)
                .requestMatchers("/api/auth/**").permitAll()             // Authentication endpoints are public
                .requestMatchers("/h2-console/**").permitAll()           // H2 console is public
                .requestMatchers("/uploads/**").permitAll()              // Ensure uploads are publicly accessible
                .requestMatchers(HttpMethod.GET, "/api/items/**").permitAll() // Anyone can view items
                .requestMatchers(HttpMethod.POST, "/api/items/create").authenticated() // Creating items requires authentication
                .anyRequest().authenticated()                             // Any other request requires authentication
            )
            .addFilterBefore(new JwtAuthenticationFilter(), org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class) // Add JWT authentication filter
            .httpBasic(AbstractHttpConfigurer::disable);  // Disable Basic auth

        return http.build();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        // Ignore authentication for the uploads folder so that images can be accessed publicly
        return (web) -> web.ignoring().requestMatchers("/uploads/**");
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        // Define CORS configuration
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Collections.singletonList("http://localhost:3000"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));
        configuration.setExposedHeaders(Arrays.asList("Authorization"));
        configuration.setAllowCredentials(true); // Required for JWT in some cases
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // Apply this config to all endpoints
        return source;
    }
}
