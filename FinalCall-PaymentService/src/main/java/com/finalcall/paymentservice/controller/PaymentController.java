package com.finalcall.paymentservice.controller;

import com.finalcall.paymentservice.dto.PaymentRequest;
import com.finalcall.paymentservice.dto.PaymentResponse;
import com.finalcall.paymentservice.entity.Payment;
import com.finalcall.paymentservice.exception.PaymentNotFoundException;
import com.finalcall.paymentservice.repository.PaymentRepository;
import com.finalcall.paymentservice.service.PaymentService;
import com.finalcall.paymentservice.dto.AuctionDTO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
//@CrossOrigin(
//    origins = "${app.allowedOrigins}", 
//    allowedHeaders = "*",
//    methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, 
//               RequestMethod.DELETE, RequestMethod.OPTIONS}
//)
public class PaymentController {
    @Autowired
    private PaymentService paymentService;
    
    @Autowired
    private PaymentRepository paymentRepository;  
    
    
    @GetMapping("/transaction/{transactionId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PaymentResponse> getTransactionDetails(@PathVariable String transactionId) {
        Payment payment = paymentRepository.findByTransactionId(transactionId)
            .orElseThrow(() -> new PaymentNotFoundException("Payment not found"));
        return ResponseEntity.ok(paymentService.mapToPaymentResponse(payment));  // Use service's mapping method
    }
    
    @GetMapping("/auction/{auctionId}/payment-page")
   // @PreAuthorize("hasAuthority('SCOPE_read')")
    public String showPaymentPage(@PathVariable Long auctionId, Model model) {
        var auctionDetails = paymentService.getAuctionDetails(auctionId);
        model.addAttribute("auctionDetails", auctionDetails);
        model.addAttribute("currency", "USD");
        return "paymentHome";
    }

    @PostMapping("/process-auction-payment/{auctionId}")
   // @PreAuthorize("hasAuthority('SCOPE_write')")
    public ResponseEntity<PaymentResponse> processAuctionPayment(
            @PathVariable Long auctionId,
            @Valid @RequestBody PaymentRequest paymentRequest) {
        PaymentResponse response = paymentService.processAuctionPayment(auctionId, paymentRequest);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/auction/{auctionId}")
   // @PreAuthorize("hasAuthority('SCOPE_read')")
    public ResponseEntity<PaymentResponse> getAuctionPayment(@PathVariable Long auctionId) {
        PaymentResponse response = paymentService.getPaymentByAuctionId(auctionId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAuthority('SCOPE_read')")
    public ResponseEntity<?> getUserPayments(@PathVariable Long userId) {
        var payments = paymentService.getPaymentsByUserId(userId);
        return ResponseEntity.ok(payments);
    }
}
