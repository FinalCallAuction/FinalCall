// src/main/java/com/finalcall/paymentservice/dto/PaymentRequest.java

package com.finalcall.paymentservice.dto;

public class PaymentRequest {

    private Double amount;
    private String currency;
    private String paymentMethodId;
    private Long userId;
    private Long itemId;

    // Constructors
    public PaymentRequest() {}

    public PaymentRequest(Double amount, String currency, String paymentMethodId, Long userId, Long itemId) {
        this.amount = amount;
        this.currency = currency;
        this.paymentMethodId = paymentMethodId;
        this.userId = userId;
        this.itemId = itemId;
    }

    // Getters and Setters

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getPaymentMethodId() {
        return paymentMethodId;
    }

    public void setPaymentMethodId(String paymentMethodId) {
        this.paymentMethodId = paymentMethodId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    // Additional Utility Methods (if required)

    @Override
    public String toString() {
        return "PaymentRequest{" +
                "amount=" + amount +
                ", currency='" + currency + '\'' +
                ", paymentMethodId='" + paymentMethodId + '\'' +
                ", userId=" + userId +
                ", itemId=" + itemId +
                '}';
    }
}
