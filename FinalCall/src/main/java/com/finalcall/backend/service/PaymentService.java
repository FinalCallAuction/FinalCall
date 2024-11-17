package com.finalcall.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.stripe.Stripe;
import com.stripe.model.PaymentIntent;

import java.util.HashMap;
import java.util.Map;

@Service
public class PaymentService {

    @Value("${stripe.api.key}")
    private String stripeApiKey;

    public PaymentService() {
        // Initialize Stripe with the API key
        Stripe.apiKey = stripeApiKey;
    }

    public String createPaymentIntent(Long amount, String currency) {
        try {
            // Create a payment intent using Stripe
            Map<String, Object> params = new HashMap<>();
            params.put("amount", amount);
            params.put("currency", currency);
            params.put("payment_method_types", new String[]{"card"});

            PaymentIntent paymentIntent = PaymentIntent.create(params);
            return paymentIntent.getClientSecret();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create Payment Intent", e);
        }
    }
}
