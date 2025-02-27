package com.example.ecommerceapplication.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtConfig {
    @Value("${jwt.secret}")
    private String jwtSecret;
    
    @Value("${jwt.expirationMs}")
    private long jwtExpirationMs;
    
    
    // Getters for the properties
    public String getJwtSecret() {
        return jwtSecret;
    }
    
    public long getJwtExpirationMs() {
        return jwtExpirationMs;
    }
}