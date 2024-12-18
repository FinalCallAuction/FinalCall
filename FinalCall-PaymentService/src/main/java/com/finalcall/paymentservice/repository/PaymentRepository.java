package com.finalcall.paymentservice.repository;

import com.finalcall.paymentservice.entity.Payment;
import com.finalcall.paymentservice.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByTransactionId(String transactionId);
    Optional<Payment> findByAuctionId(Long auctionId);
    List<Payment> findByBuyerId(Long buyerId);
    List<Payment> findBySellerId(Long sellerId);
    List<Payment> findByStatus(PaymentStatus status);
    List<Payment> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    
    @Query("SELECT p FROM Payment p WHERE p.buyerId = ?1 AND p.status = ?2")
    List<Payment> findByBuyerIdAndStatus(Long buyerId, PaymentStatus status);
    
    @Query("SELECT p FROM Payment p WHERE p.sellerId = ?1 AND p.status = ?2")
    List<Payment> findBySellerIdAndStatus(Long sellerId, PaymentStatus status);
    
    @Query("SELECT COUNT(p) FROM Payment p WHERE p.status = ?1")
    long countByStatus(PaymentStatus status);
    
    List<Payment> findByBuyerIdAndCreatedAtBetween(Long buyerId, LocalDateTime start, LocalDateTime end);
    List<Payment> findBySellerIdAndCreatedAtBetween(Long sellerId, LocalDateTime start, LocalDateTime end);
    List<Payment> findByStatusAndCreatedAtBetween(PaymentStatus status, LocalDateTime start, LocalDateTime end);
    
}
