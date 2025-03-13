package com.example.ecommerceapplication.controller;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.ecommerceapplication.service.CaptchaService;
import com.example.ecommerceapplication.service.CaptchaService.CaptchaResponse;

@RestController
@RequestMapping("/api/captcha")
public class CaptchaController {

    private final CaptchaService captchaService;
    
    public CaptchaController(CaptchaService captchaService) {
        this.captchaService = captchaService;
    }
    
    @GetMapping("/generate")
    public ResponseEntity<CaptchaService.CaptchaResponse> generateCaptcha() {
        CaptchaResponse captcha = captchaService.generateCaptcha();
        return ResponseEntity.ok(captcha);
    }
    
    @PostMapping("/validate")
    public ResponseEntity<ValidationResponse> validateCaptcha(@RequestBody ValidationRequest request) {
        boolean isValid = captchaService.validateCaptcha(request.getCaptchaId(), request.getUserInput());
        return ResponseEntity.ok(new ValidationResponse(isValid));
    }
    
    // Request/response classes
    public static class ValidationRequest {
        private String captchaId;
        private String userInput;
        
        public ValidationRequest() {
        }
        
        public ValidationRequest(String captchaId, String userInput) {
            this.captchaId = captchaId;
            this.userInput = userInput;
        }
        
        public String getCaptchaId() {
            return captchaId;
        }
        
        public void setCaptchaId(String captchaId) {
            this.captchaId = captchaId;
        }
        
        public String getUserInput() {
            return userInput;
        }
        
        public void setUserInput(String userInput) {
            this.userInput = userInput;
        }
    }
    
    public static class ValidationResponse {
        private boolean valid;
        
        public ValidationResponse() {
        }
        
        public ValidationResponse(boolean valid) {
            this.valid = valid;
        }
        
        public boolean isValid() {
            return valid;
        }
        
        public void setValid(boolean valid) {
            this.valid = valid;
        }
    }
}
