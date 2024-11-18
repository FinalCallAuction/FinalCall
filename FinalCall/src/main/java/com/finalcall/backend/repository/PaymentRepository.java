// src/main/java/com/finalcall/backend/repository/PaymentRepository.java

package com.finalcall.backend.repository;

import com.finalcall.backend.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    // Additional query methods can be defined here if needed
}
