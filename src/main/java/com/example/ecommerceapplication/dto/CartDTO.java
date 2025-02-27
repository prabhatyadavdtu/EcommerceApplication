package com.example.ecommerceapplication.dto;

import com.example.ecommerceapplication.model.CartItem;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CartDTO {
    private Long userId;
    private List<CartItem> items;
    private BigDecimal totalAmount;
}
