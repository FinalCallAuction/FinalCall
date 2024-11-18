package com.finalcall.backend.entity;

public class PaymentRequest {

    private Long amount;
    private String currency;
    private String paymentMethod;

    // Default constructor
    public PaymentRequest() {
    }

    // Constructor to initialize all fields
    public PaymentRequest(Long amount, String currency, String paymentMethod) {
        this.amount = amount;
        this.currency = currency;
        this.paymentMethod = paymentMethod;
    }

    // Getters and Setters
    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}
