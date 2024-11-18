package com.finalcall.backend.controller;

import com.finalcall.backend.dto.PaymentRequest;
import com.finalcall.backend.service.PaymentService;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "http://localhost:3000")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/submit")
    public ResponseEntity<?> submitPayment(@RequestBody PaymentRequest paymentRequest) {
        try {
            // Validate inputs
            if (paymentRequest.getPaymentMethodId() == null || paymentRequest.getAmount() == null ||
                paymentRequest.getCurrency() == null || paymentRequest.getUserId() == null ||
                paymentRequest.getItemId() == null) {
                return ResponseEntity.badRequest().body("Missing payment details");
            }

            // Process payment
            PaymentIntent paymentIntent = paymentService.createPaymentIntent(paymentRequest);

            return ResponseEntity.ok(paymentIntent);
        } catch (StripeException e) {
            return ResponseEntity.status(500).body("Payment failed: " + e.getMessage());
        }
    }
}
