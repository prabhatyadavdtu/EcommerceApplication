package com.example.ecommerceapplication.service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

@Service
public class TokenBlacklistService {
    // Using a ConcurrentHashMap for thread safety
    private final Set<String> blacklistedTokens = ConcurrentHashMap.newKeySet();
    
    /**
     * Add a token to the blacklist
     */
    public void blacklistToken(String token) {
        blacklistedTokens.add(token);
    }
    
    /**
     * Check if a token is blacklisted
     */
    public boolean isTokenBlacklisted(String token) {
        return blacklistedTokens.contains(token);
    }
    
    /**
     * Remove expired tokens from the blacklist (can be scheduled)
     * This would need to be called periodically to clean up the blacklist
     */
    public void cleanupExpiredTokens(JwtService jwtService) {
        blacklistedTokens.removeIf(token -> {
            try {
                return jwtService.isTokenExpired(token);
            } catch (Exception e) {
                // If there's an error parsing the token, assume it's invalid and remove it
                return true;
            }
        });
    }
}