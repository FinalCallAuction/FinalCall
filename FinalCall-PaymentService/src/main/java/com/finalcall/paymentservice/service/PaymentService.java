// src/main/java/com/finalcall/paymentservice/service/PaymentService.java

package com.finalcall.paymentservice.service;

import com.finalcall.paymentservice.dto.PaymentRequest;
import com.finalcall.paymentservice.entity.Payment;

public interface PaymentService {
    Payment processPayment(PaymentRequest paymentRequest) throws Exception;
}
