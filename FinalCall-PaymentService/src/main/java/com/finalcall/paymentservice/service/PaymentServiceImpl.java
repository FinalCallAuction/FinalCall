// src/main/java/com/finalcall/paymentservice/service/PaymentServiceImpl.java

package com.finalcall.paymentservice.service;

import com.finalcall.paymentservice.dto.PaymentRequest;
import com.finalcall.paymentservice.entity.Payment;
import com.finalcall.paymentservice.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Service implementation for handling payment processing.
 */
@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    /**
     * Processes the payment request by performing validation and saving the payment record.
     *
     * @param paymentRequest The payment details received from the frontend.
     * @return The processed Payment entity.
     * @throws Exception If any validation fails.
     */
    @Override
    public Payment processPayment(PaymentRequest paymentRequest) throws Exception {
        // Validate payment amount
        if (paymentRequest.getAmount() == null || paymentRequest.getAmount() <= 0) {
            throw new Exception("Payment amount must be greater than zero.");
        }

        // Validate currency
        if (paymentRequest.getCurrency() == null || paymentRequest.getCurrency().isEmpty()) {
            throw new Exception("Currency is required.");
        }

        // Validate userId
        if (paymentRequest.getUserId() == null || paymentRequest.getUserId() <= 0) {
            throw new Exception("Valid userId is required.");
        }

        // Validate itemId
        if (paymentRequest.getItemId() == null || paymentRequest.getItemId() <= 0) {
            throw new Exception("Valid itemId is required.");
        }

        // Validate paymentMethodId
        if (paymentRequest.getPaymentMethodId() == null || paymentRequest.getPaymentMethodId().isEmpty()) {
            throw new Exception("Payment method ID is required.");
        }

        // Deterministically set payment status to "COMPLETED"
        String status = "COMPLETED";

        // Create and save the payment record
        Payment payment = new Payment();
        payment.setUserId(paymentRequest.getUserId());
        payment.setItemId(paymentRequest.getItemId());
        payment.setAmount(paymentRequest.getAmount());
        payment.setCurrency(paymentRequest.getCurrency());
        payment.setPaymentMethodId(paymentRequest.getPaymentMethodId());
        payment.setStatus(status);
        payment.setTimestamp(LocalDateTime.now());

        return paymentRepository.save(payment);
    }
}
