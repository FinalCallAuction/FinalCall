package com.finalcall.backend.controller;

import com.finalcall.backend.entity.User;
import com.finalcall.backend.dto.UserDTO;
import com.finalcall.backend.service.UserService;
import com.finalcall.backend.security.JwtAuthenticationFilter;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:3000") // Adjust as per frontend port
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        try {
            User registeredUser = userService.registerUser(user);
            // Generate token for new user
            String token = generateToken(registeredUser.getUsername());
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
        Optional<User> userOpt = userService.authenticate(username, password);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            UserDTO userDTO = new UserDTO(user.getId(), user.getUsername(), user.getEmail());
            // Generate JWT token
            String token = generateToken(user.getUsername());
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Login successful");
            response.put("token", token);
            response.put("user", userDTO);
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(401).body("Invalid credentials");
        }
    }

    private String generateToken(String username) {
        Instant now = Instant.now();
        // Setting up a 2-hour expiration for demonstration; adjust as needed
        Instant expiration = now.plus(2, ChronoUnit.HOURS);
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiration))
                .signWith(Keys.hmacShaKeyFor(JwtAuthenticationFilter.SECRET_KEY.getBytes()), SignatureAlgorithm.HS256)
                .compact();
    }
}
