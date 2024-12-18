package com.finalcall.paymentservice.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class PaymentRequest {
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private Double amount;

    @NotNull(message = "Currency is required")
    @Pattern(regexp = "^(USD|EUR|GBP)$", message = "Invalid currency")
    private String currency;

    @NotNull(message = "Card number is required")
    @Pattern(regexp = "^[0-9]{16}$", message = "Invalid card number")
    private String cardNumber;

    @NotNull(message = "Expiry date is required")
    @Pattern(regexp = "^(0[1-9]|1[0-2])\\/([0-9]{2})$", message = "Invalid expiry date")
    private String expiryDate;

    @NotNull(message = "CVV is required")
    @Pattern(regexp = "^[0-9]{3,4}$", message = "Invalid CVV")
    private String cvv;

    @NotBlank(message = "Card holder name is required")
    private String cardHolderName;

    // Default constructor
    public PaymentRequest() {
    }

    // Constructor with all fields
    public PaymentRequest(Double amount, String currency, String cardNumber, 
                         String expiryDate, String cvv, String cardHolderName) {
        this.amount = amount;
        this.currency = currency;
        this.cardNumber = cardNumber;
        this.expiryDate = expiryDate;
        this.cvv = cvv;
        this.cardHolderName = cardHolderName;
    }

    // Getters
    public Double getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public String getCvv() {
        return cvv;
    }

    public String getCardHolderName() {
        return cardHolderName;
    }

    // Setters
    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public void setCvv(String cvv) {
        this.cvv = cvv;
    }

    public void setCardHolderName(String cardHolderName) {
        this.cardHolderName = cardHolderName;
    }

    // Utility methods
    public String getLastFourDigits() {
        if (cardNumber != null && cardNumber.length() >= 4) {
            return cardNumber.substring(cardNumber.length() - 4);
        }
        return null;
    }

    // toString method (excluding sensitive data)
    @Override
    public String toString() {
        return "PaymentRequest{" +
               "amount=" + amount +
               ", currency='" + currency + '\'' +
               ", cardNumber='****" + getLastFourDigits() + '\'' +
               ", expiryDate='**/**'" +
               ", cardHolderName='" + cardHolderName + '\'' +
               '}';
    }
}