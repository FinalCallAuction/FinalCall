// src/main/java/com/finalcall/authenticationservice/config/AuthorizationServerConfig.java

package com.finalcall.authenticationservice.config;

import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.UUID;
import java.util.Optional;
import java.util.List; // Import List

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.configuration.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.authorization.client.*;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.web.SecurityFilterChain;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import com.finalcall.authenticationservice.repository.UserRepository;
import com.finalcall.authenticationservice.entity.User;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
public class AuthorizationServerConfig {

    private static final Logger logger = LoggerFactory.getLogger(AuthorizationServerConfig.class);

    @Bean
    @Order(1)
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
        OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);
        http.cors(); // Enable CORS
        return http.formLogin().and().build(); // Enable form login for the authorization server
    }

    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder()
            .issuer("http://localhost:8081")
            .jwkSetEndpoint("/.well-known/jwks.json")
            .build();
    }

    @Bean
    public JWKSource<SecurityContext> jwkSource() {
        RSAKey rsaKey = generateRsa();
        JWKSet jwkSet = new JWKSet(rsaKey);
        return (jwkSelector, context) -> jwkSelector.select(jwkSet);
    }

    @Bean
    public RegisteredClientRepository registeredClientRepository(PasswordEncoder passwordEncoder) {
        RegisteredClient auctionServiceClient = RegisteredClient.withId(UUID.randomUUID().toString())
            .clientId("auction-service-client") // Ensure this matches the AuctionService's client_id
            .clientSecret(passwordEncoder.encode("auction-service-secret"))
            .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
            .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
            .scope("read")
            .scope("write")
            .build();

        RegisteredClient catalogueServiceClient = RegisteredClient.withId(UUID.randomUUID().toString())
            .clientId("catalogue-service-client")
            .clientSecret(passwordEncoder.encode("catalogue-service-secret"))
            .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
            .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
            .scope("read")
            .scope("write")
            .build();

        // Frontend client with authorization_code and REFRESH_TOKEN
        RegisteredClient frontendClient = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId("frontend-client")
                .clientSecret(passwordEncoder.encode("frontend-secret"))
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .redirectUri("http://localhost:3000/callback")
                .scope("read")
                .scope("write")
                .build();

        return new InMemoryRegisteredClientRepository(auctionServiceClient, catalogueServiceClient, frontendClient);
    }

    private static RSAKey generateRsa() {
        KeyPair keyPair = KeyGeneratorUtils.generateRsaKey();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        return new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID("auth-server-key")
                .build();
    }

    @Bean
    public OAuth2TokenCustomizer<JwtEncodingContext> tokenCustomizer(UserRepository userRepository) {
        return (context) -> {
            if (context.getTokenType().getValue().equals("access_token")) {
                AuthorizationGrantType grantType = context.getAuthorizationGrantType();

                if (AuthorizationGrantType.AUTHORIZATION_CODE.equals(grantType)) {
                    // For authorization_code, set 'sub' to user ID
                    Authentication principal = context.getPrincipal();
                    String username = principal.getName();

                    Optional<User> userOpt = userRepository.findByUsername(username);
                    userOpt.ifPresent(user -> {
                        context.getClaims().subject(user.getId().toString());
                        context.getClaims().claim("email", user.getEmail());
                        context.getClaims().claim("isSeller", user.getIsSeller());
                        logger.debug("Customized JWT for user ID: {}", user.getId());
                    });
                } else if (AuthorizationGrantType.CLIENT_CREDENTIALS.equals(grantType)) {
                    // For client_credentials, set 'sub' to client ID and adjust 'aud' as needed
                    String clientId = context.getRegisteredClient().getClientId();
                    context.getClaims().subject(clientId);
                    context.getClaims().audience(List.of("auction-service-client")); // Corrected to List<String>
                    context.getClaims().claim("client_id", clientId);
                    logger.debug("Customized JWT for client ID: {}", clientId);
                }
            }
        };
    }
}
