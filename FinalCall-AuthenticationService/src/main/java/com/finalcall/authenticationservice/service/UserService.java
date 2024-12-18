package com.finalcall.authenticationservice.service;

import com.finalcall.authenticationservice.dto.UserDTO;
import com.finalcall.authenticationservice.entity.User;
import com.finalcall.authenticationservice.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

	 @Autowired
	    private UserRepository userRepository;

	    @Autowired
	    private PasswordEncoder passwordEncoder;

	    /**
	     * Registers a new user after validating username and email uniqueness.
	     *
	     * @param user The user to register.
	     * @return The registered user.
	     * @throws Exception If username or email already exists.
	     */
	    public User registerUser(User user) throws Exception {
	        if (userRepository.existsByUsername(user.getUsername())) {
	            throw new Exception("Username is already taken.");
	        }
	        if (userRepository.existsByEmail(user.getEmail())) {
	            throw new Exception("Email is already in use.");
	        }
	        // Encode the password before saving
	        user.setPassword(passwordEncoder.encode(user.getPassword()));
	        return userRepository.save(user);
	    }

	    /**
	     * Finds a user by their username.
	     *
	     * @param username The username to search for.
	     * @return An Optional containing the user if found.
	     */
	    public Optional<User> findByUsername(String username) {
	        return userRepository.findByUsername(username);
	    }

	    /**
	     * Finds a user by their ID.
	     *
	     * @param id The user ID.
	     * @return An Optional containing the user if found.
	     */
	    public Optional<User> findById(Long id) {
	        return userRepository.findById(id);
	    }
	    
	    public UserDTO getUserById(Long userId) throws Exception {
	        User user = userRepository.findById(userId)
	            .orElseThrow(() -> new Exception("User not found."));

	        // Convert User to UserDTO
	        UserDTO userDTO = new UserDTO(
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
	        );
	        return userDTO;
	    }


	    /**
	     * Authenticates a user with the provided username and password.
	     *
	     * @param username The username.
	     * @param password The password.
	     * @return An Optional containing the authenticated user if credentials match.
	     */
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


    /**
     * Updates the email of a user.
     *
     * @param userId   The user ID.
     * @param newEmail The new email.
     * @return The updated user.
     * @throws Exception If the user is not found or the email is already in use.
     */
    public User updateUserEmail(Long userId, String newEmail) throws Exception {
        User user = userRepository.findById(userId)
                    .orElseThrow(() -> new Exception("User not found."));
        if (userRepository.existsByEmail(newEmail)) {
            throw new Exception("Email is already in use.");
        }
        user.setEmail(newEmail);
        return userRepository.save(user);
    }

    /**
     * Updates the password of a user.
     *
     * @param userId      The user ID.
     * @param newPassword The new password.
     * @return The updated user.
     * @throws Exception If the user is not found.
     */
    public User updateUserPassword(Long userId, String newPassword) throws Exception {
        User user = userRepository.findById(userId)
                    .orElseThrow(() -> new Exception("User not found."));
        user.setPassword(passwordEncoder.encode(newPassword));
        return userRepository.save(user);
    }

    /**
     * Updates the address of a user.
     *
     * @param userId        The user ID.
     * @param streetAddress The new street address.
     * @param province      The new province/state.
     * @param country       The new country.
     * @param postalCode    The new postal code.
     * @return The updated user.
     * @throws Exception If the user is not found.
     */
    public User updateUserAddress(Long userId, String streetAddress, String province, String country, String postalCode) throws Exception {
        User user = userRepository.findById(userId)
                    .orElseThrow(() -> new Exception("User not found."));
        user.setStreetAddress(streetAddress);
        user.setProvince(province);
        user.setCountry(country);
        user.setPostalCode(postalCode);
        return userRepository.save(user);
    }

    /**
     * Saves the user entity.
     *
     * @param user The user to save.
     * @return The saved user.
     */
    public User saveUser(User user) {
        return userRepository.save(user);
    }
    
    public List<User> findAll() {
        return userRepository.findAll();
    }

}
