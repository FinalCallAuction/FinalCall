// src/main/java/com/finalcall/authenticationservice/repository/UserRepository.java

package com.finalcall.authenticationservice.repository;

import com.finalcall.authenticationservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    Optional<User> findByUsername(String username);
}
