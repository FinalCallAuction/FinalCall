package com.finalcall.auctionservice.service;

import com.finalcall.auctionservice.config.FeignConfig;
import com.finalcall.auctionservice.dto.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
    name = "authentication-service",
    url = "${authentication.service.url}",
    configuration = FeignConfig.class
)
public interface AuthenticationServiceClient {

    @GetMapping("/api/user/{id}")
    UserDTO getUserById(@PathVariable("id") Long id);
}
