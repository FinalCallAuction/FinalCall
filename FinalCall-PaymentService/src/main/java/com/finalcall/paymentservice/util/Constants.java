package com.finalcall.paymentservice.util;

public class Constants {
    public static final String PAYMENT_SUCCESS = "SUCCESS";
    public static final String PAYMENT_FAILED = "FAILED";
    public static final String PAYMENT_PROCESSING = "PROCESSING";
    
    public static final int CARD_NUMBER_LENGTH = 16;
    public static final int CVV_MIN_LENGTH = 3;
    public static final int CVV_MAX_LENGTH = 4;
    
    private Constants() {
        // Private constructor to prevent instantiation
    }
}