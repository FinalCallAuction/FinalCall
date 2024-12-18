package com.finalcall.paymentservice.service.card;

import com.finalcall.paymentservice.dto.PaymentRequest;
import org.springframework.stereotype.Component;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

@Component
public class CreditCardValidator {

    public boolean validateCard(PaymentRequest request) {
        return validateNumber(request.getCardNumber()) &&
               validateExpiryDate(request.getExpiryDate()) &&
               validateCVV(request.getCvv());
    }

    private boolean validateNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() != 16) {
            return false;
        }
        
        // Luhn algorithm implementation
        int sum = 0;
        boolean alternate = false;
        
        for (int i = cardNumber.length() - 1; i >= 0; i--) {
            int n = Integer.parseInt(cardNumber.substring(i, i + 1));
            if (alternate) {
                n *= 2;
                if (n > 9) {
                    n = (n % 10) + 1;
                }
            }
            sum += n;
            alternate = !alternate;
        }
        return (sum % 10 == 0);
    }

    private boolean validateExpiryDate(String expiryDate) {
        try {
            YearMonth expiry = YearMonth.parse(expiryDate, DateTimeFormatter.ofPattern("MM/yy"));
            return !expiry.isBefore(YearMonth.now());
        } catch (Exception e) {
            return false;
        }
    }

    private boolean validateCVV(String cvv) {
        return cvv != null && cvv.matches("[0-9]{3,4}");
    }
}