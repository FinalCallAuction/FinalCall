// src/main/java/com/finalcall/catalogueservice/config/OAuth2FeignRequestInterceptor.java

package com.finalcall.catalogueservice.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.core.OAuth2AccessToken;

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
        OAuth2AuthorizeRequest authorizeRequest = OAuth2AuthorizeRequest.withClientRegistrationId(clientRegistrationId)
                .principal(clientRegistrationId) // Not used in client_credentials
                .build();

        var authorizedClient = authorizedClientManager.authorize(authorizeRequest);

        if (authorizedClient != null) {
            OAuth2AccessToken accessToken = authorizedClient.getAccessToken();
            if (accessToken != null) {
                template.header("Authorization", "Bearer " + accessToken.getTokenValue());
            }
        }
    }
}
