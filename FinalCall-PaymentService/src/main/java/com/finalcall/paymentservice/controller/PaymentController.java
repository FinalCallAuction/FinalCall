// src/main/java/com/finalcall/paymentservice/controller/PaymentController.java

package com.finalcall.paymentservice.controller;

import com.finalcall.paymentservice.dto.PaymentRequest;
import com.finalcall.paymentservice.entity.Payment;
import com.finalcall.paymentservice.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for handling payment-related operations.
 */
@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "http://localhost:3000") // Adjust as needed
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    /**
     * Endpoint to submit a payment.
     *
     * @param paymentRequest The payment details.
     * @return The processed payment details.
     */
    @PostMapping("/submit")
    public ResponseEntity<?> submitPayment(@RequestBody PaymentRequest paymentRequest) {
        try {
            // Validate inputs
            if (paymentRequest.getPaymentMethodId() == null || paymentRequest.getPaymentMethodId().isEmpty()
                || paymentRequest.getAmount() == null || paymentRequest.getCurrency() == null
                || paymentRequest.getCurrency().isEmpty()
                || paymentRequest.getUserId() == null || paymentRequest.getItemId() == null) {
                return ResponseEntity.badRequest().body("Missing payment details");
            }

            // Process payment
            Payment payment = paymentService.processPayment(paymentRequest);

            // Optionally, notify AuctionService about the payment
            // For a mock setup, this can be simulated or handled via frontend

            return ResponseEntity.ok(payment);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Payment failed: " + e.getMessage());
        }
    }
}
