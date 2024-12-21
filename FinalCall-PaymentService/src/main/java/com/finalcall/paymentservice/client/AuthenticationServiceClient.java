//package com.finalcall.paymentservice.client;
//
//import org.springframework.cloud.openfeign.FeignClient;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PathVariable;
//import com.finalcall.authenticationservice.dto.UserDTO;
//
//@FeignClient(name = "authentication-service", url = "${authentication.service.url}")
//public interface AuthenticationServiceClient {
//    @GetMapping("/api/users/{userId}")
//    UserDTO getUserById(@PathVariable("userId") Long userId);
//}