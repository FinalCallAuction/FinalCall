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

    // Handle displaying the payment home page
    @GetMapping("/")
    public String showPaymentHome() {
        return "paymentHome";
    }

    // Handle payment processing form submission
    @PostMapping("/processPayment")
    public String processPayment(
            @RequestParam("amount") String amount,
            @RequestParam("currency") String currency,
            Model model) {
        
        // Create PaymentRequest object and process the payment
        PaymentRequest paymentRequest = new PaymentRequest(amount, currency);
        boolean isSuccess = paymentService.processPayment(paymentRequest);
        
        // Add payment details to the model to display on confirmation page
        model.addAttribute("amount", amount);
        model.addAttribute("currency", currency);
        
        if (isSuccess) {
            return "paymentConfirmation";
        } else {
            model.addAttribute("error", "There was an issue processing your payment. Please try again.");
            return "paymentHome";
        }
    }
}
