package com.example.ecommerceapplication.service;

import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

@Service
public class CaptchaService {

    private final Map<String, String> captchaStore = new HashMap<>();
    private final Random random = new Random();

    public CaptchaResponse generateCaptcha() {
        String captchaId = UUID.randomUUID().toString();
        String captchaText = generateRandomText(6); // 6 characters
        String captchaImage = generateCaptchaImage(captchaText);
        System.out.println(captchaText);
        // Store the captcha for validation later
        captchaStore.put(captchaId, captchaText);

        return new CaptchaResponse(captchaId, captchaImage);
    }

    public boolean validateCaptcha(String captchaId, String userInput) {
        if (captchaId == null || userInput == null) {
            return false;
        }

        String storedCaptcha = captchaStore.get(captchaId);
        if (storedCaptcha == null) {
            return false;
        }

        // Remove from store after validation attempt (one-time use)
        captchaStore.remove(captchaId);

        return storedCaptcha.equals(userInput);
    }

    private String generateRandomText(int length) {
        // Use a mix of digits and letters, avoiding confusing characters like 0/O or
        // 1/I
        String chars = "23456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz";
        StringBuilder captchaText = new StringBuilder();

        for (int i = 0; i < length; i++) {
            captchaText.append(chars.charAt(random.nextInt(chars.length())));
        }

        return captchaText.toString();
    }

    private String generateCaptchaImage(String text) {
        int width = 250;
        int height = 80;
    
        // Create the image
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
    
        // Set background
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);
    
        // Draw random lines (noise)
        for (int i = 0; i < 20; i++) {
            g.setColor(new Color(random.nextInt(255), random.nextInt(255), random.nextInt(255)));
            g.drawLine(random.nextInt(width), random.nextInt(height),
                    random.nextInt(width), random.nextInt(height));
        }
    
        // Calculate actual text width with the font we're using
        g.setFont(new Font("Arial", Font.BOLD, 40));
        FontMetrics metrics = g.getFontMetrics();
        int textWidth = 0;
        for (char c : text.toCharArray()) {
            textWidth += metrics.charWidth(c) + 10; // Add 5px spacing between chars
        }
        
        // Calculate starting X position to center the text
        int startX = (width - textWidth) / 2 + 15; // Add extra padding to compensate for rotation
        int currentX = startX;
    
        // Draw the text
        for (int i = 0; i < text.length(); i++) {
            g.setColor(new Color(random.nextInt(90), random.nextInt(90), random.nextInt(90)));
            
            char currentChar = text.charAt(i);
            int charWidth = metrics.charWidth(currentChar);
            
            // Add rotation for each character
            double rotate = (random.nextInt(41) - 20) * Math.PI / 180.0; // Reduced rotation angle
            g.rotate(rotate, currentX + charWidth/2, 50);
            g.drawString(String.valueOf(currentChar), currentX, 50);
            g.rotate(-rotate, currentX + charWidth/2, 50);
            
            // Move to next character position
            currentX += charWidth + 5; // 5 pixels spacing between characters
        }
    
        // Draw random dots
        for (int i = 0; i < 100; i++) {
            g.setColor(new Color(random.nextInt(255), random.nextInt(255), random.nextInt(255)));
            g.fillRect(random.nextInt(width), random.nextInt(height), 2, 2);
        }
    
        g.dispose();
    
        // Convert to Base64 string
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "png", byteArrayOutputStream);
            byte[] imageBytes = byteArrayOutputStream.toByteArray();
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(imageBytes);
        } catch (IOException e) {
            throw new RuntimeException("Error generating captcha image", e);
        }
    }

    // Inner class for response
    public static class CaptchaResponse {
        private final String captchaId;
        private final String imageData;

        public CaptchaResponse(String captchaId, String imageData) {
            this.captchaId = captchaId;
            this.imageData = imageData;
        }

        public String getCaptchaId() {
            return captchaId;
        }

        public String getImageData() {
            return imageData;
        }
    }
}
