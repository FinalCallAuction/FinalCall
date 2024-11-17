package com.finalcall.backend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/payment")
public class PaymentController {

    @GetMapping("/process")
    public String processPayment() {
        // Add logic to process the payment
        return "payment";
    }

    @GetMapping("/home")
    public String homePage() {
        // This could be the landing page for payments or summary view
        return "home";
    }
}
