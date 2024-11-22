// src/main/java/com/finalcall/authenticationservice/controller/AuthController.java

package com.finalcall.authenticationservice.controller;

import com.finalcall.authenticationservice.dto.AuthResponse;
import com.finalcall.authenticationservice.dto.LoginRequest;
import com.finalcall.authenticationservice.entity.User;
import com.finalcall.authenticationservice.service.UserService;
import com.finalcall.authenticationservice.security.JwtTokenProvider;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:3000") // Adjust as per frontend port
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UserService userService;

    /**
     * Endpoint to register a new user.
     *
     * @param user The user details.
     * @return A response containing a success message and user details.
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        try {
            User registeredUser = userService.registerUser(user);
            // Optionally, you can auto-login the user after registration
            return ResponseEntity.ok("User registered successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Endpoint to authenticate a user and issue a JWT token.
     *
     * @param loginRequest The login credentials.
     * @return A response containing the JWT token.
     */
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest loginRequest) {
        try {
            // Authenticate the user
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsername(),
                    loginRequest.getPassword()
                )
            );

            // Generate JWT token
            String token = jwtTokenProvider.generateToken(authentication);

            // Return the token
            return ResponseEntity.ok(new AuthResponse(token));
        } catch (BadCredentialsException ex) {
            return ResponseEntity.status(401).body("Invalid username or password");
        } catch (Exception ex) {
            return ResponseEntity.status(500).body("An error occurred during login.");
        }
    }
}
