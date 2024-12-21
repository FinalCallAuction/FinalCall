package com.finalcall.paymentservice.dto;

import lombok.Data;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;

@Data
public class BidRequest {
    @NotNull(message = "Bid amount is required")
    @Min(value = 0, message = "Bid amount must be positive")
    private Double bidAmount;
    
    @NotNull(message = "Bidder ID is required")
    private Long bidderId;
}