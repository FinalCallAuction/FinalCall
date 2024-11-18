package com.finalcall.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.finalcall.backend.entity.PaymentRequest;
import com.stripe.Stripe;
import com.stripe.model.PaymentIntent;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Service
public class PaymentService {

    @Value("${stripe.api.key}")
    private String stripeApiKey;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeApiKey;
    }
    
    public boolean processPayment(PaymentRequest paymentRequest) {
        // Use Stripe API or mock logic to process the payment request
        try {
            // If using Stripe PaymentIntent creation for example
            Map<String, Object> params = new HashMap<>();
            params.put("amount", paymentRequest.getAmount());
            params.put("currency", paymentRequest.getCurrency());
            params.put("payment_method_types", new String[] { paymentRequest.getPaymentMethod() });

            PaymentIntent paymentIntent = PaymentIntent.create(params);
            return paymentIntent.getStatus().equals("succeeded");
        } catch (Exception e) {
            return false;
        }
    }	

    public String createPaymentIntent(Long amount, String currency) {
        try {
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
