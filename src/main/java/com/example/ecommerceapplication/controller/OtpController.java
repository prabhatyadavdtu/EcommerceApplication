package com.example.ecommerceapplication.controller;

import com.example.ecommerceapplication.dto.OtpRequest;
import com.example.ecommerceapplication.dto.OtpResponse;
import com.example.ecommerceapplication.service.OtpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*") // Configure as needed for security
public class OtpController {

    @Autowired
    private OtpService otpService;

    @PostMapping("/send-otp")
    public ResponseEntity<OtpResponse> sendOtp(@RequestBody OtpRequest request) {
        return ResponseEntity.ok(otpService.generateAndSendOtp(request.getPhoneNumber()));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<OtpResponse> verifyOtp(@RequestBody OtpRequest request) {
        return ResponseEntity.ok(otpService.verifyOtp(request.getPhoneNumber(), request.getOtp()));
    }
}
