package com.example.ecommerceapplication.dto;

public class FirebaseUserDto {
    private String uid;
    private String email;
    private String displayName;
    private String photoURL;
    private boolean emailVerified;
    
    // Getters
    public String getUid() {
        return uid;
    }
    
    public String getEmail() {
        return email;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getPhotoURL() {
        return photoURL;
    }
    
    public boolean isEmailVerified() {
        return emailVerified;
    }
    
    // Setters
    public void setUid(String uid) {
        this.uid = uid;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
    
    public void setPhotoURL(String photoURL) {
        this.photoURL = photoURL;
    }
    
    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }
}
