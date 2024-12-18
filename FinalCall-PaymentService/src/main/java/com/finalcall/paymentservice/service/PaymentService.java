package com.finalcall.paymentservice.service;

import com.finalcall.paymentservice.client.AuctionServiceClient;
import com.finalcall.paymentservice.dto.PaymentRequest;
import com.finalcall.paymentservice.dto.PaymentResponse;
import com.finalcall.paymentservice.entity.Payment;
import com.finalcall.paymentservice.entity.PaymentStatus;
import com.finalcall.paymentservice.exception.PaymentProcessingException;
import com.finalcall.paymentservice.repository.PaymentRepository;
import com.finalcall.paymentservice.service.card.CreditCardValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PaymentService {
    @Autowired
    private PaymentRepository paymentRepository;
    
    @Autowired
    private CreditCardValidator creditCardValidator;
    
    @Autowired
    private AuctionServiceClient auctionServiceClient;

    public com.finalcall.auctionservice.dto.AuctionDTO getAuctionDetails(Long auctionId) {
        return auctionServiceClient.getAuctionById(auctionId);
    }

    @Transactional
    public PaymentResponse processAuctionPayment(Long auctionId, PaymentRequest paymentRequest) {
        // Validate the auction exists and is ended
        var auctionDTO = auctionServiceClient.getAuctionById(auctionId);
        if (auctionDTO == null || !"ENDED".equals(auctionDTO.getStatus())) {
            throw new PaymentProcessingException("Invalid auction state for payment");
        }

        // Validate payment amount matches auction final price
        if (!paymentRequest.getAmount().equals(auctionDTO.getCurrentBidPrice())) {
            throw new PaymentProcessingException("Payment amount does not match auction price");
        }

        // Validate card details
        if (!creditCardValidator.validateCard(paymentRequest)) {
            throw new PaymentProcessingException("Invalid card details");
        }

        // Create payment record
        Payment payment = new Payment();
        payment.setAmount(paymentRequest.getAmount());
        payment.setCurrency(paymentRequest.getCurrency());
        payment.setPaymentMethod("Credit Card");
        payment.setAuctionId(auctionId);
        payment.setBuyerId(auctionDTO.getCurrentBidderId());
        payment.setSellerId(auctionDTO.getSellerId());
        payment.setStatus(PaymentStatus.PROCESSING);
        payment.setTransactionId(UUID.randomUUID().toString());
        payment.setLastFourDigits(paymentRequest.getCardNumber().substring(12));
        payment.setCreatedAt(LocalDateTime.now());
        payment.setUpdatedAt(LocalDateTime.now());

        // Process payment
        boolean isSuccess = processPayment(payment);
        payment.setStatus(isSuccess ? PaymentStatus.SUCCESS : PaymentStatus.FAILED);
        
        Payment savedPayment = paymentRepository.save(payment);
        
        return mapToPaymentResponse(savedPayment);
    }

    private boolean processPayment(Payment payment) {
        // Mock payment processing
        try {
            Thread.sleep(1000); // Simulate processing time
            // Mock success for amounts under 10000
            return payment.getAmount() < 10000;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    public PaymentResponse getPaymentByAuctionId(Long auctionId) {
        Payment payment = paymentRepository.findByAuctionId(auctionId)
            .orElseThrow(() -> new PaymentProcessingException("No payment found for auction"));
        return mapToPaymentResponse(payment);
    }

    public List<PaymentResponse> getPaymentsByUserId(Long userId) {
        List<Payment> payments = paymentRepository.findByBuyerId(userId);
        return payments.stream()
            .map(this::mapToPaymentResponse)
            .collect(Collectors.toList());
    }

    private PaymentResponse mapToPaymentResponse(Payment payment) {
        PaymentResponse response = new PaymentResponse();
        response.setTransactionId(payment.getTransactionId());
        response.setAmount(payment.getAmount());
        response.setCurrency(payment.getCurrency());
        response.setStatus(payment.getStatus().name());
        response.setLastFourDigits(payment.getLastFourDigits());
        response.setPaymentMethod(payment.getPaymentMethod());
        response.setTimestamp(payment.getCreatedAt());
        response.setAuctionId(payment.getAuctionId());
        response.setBuyerId(payment.getBuyerId());
        response.setSellerId(payment.getSellerId());
        return response;
    }

}