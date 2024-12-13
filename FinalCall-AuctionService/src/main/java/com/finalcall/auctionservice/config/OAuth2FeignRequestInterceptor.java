// src/main/java/com/finalcall/auctionservice/config/OAuth2FeignRequestInterceptor.java

package com.finalcall.auctionservice.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Feign RequestInterceptor that adds the OAuth2 Bearer Token to outgoing requests.
 */
public class OAuth2FeignRequestInterceptor implements RequestInterceptor {

    private final OAuth2AuthorizedClientManager authorizedClientManager;
    private final String clientRegistrationId;

    /**
     * Constructor for OAuth2FeignRequestInterceptor.
     *
     * @param authorizedClientManager The OAuth2AuthorizedClientManager.
     * @param clientRegistrationId    The client registration ID.
     */
    public OAuth2FeignRequestInterceptor(OAuth2AuthorizedClientManager authorizedClientManager, String clientRegistrationId) {
        this.authorizedClientManager = authorizedClientManager;
        this.clientRegistrationId = clientRegistrationId;
    }

    @Override
    public void apply(RequestTemplate template) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            // No authentication available; do not set Authorization header
            return;
        }

        OAuth2AuthorizeRequest authorizeRequest = OAuth2AuthorizeRequest.withClientRegistrationId(clientRegistrationId)
                .principal(authentication)
                .build();

        var authorizedClient = authorizedClientManager.authorize(authorizeRequest);
        if (authorizedClient != null && authorizedClient.getAccessToken() != null) {
            OAuth2AccessToken accessToken = authorizedClient.getAccessToken();
            template.header("Authorization", "Bearer " + accessToken.getTokenValue());
        }
    }
}
