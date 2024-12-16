package com.finalcall.auctionservice.client;

import com.finalcall.auctionservice.dto.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "authentication-service", url = "${authentication.service.url}")
public interface AuthenticationServiceClient {
    @GetMapping("/api/users/{userId}")
    UserDTO getUserById(@PathVariable("userId") Long userId);
}