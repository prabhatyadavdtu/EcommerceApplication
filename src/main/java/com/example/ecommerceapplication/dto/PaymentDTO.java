package com.example.ecommerceapplication.dto;

import lombok.Data;

@Data
public class PaymentDTO {
    private String cardNumber;
    private String expiryDate;
    private String cvv;
}
