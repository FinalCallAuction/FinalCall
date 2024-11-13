// src/main/java/com/finalcall/backend/service/UserService.java

package com.finalcall.backend.service;

import com.finalcall.backend.entity.User;
import com.finalcall.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Registers a new user after validating the uniqueness of username and email.
     *
     * @param user The user object containing registration details.
     * @return The saved user object.
     * @throws Exception if the username or email already exists.
     */
    public User registerUser(User user) throws Exception {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new Exception("Username is already taken!");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new Exception("Email is already in use!");
        }
        // Encrypt the user's password before saving
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    /**
     * Authenticates a user based on username and password.
     *
     * @param username The username.
     * @param password The raw password.
     * @return An Optional containing the user if authentication is successful.
     */
    public Optional<User> authenticate(String username, String password) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            // Check if the raw password matches the encoded password
            if (passwordEncoder.matches(password, user.getPassword())) {
                return Optional.of(user);
            }
        }
        return Optional.empty();
    }

    /**
     * Retrieves user details by username.
     *
     * @param username The username.
     * @return An Optional containing the user if found.
     */
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * Retrieves user details by ID.
     *
     * @param id The user ID.
     * @return An Optional containing the user if found.
     */
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    // Additional user-related methods can be added here, such as updating user profiles

    /**
     * Updates the user's email.
     *
     * @param id    The user ID.
     * @param email The new email address.
     * @return The updated user object.
     * @throws Exception if the new email is already in use.
     */
    public User updateUserEmail(Long id, String email) throws Exception {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isPresent()) {
            if (userRepository.existsByEmail(email)) {
                throw new Exception("Email is already in use!");
            }
            User user = userOpt.get();
            user.setEmail(email);
            return userRepository.save(user);
        } else {
            throw new Exception("User not found!");
        }
    }

    /**
     * Updates the user's password.
     *
     * @param id          The user ID.
     * @param newPassword The new raw password.
     * @return The updated user object.
     */
    public User updateUserPassword(Long id, String newPassword) throws Exception {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setPassword(passwordEncoder.encode(newPassword));
            return userRepository.save(user);
        } else {
            throw new Exception("User not found!");
        }
    }

    // Implement additional methods as needed (e.g., update username, shipping info, etc.)
}
