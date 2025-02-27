package com.example.ecommerceapplication.service;

import com.example.ecommerceapplication.exception.PaymentException;
import com.example.ecommerceapplication.model.Order;
import com.example.ecommerceapplication.model.OrderStatus;
import com.example.ecommerceapplication.model.PaymentDetails;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class PaymentService {
    public void processPayment(Order order, PaymentDetails paymentDetails) {
        if (paymentDetails.getCardNumber() == null || paymentDetails.getCvv() == null) {
            throw new PaymentException("Invalid payment details");
        }
        order.setStatus(OrderStatus.COMPLETED);
    }
}
