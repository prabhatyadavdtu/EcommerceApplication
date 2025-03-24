package com.example.ecommerceapplication.service;

public interface SmsService {
    boolean sendSms(String phoneNumber, String message);
}
