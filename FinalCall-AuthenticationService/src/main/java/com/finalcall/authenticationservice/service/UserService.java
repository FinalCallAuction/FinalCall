// src/main/java/com/finalcall/authenticationservice/service/UserService.java

package com.finalcall.authenticationservice.service;

import com.finalcall.authenticationservice.entity.User;
import com.finalcall.authenticationservice.repository.UserRepository;
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

    public User registerUser(User user) throws Exception {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new Exception("Username is already taken!");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new Exception("Email is already in use!");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public Optional<User> authenticate(String username, String password) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (passwordEncoder.matches(password, user.getPassword())) {
                return Optional.of(user);
            }
        }
        return Optional.empty();
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
}
