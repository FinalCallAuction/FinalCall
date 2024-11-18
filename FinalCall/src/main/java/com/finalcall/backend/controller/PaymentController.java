package com.finalcall.backend.controller;

import com.finalcall.backend.entity.PaymentRequest;
import com.finalcall.backend.service.PaymentService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @GetMapping("/paymentHome")
    public String paymentHome() {
        return "paymentHome"; // This will map to paymentHome.jsp
    }

    @PostMapping("/processPayment")
    public String processPayment(
            @RequestParam("amount") Long amount,
            @RequestParam("currency") String currency,
            @RequestParam("paymentMethod") String paymentMethod,
            Model model) {
        
        // Create PaymentRequest object and process the payment
        PaymentRequest paymentRequest = new PaymentRequest(amount, currency, paymentMethod);
        boolean isSuccess = paymentService.processPayment(paymentRequest);

        // Add payment details to the model to display on confirmation page
        model.addAttribute("amount", amount);
        model.addAttribute("currency", currency);
        model.addAttribute("paymentMethod", paymentMethod);

        if (isSuccess) {
            return "paymentConfirmation";
        } else {
            model.addAttribute("error", "Payment failed. Please try again.");
            return "paymentHome";
        }
    }
}
