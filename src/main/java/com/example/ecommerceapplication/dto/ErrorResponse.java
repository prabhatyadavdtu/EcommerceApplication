package com.example.ecommerceapplication.dto;

import java.util.HashMap;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    private int status;
    private String message;
    private Map<String, String> errors;
    
    public ErrorResponse(int status, String message) {
        this.status = status;
        this.message = message;
        this.errors = new HashMap<>();
    }
}