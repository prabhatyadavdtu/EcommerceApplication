package com.example.ecommerceapplication.service;

import com.example.ecommerceapplication.dto.OtpResponse;
import com.example.ecommerceapplication.model.OtpEntity;
import com.example.ecommerceapplication.repository.OtpRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Random;

@Service
public class OtpService {

    @Autowired
    private OtpRepository otpRepository;
    
    @Autowired
    private SmsService smsService;

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration}")
    private long jwtExpiration;

    public OtpResponse generateAndSendOtp(String phoneNumber) {
        // Validate phone number
        if (!isValidPhoneNumber(phoneNumber)) {
            return new OtpResponse("Invalid phone number", false);
        }
        
        // Generate 6-digit OTP
        String otp = generateOtp();
        
        // Save OTP to database with expiration time (5 minutes)
        OtpEntity otpEntity = new OtpEntity();
        otpEntity.setPhoneNumber(phoneNumber);
        otpEntity.setOtp(otp);
        otpEntity.setExpiryTime(LocalDateTime.now().plusMinutes(5));
        otpRepository.save(otpEntity);
        
        // Send OTP via SMS
        boolean smsSent = smsService.sendSms(phoneNumber, "Your OTP is: " + otp + ". Valid for 5 minutes.");
        
        if (!smsSent) {
            return new OtpResponse("Failed to send OTP", false);
        }
        
        return new OtpResponse("OTP sent successfully", true);
    }

    public OtpResponse verifyOtp(String phoneNumber, String otp) {
        // Find OTP in database
        OtpEntity otpEntity = otpRepository.findByPhoneNumber(phoneNumber);
        
        // Check if OTP exists and is valid
        if (otpEntity == null || !otpEntity.getOtp().equals(otp) || 
            otpEntity.getExpiryTime().isBefore(LocalDateTime.now())) {
            return new OtpResponse("Invalid or expired OTP", false);
        }
        
        // Delete OTP after successful verification
        otpRepository.delete(otpEntity);
        
        // Generate JWT token
        String token = generateJwtToken(phoneNumber);
        
        return new OtpResponse("OTP verified successfully", token, true);
    }
    
    // Helper methods
    private String generateOtp() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }
    
    private boolean isValidPhoneNumber(String phoneNumber) {
        // Implement your phone number validation logic
        return phoneNumber != null && phoneNumber.matches("^\\d{10}$");
    }
    
    private String generateJwtToken(String phoneNumber) {
        Key signingKey = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        return Jwts.builder()
                .setSubject(phoneNumber)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(signingKey)
                .compact();
    }

    // public String generateJwtToken(User user, long expiration) {
    //     Date now = new Date();
    //     //Date expiryDate = new Date(now.getTime() + jwtExpirationMs);
    //     Date expiryDate = new Date(now.getTime() + expiration);

    //     // Create signing key from secret bytes
    //     Key signingKey = Keys.hmacShaKeyFor(jwtSecret.getBytes());

    //     return Jwts.builder()
    //             .setSubject(user.getUsername())
    //             .claim("id", user.getId())
    //             .claim("role", user.getRole().name())
    //             .setIssuedAt(now)
    //             .setExpiration(expiryDate)
    //             .signWith(signingKey)
    //             .compact();
    // }
}
