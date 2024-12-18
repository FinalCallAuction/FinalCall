package com.finalcall.auctionservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.*;
import java.util.Arrays;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/ws/**").permitAll()
                .requestMatchers("/ws/internal").permitAll()
                .requestMatchers("/sockjs-node/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/auctions/item/*").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/auctions/*/bids").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/auctions/**").permitAll()
                .requestMatchers("/api/auctions/*/bid").hasAuthority("SCOPE_write")
                .requestMatchers("/api/auctions/*/decrement").hasAuthority("SCOPE_write")
                .requestMatchers("/api/auctions/create").hasAuthority("SCOPE_write")
                .requestMatchers("/api/notifications/**").authenticated()
                .anyRequest().permitAll()
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setExposedHeaders(Arrays.asList("Access-Control-Allow-Origin", "Access-Control-Allow-Credentials", "Authorization"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
