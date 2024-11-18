// src/main/java/com/finalcall/backend/service/PaymentService.java

package com.finalcall.backend.service;

import com.finalcall.backend.dto.PaymentRequest;
import com.finalcall.backend.entity.Payment;
import com.finalcall.backend.repository.PaymentRepository;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class PaymentService {

    @Value("${stripe.api.key}")
    private String stripeApiKey;

    @Autowired
    private PaymentRepository paymentRepository;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeApiKey;
    }

    /**
     * Creates a PaymentIntent with Stripe and records the payment in the database.
     *
     * @param paymentRequest The payment details received from the frontend.
     * @return The created PaymentIntent object.
     * @throws StripeException If an error occurs while communicating with Stripe.
     */
    public PaymentIntent createPaymentIntent(PaymentRequest paymentRequest) throws StripeException {
        // Prepare payment parameters
        Map<String, Object> params = new HashMap<>();
        params.put("amount", (int) (paymentRequest.getAmount() * 100)); // Convert to cents
        params.put("currency", paymentRequest.getCurrency());
        params.put("payment_method", paymentRequest.getPaymentMethodId());
        params.put("confirmation_method", "manual");
        params.put("confirm", true);

        // Include metadata
        Map<String, String> metadata = new HashMap<>();
        metadata.put("userId", String.valueOf(paymentRequest.getUserId()));
        metadata.put("itemId", String.valueOf(paymentRequest.getItemId()));
        params.put("metadata", metadata);

        // Create PaymentIntent with Stripe
        PaymentIntent paymentIntent = PaymentIntent.create(params);

        // Save payment details to the database
        Payment payment = new Payment();
        payment.setUserId(paymentRequest.getUserId());
        payment.setItemId(paymentRequest.getItemId());
        payment.setAmount(paymentRequest.getAmount());
        payment.setCurrency(paymentRequest.getCurrency());
        payment.setPaymentMethodId(paymentRequest.getPaymentMethodId());
        payment.setStatus(paymentIntent.getStatus());
        payment.setTimestamp(LocalDateTime.now());

        paymentRepository.save(payment);

        return paymentIntent;
    }
}
