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

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeApiKey;
    }

    public boolean processPayment(PaymentRequest paymentRequest) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("amount", paymentRequest.getAmount());
            params.put("currency", paymentRequest.getCurrency());
            params.put("payment_method_types", new String[]{"card"});

            PaymentIntent paymentIntent = PaymentIntent.create(params);
            return "succeeded".equals(paymentIntent.getStatus());
        } catch (Exception e) {
            return false;
        }
    }
}
