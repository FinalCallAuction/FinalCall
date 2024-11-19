// src/main/java/com/finalcall/paymentservice/repository/PaymentRepository.java

package com.finalcall.paymentservice.repository;

import com.finalcall.paymentservice.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    // Additional query methods can be defined here if needed
}
