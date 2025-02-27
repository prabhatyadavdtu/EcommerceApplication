package com.example.ecommerceapplication.dto;

import com.example.ecommerceapplication.model.PaymentDetails;

public class OrderDTO {
    private Long userId;
    private PaymentDetails paymentDetails;
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public PaymentDetails getPaymentDetails() {
        return paymentDetails;
    }

    public void setPaymentDetails(PaymentDetails paymentDetails) {
        this.paymentDetails = paymentDetails;
    }
}
