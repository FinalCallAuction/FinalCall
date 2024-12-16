// src/main/java/com/finalcall/auctionservice/config/FeignConfig.java

package com.finalcall.auctionservice.config;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.*;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

/**
 * Configuration class for Feign clients.
 */
@Configuration
public class FeignConfig {

    @Value("${spring.security.oauth2.client.registration.auction-service-client.client-id}")
    private String clientRegistrationId;

    /**
     * Configures a Feign RequestInterceptor to add OAuth2 tokens to outgoing requests.
     *
     * @param authorizedClientManager The OAuth2AuthorizedClientManager.
     * @return The configured RequestInterceptor.
     */
    @Bean
    public RequestInterceptor oauth2FeignRequestInterceptor(OAuth2AuthorizedClientManager authorizedClientManager) {
        return new OAuth2FeignRequestInterceptor(authorizedClientManager, clientRegistrationId);
    }

    /**
     * Configures the OAuth2AuthorizedClientManager with client credentials.
     *
     * @param clientRegistrationRepository The ClientRegistrationRepository.
     * @param authorizedClientService      The OAuth2AuthorizedClientService.
     * @return The configured OAuth2AuthorizedClientManager.
     */
    @Bean
    public OAuth2AuthorizedClientManager authorizedClientManager(
            ClientRegistrationRepository clientRegistrationRepository,
            OAuth2AuthorizedClientService authorizedClientService) {

        OAuth2AuthorizedClientProvider authorizedClientProvider =
                OAuth2AuthorizedClientProviderBuilder.builder()
                        .clientCredentials()
                        .build();

        AuthorizedClientServiceOAuth2AuthorizedClientManager authorizedClientManager =
                new AuthorizedClientServiceOAuth2AuthorizedClientManager(
                        clientRegistrationRepository,
                        authorizedClientService
                );

        authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);

        return authorizedClientManager;
    }
}
