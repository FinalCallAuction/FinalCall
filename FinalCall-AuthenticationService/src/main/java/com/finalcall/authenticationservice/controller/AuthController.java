// src/main/java/com/finalcall/authenticationservice/controller/AuthController.java

package com.finalcall.authenticationservice.controller;

import com.finalcall.authenticationservice.entity.User;
import com.finalcall.authenticationservice.service.UserService;
import com.finalcall.authenticationservice.dto.UserDTO;
import com.finalcall.authenticationservice.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:3000") // Adjust as per frontend port
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider; // Autowire JwtTokenProvider

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        try {
            User registeredUser = userService.registerUser(user);
            String token = jwtTokenProvider.generateToken(registeredUser); // Pass User object
            Map<String, Object> response = new HashMap<>();
            response.put("message", "User registered successfully");
            response.put("token", token);
            response.put("user", new UserDTO(registeredUser.getId(), registeredUser.getUsername(), registeredUser.getEmail()));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody Map<String, String> loginData) {
        String username = loginData.get("username");
        String password = loginData.get("password");
        Optional<User> userOpt = userService.findByUsername(username);
        if (!userOpt.isPresent()) {
            return ResponseEntity.status(404).body("Username does not exist");
        }
        User user = userOpt.get();
        if (!userService.authenticate(username, password).isPresent()) {
            return ResponseEntity.status(401).body("Credentials do not match");
        }
        String token = jwtTokenProvider.generateToken(user); // Pass User object
        UserDTO userDTO = new UserDTO(user.getId(), user.getUsername(), user.getEmail());
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Login successful");
        response.put("token", token);
        response.put("user", userDTO);
        return ResponseEntity.ok(response);
    }
}
