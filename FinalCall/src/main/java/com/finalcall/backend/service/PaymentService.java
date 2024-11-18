package com.finalcall.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.stripe.Stripe;
import com.stripe.model.PaymentIntent;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Service
public class PaymentService {

    @Value("${stripe.api.key}")
    private String stripeApiKey;

    // Use @PostConstruct to properly initialize the Stripe API key after the bean is created
    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeApiKey;
    }

    public String createPaymentIntent(Long amount, String currency) {
        try {
            // Input validation
            if (amount == null || amount <= 0) {
                throw new IllegalArgumentException("Amount must be greater than zero.");
            }

            if (currency == null || currency.isEmpty()) {
                throw new IllegalArgumentException("Currency must be provided.");
            }

            // Create a payment intent using Stripe
            Map<String, Object> params = new HashMap<>();
            params.put("amount", amount);
            params.put("currency", currency);
            params.put("payment_method_types", new String[]{"card"});

            PaymentIntent paymentIntent = PaymentIntent.create(params);
            return paymentIntent.getClientSecret();
        } catch (IllegalArgumentException e) {
            // Handle validation errors
            throw new PaymentProcessingException("Validation failed: " + e.getMessage(), e);
        } catch (Exception e) {
            // Handle Stripe exceptions or other runtime exceptions
            throw new PaymentProcessingException("Failed to create Payment Intent", e);
        }
    }
}
