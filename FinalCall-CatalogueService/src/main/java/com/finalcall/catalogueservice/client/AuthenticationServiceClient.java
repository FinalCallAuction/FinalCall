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
@FeignClient(name = "authentication-service", url = "${authentication.service.url}", configuration = FeignConfig.class)
public interface AuthenticationServiceClient {

    /**
     * Get user details by user ID.
     *
     * @param id The ID of the user.
     * @return UserDTO containing user details.
     */
    @GetMapping("/api/user/{id}")
    UserDTO getUserById(@PathVariable("id") Long id);
}
