package com.example.ecommerceapplication.dto;

import com.example.ecommerceapplication.model.Role;
import com.example.ecommerceapplication.model.User;
import com.example.ecommerceapplication.model.UserStatus;

public class UserResponseDto {
    private Long id;
    private String username;
    private String email;
    private UserStatus status;
    private Role role;
    
    public UserResponseDto(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.status = user.getStatus();
        this.role = user.getRole();
    }
    
    // Getters
    public Long getId() {
        return id;
    }
    
    public String getUsername() {
        return username;
    }
    
    public String getEmail() {
        return email;
    }
    
    public UserStatus getStatus() {
        return status;
    }
    
    public Role getRole() {
        return role;
    }
}
