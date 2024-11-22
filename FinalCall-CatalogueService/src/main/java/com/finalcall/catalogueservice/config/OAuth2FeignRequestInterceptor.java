package com.finalcall.catalogueservice.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.security.oauth2.client.*;
import org.springframework.security.oauth2.core.OAuth2AccessToken;

public class OAuth2FeignRequestInterceptor implements RequestInterceptor {

    private final OAuth2AuthorizedClientManager authorizedClientManager;

    public OAuth2FeignRequestInterceptor(OAuth2AuthorizedClientManager authorizedClientManager) {
        this.authorizedClientManager = authorizedClientManager;
    }

    @Override
    public void apply(RequestTemplate template) {
        String clientRegistrationId = "catalogue-service-client"; // Must match the registration ID in application.properties

        OAuth2AuthorizeRequest authorizeRequest = OAuth2AuthorizeRequest.withClientRegistrationId(clientRegistrationId)
                .principal("catalogue-service-client") // Can be any string; not used in client_credentials
                .build();

        OAuth2AuthorizedClient authorizedClient = authorizedClientManager.authorize(authorizeRequest);

        if (authorizedClient != null && authorizedClient.getAccessToken() != null) {
            OAuth2AccessToken accessToken = authorizedClient.getAccessToken();
            System.out.println("Access Token: " + accessToken.getTokenValue()); // Logging the token for debugging
            template.header("Authorization", "Bearer " + accessToken.getTokenValue());
        } else {
            System.out.println("No valid access token found"); // Logging when there is no token found
        }
    }
}
