// src/main/java/com/finalcall/authenticationservice/config/SecurityConfig.java

package com.finalcall.authenticationservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;

@Configuration
public class SecurityConfig {

    @Value("${app.allowedOrigins}")
    private String[] allowedOrigins;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(Customizer.withDefaults())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/register").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/users/**").permitAll()  // Allow public access to user profiles
                .requestMatchers(HttpMethod.PUT, "/api/users/**").authenticated()  // Require auth for updates
                .requestMatchers("/oauth2/**").permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(Customizer.withDefaults())
            .oauth2ResourceServer(oauth2 -> oauth2.jwt());

        return http.build();
    }
}
