// src/main/java/com/finalcall/catalogueservice/client/AuthenticationServiceClient.java

package com.finalcall.catalogueservice.client;

import com.finalcall.catalogueservice.config.FeignConfig;
import com.finalcall.catalogueservice.dto.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Feign client to communicate with the AuthenticationService.
 */
@FeignClient(name = "authentication-service", url = "${authentication.service.url}")
public interface AuthenticationServiceClient {

    @GetMapping("/api/users/{userId}")
    UserDTO getUserById(@PathVariable("userId") Long userId);
}