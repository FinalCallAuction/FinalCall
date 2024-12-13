// src/main/java/com/finalcall/auctionservice/service/AuthenticationServiceClient.java

package com.finalcall.auctionservice.service;

import com.finalcall.auctionservice.dto.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Feign client for interacting with the Authentication Service.
 */
@FeignClient(name = "authentication-service", url = "${authentication.service.url}")
public interface AuthenticationServiceClient {

    /**
     * Retrieves user details by user ID.
     *
     * @param userId The ID of the user.
     * @return UserDTO containing user information.
     */
    @GetMapping("/api/users/{userId}")
    UserDTO getUserById(@PathVariable("userId") Long userId);
}
