package com.example.ecommerceapplication.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${application.frontend.url}")
    private String frontendUrl;

    public void sendVerificationEmail(String email, String token) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            String verificationLink = frontendUrl + "/verify?token=" + token;
            String emailContent = createVerificationEmailTemplate(verificationLink);

            helper.setFrom(fromEmail);
            helper.setTo(email);
            helper.setSubject("Email Verification");
            helper.setText(emailContent, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send verification email", e);
        }
    }

    public void sendAdminInvitationEmail(String email, String token) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            String invitationLink = frontendUrl + "/admin/accept-invitation?token=" + token;
            String emailContent = createAdminInvitationEmailTemplate(invitationLink);

            helper.setFrom(fromEmail);
            helper.setTo(email);
            helper.setSubject("Admin Invitation");
            helper.setText(emailContent, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send admin invitation email", e);
        }
    }

    private String createVerificationEmailTemplate(String verificationLink) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    .email-container {
                        font-family: Arial, sans-serif;
                        max-width: 600px;
                        margin: 0 auto;
                        padding: 20px;
                    }
                    .button {
                        background-color: #4CAF50;
                        border: none;
                        color: white;
                        padding: 15px 32px;
                        text-align: center;
                        text-decoration: none;
                        display: inline-block;
                        font-size: 16px;
                        margin: 4px 2px;
                        cursor: pointer;
                        border-radius: 4px;
                    }
                </style>
            </head>
            <body>
                <div class="email-container">
                    <h2>Verify Your Email Address</h2>
                    <p>Hello,</p>
                    <p>Thank you for registering! Please click the button below to verify your email address:</p>
                    <p>
                        <a href="%s" class="button">Verify Email</a>
                    </p>
                    <p>Or copy and paste this link in your browser:</p>
                    <p>%s</p>
                    <p>If you didn't request this verification, please ignore this email.</p>
                    <p>This link will expire in 24 hours.</p>
                    <p>Best regards,<br/>Your Application Team</p>
                </div>
            </body>
            </html>
        """.formatted(verificationLink, verificationLink);
    }

    private String createAdminInvitationEmailTemplate(String invitationLink) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    .email-container {
                        font-family: Arial, sans-serif;
                        max-width: 600px;
                        margin: 0 auto;
                        padding: 20px;
                    }
                    .button {
                        background-color: #4CAF50;
                        border: none;
                        color: white;
                        padding: 15px 32px;
                        text-align: center;
                        text-decoration: none;
                        display: inline-block;
                        font-size: 16px;
                        margin: 4px 2px;
                        cursor: pointer;
                        border-radius: 4px;
                    }
                </style>
            </head>
            <body>
                <div class="email-container">
                    <h2>Admin Invitation</h2>
                    <p>Hello,</p>
                    <p>You have been invited to become an administrator. Please click the button below to accept the invitation:</p>
                    <p>
                        <a href="%s" class="button">Accept Invitation</a>
                    </p>
                    <p>Or copy and paste this link in your browser:</p>
                    <p>%s</p>
                    <p>If you didn't expect this invitation, please ignore this email.</p>
                    <p>This invitation link will expire in 48 hours.</p>
                    <p>Best regards,<br/>Your Application Team</p>
                </div>
            </body>
            </html>
        """.formatted(invitationLink, invitationLink);
    }
}