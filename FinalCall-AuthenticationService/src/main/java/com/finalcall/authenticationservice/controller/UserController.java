package com.finalcall.authenticationservice.controller;

import com.finalcall.authenticationservice.entity.User;
import com.finalcall.authenticationservice.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "http://localhost:3000") // Adjust as per frontend port
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
//    @PutMapping("/{id}/email")
//    public ResponseEntity<?> updateEmail(@PathVariable("id") Long userId, @RequestBody Map<String, String> updates) {
//        String newEmail = updates.get("email");
//        try {
//            User updatedUser = userService.updateUserEmail(userId, newEmail);
//            return ResponseEntity.ok("Email updated successfully");
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().body(e.getMessage());
//        }
//    }

    /**
     * Endpoint to update the user's password.
     *
     * @param userId The user ID.
     * @param updates A map containing the new password.
     * @return A success message or an error message.
     */
//    @PutMapping("/{id}/password")
//    public ResponseEntity<?> updatePassword(@PathVariable("id") Long userId, @RequestBody Map<String, String> updates) {
//        String newPassword = updates.get("password");
//        try {
//            User updatedUser = userService.updateUserPassword(userId, newPassword);
//            return ResponseEntity.ok("Password updated successfully");
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().body(e.getMessage());
//        }
//    }

    // Additional endpoints for other user-related operations can be added here
}
