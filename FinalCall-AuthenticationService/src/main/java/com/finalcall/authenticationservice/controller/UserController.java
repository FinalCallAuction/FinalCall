package com.finalcall.authenticationservice.controller;

import com.finalcall.authenticationservice.entity.User;
import com.finalcall.authenticationservice.dto.UserDTO;
import com.finalcall.authenticationservice.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")  // Changed from "/api/user" to "/api"
@CrossOrigin(origins = "http://localhost:3000")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * Endpoint to update the user's email.
     *
     * @param userId The user ID.
     * @param updates A map containing the new email.
     * @return A success message or an error message.
     */
    @PutMapping("/{id}/email")
    public ResponseEntity<?> updateEmail(@PathVariable("id") Long userId, @RequestBody Map<String, String> updates) {
        String newEmail = updates.get("email");
        try {
            User updatedUser = userService.updateUserEmail(userId, newEmail);
            return ResponseEntity.ok("Email updated successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Endpoint to update the user's password.
     *
     * @param userId The user ID.
     * @param updates A map containing the new password.
     * @return A success message or an error message.
     */
    @PutMapping("/{id}/password")
    public ResponseEntity<?> updatePassword(@PathVariable("id") Long userId, @RequestBody Map<String, String> updates) {
        String newPassword = updates.get("password");
        try {
            User updatedUser = userService.updateUserPassword(userId, newPassword);
            return ResponseEntity.ok("Password updated successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Endpoint to get user details by ID.
     *
     * @param userId The user ID.
     * @return User details.
     */
    @GetMapping("/users/{id}")
    public ResponseEntity<?> getUserById(@PathVariable("id") Long userId) {
        Optional<User> userOpt = userService.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            // Only return public information for non-authenticated requests
            UserDTO userDTO = new UserDTO(
                user.getId(),
                user.getUsername(),
                null,  // Don't include email
                user.getFirstName(),
                user.getLastName(),
                null,  // Don't include private address details
                null,
                user.getCountry(),
                null,
                user.getIsSeller()
            );
            return ResponseEntity.ok(userDTO);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Endpoint to update user's address and other information.
     *
     * @param userId The user ID.
     * @param updates A map containing the fields to update.
     * @return A success message or an error message.
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUserInfo(@PathVariable("id") Long userId, @RequestBody Map<String, Object> updates) {
        try {
            User user = userService.findById(userId).orElseThrow(() -> new Exception("User not found."));
            
            if (updates.containsKey("firstName")) {
                user.setFirstName((String) updates.get("firstName"));
            }
            if (updates.containsKey("lastName")) {
                user.setLastName((String) updates.get("lastName"));
            }
            if (updates.containsKey("streetAddress")) {
                user.setStreetAddress((String) updates.get("streetAddress"));
            }
            if (updates.containsKey("province")) {
                user.setProvince((String) updates.get("province"));
            }
            if (updates.containsKey("country")) {
                user.setCountry((String) updates.get("country"));
            }
            if (updates.containsKey("postalCode")) {
                user.setPostalCode((String) updates.get("postalCode"));
            }
            if (updates.containsKey("isSeller")) {
                user.setIsSeller((Boolean) updates.get("isSeller"));
            }

            User updatedUser = userService.saveUser(user);
            return ResponseEntity.ok("User information updated successfully.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @GetMapping("/all")
    public ResponseEntity<?> getAllUsers() {
        try {
            List<User> users = userService.findAll();
            List<UserDTO> userDTOs = users.stream().map(user -> new UserDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getStreetAddress(),
                user.getProvince(),
                user.getCountry(),
                user.getPostalCode(),
                user.getIsSeller()
            )).collect(Collectors.toList());
            return ResponseEntity.ok(userDTOs);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to fetch users");
        }
    }
    
  
}
