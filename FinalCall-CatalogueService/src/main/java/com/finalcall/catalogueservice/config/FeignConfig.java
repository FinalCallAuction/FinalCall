package com.finalcall.catalogueservice.config;

import com.finalcall.catalogueservice.exception.UserNotFoundException;
import feign.Response;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for Feign clients.
 */
@Configuration
public class FeignConfig {

    @Bean
    public ErrorDecoder errorDecoder() {
        return new CustomFeignErrorDecoder();
    }

    /**
     * Custom Feign Error Decoder to handle specific HTTP errors.
     */
    public class CustomFeignErrorDecoder implements ErrorDecoder {

        private final ErrorDecoder defaultErrorDecoder = new Default();

        @Override
        public Exception decode(String methodKey, Response response) {
            if (response.status() == 404 && methodKey.contains("getUserById")) {
                return new UserNotFoundException("User not found with the provided ID.");
            }
            return defaultErrorDecoder.decode(methodKey, response);
        }
    }
}
