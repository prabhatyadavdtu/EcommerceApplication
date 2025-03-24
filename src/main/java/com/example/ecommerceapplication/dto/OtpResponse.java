package com.example.ecommerceapplication.dto;

public class OtpResponse {
    private String message;
    private String token;
    private boolean success;

    // Constructors
    public OtpResponse() {}

    public OtpResponse(String message, boolean success) {
        this.message = message;
        this.success = success;
    }

    public OtpResponse(String message, String token, boolean success) {
        this.message = message;
        this.token = token;
        this.success = success;
    }

    // Getters and Setters
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
