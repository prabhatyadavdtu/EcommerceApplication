package com.example.ecommerceapplication.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.ecommerceapplication.dto.AdminRegistrationRequest;
import com.example.ecommerceapplication.dto.CustomerRegistrationRequest;
import com.example.ecommerceapplication.dto.FirebaseUserDto;
import com.example.ecommerceapplication.dto.InitialSuperAdminRequest;
import com.example.ecommerceapplication.exception.EmailAlreadyExistsException;
import com.example.ecommerceapplication.exception.InvalidTokenException;
import com.example.ecommerceapplication.exception.ResourceNotFoundException;
import com.example.ecommerceapplication.exception.UsernameAlreadyExistsException;
import com.example.ecommerceapplication.model.AdminInvitationToken;
import com.example.ecommerceapplication.model.Role;
import com.example.ecommerceapplication.model.User;
import com.example.ecommerceapplication.model.UserStatus;
import com.example.ecommerceapplication.repository.AdminTokenRepository;
import com.example.ecommerceapplication.repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @Autowired
    private AdminTokenRepository adminTokenRepository; // For storing admin invitation tokens

    @Value("${admin.creation.secret}")
    private String adminCreationSecret; // Secure secret stored in application properties

    public User getUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    // Public registration for customers
    public User registerCustomer(CustomerRegistrationRequest request) {
        validateNewUser(request.getEmail(), request.getUsername());

        User customer = new User();
        customer.setUsername(request.getUsername());
        customer.setPassword(passwordEncoder.encode(request.getPassword()));
        customer.setEmail(request.getEmail());
        customer.setRole(Role.CUSTOMER);
        customer.setEmailVerified(false);
        customer.setStatus(UserStatus.PENDING_EMAIL_VERIFICATION);

        // Generate and send email verification token
        String verificationToken = generateVerificationToken();
        customer.setVerificationToken(verificationToken);
        emailService.sendVerificationEmail(customer.getEmail(), verificationToken);

        return userRepository.save(customer);
    }

    public User registerGoogleUser(FirebaseUserDto firebaseUserDto) {
        // Check if user exists by email
        Optional<User> existingUser = userRepository.findByEmail(firebaseUserDto.getEmail());

        User user;
        if (existingUser.isPresent()) {
            // Update existing user
            user = existingUser.get();
        } else {
            // Create new user
            user = new User();
            // Generate username from email (or use display name)
            String username = generateUsernameFromEmail(firebaseUserDto.getEmail());
            user.setUsername(username);
            user.setStatus(UserStatus.ACTIVE);
            user.setRole(Role.USER); // Or determine role based on your business logic
        }

        // Set/update Google-specific fields
        user.setEmail(firebaseUserDto.getEmail());
        if (firebaseUserDto.isEmailVerified()) {
            user.setEmailVerified(firebaseUserDto.isEmailVerified());
        } else {
            String verificationToken = generateVerificationToken();
            user.setVerificationToken(verificationToken);
            emailService.sendVerificationEmail(firebaseUserDto.getEmail(), verificationToken);
            user.setEmailVerified(firebaseUserDto.isEmailVerified());
        }
        // No need to set password for OAuth users

        // Generate a token if needed
        if (user.getToken() == null) {
            user.setToken(UUID.randomUUID().toString());
            user.setRevoked(false);
            user.setTokenExpired(false);
        }

        return userRepository.save(user);
    }

    public String generateUsernameFromEmail(String email) {
        if (email == null)
            return "user_" + UUID.randomUUID().toString().substring(0, 8);

        // Extract part before @ and remove special characters
        String usernameBase = email.split("@")[0].replaceAll("[^a-zA-Z0-9]", "");

        // Check if username exists
        String username = usernameBase;
        int counter = 1;

        while (userRepository.findByUsername(username).isPresent()) {
            username = usernameBase + counter;
            counter++;
        }

        return username;
    }

    // Admin creation - only possible through invitation or initial setup
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public void createAdminInvitation(String email) {
        // Only existing super admins can create new admins
        AdminInvitationToken token = new AdminInvitationToken();
        token.setEmail(email);
        token.setToken(generateSecureToken());
        token.setExpiryDate(LocalDateTime.now().plusHours(24));

        adminTokenRepository.save(token);
        emailService.sendAdminInvitationEmail(email, token.getToken());
    }

    // Process admin registration through invitation
    public User registerAdmin(AdminRegistrationRequest request) {
        AdminInvitationToken token = adminTokenRepository.findByToken(request.getInvitationToken())
                .orElseThrow(() -> new InvalidTokenException("Invalid or expired invitation token"));

        if (token.isExpired() || !token.getEmail().equals(request.getEmail())) {
            throw new InvalidTokenException("Invalid or expired invitation token");
        }

        validateNewUser(request.getEmail(), request.getUsername());

        User admin = new User();
        admin.setUsername(request.getUsername());
        admin.setPassword(passwordEncoder.encode(request.getPassword()));
        admin.setEmail(request.getEmail());
        admin.setRole(Role.ADMIN);
        admin.setEmailVerified(true); // Email already verified through invitation
        admin.setStatus(UserStatus.ACTIVE);

        // Invalidate the used token
        adminTokenRepository.delete(token);

        return userRepository.save(admin);
    }

    // Initial super admin creation - should only be possible during first
    // application setup
    public User createInitialSuperAdmin(InitialSuperAdminRequest request) {
        // Check if any admin exists
        if (userRepository.existsByRole(Role.SUPER_ADMIN)) {
            throw new SecurityException("Super admin already exists");
        }

        // Verify the admin creation secret
        if (!adminCreationSecret.equals(request.getAdminCreationSecret())) {
            throw new SecurityException("Invalid admin creation secret");
        }

        validateNewUser(request.getEmail(), request.getUsername());

        User superAdmin = new User();
        superAdmin.setUsername(request.getUsername());
        superAdmin.setPassword(passwordEncoder.encode(request.getPassword()));
        superAdmin.setEmail(request.getEmail());
        superAdmin.setRole(Role.SUPER_ADMIN);
        superAdmin.setEmailVerified(true);
        superAdmin.setStatus(UserStatus.ACTIVE);

        return userRepository.save(superAdmin);
    }

    private void validateNewUser(String email, String username) {
        if (existsByEmail(email)) {
            throw new EmailAlreadyExistsException("Email already registered: " + email);
        }
        if (existsByUsername(username)) {
            throw new UsernameAlreadyExistsException("Username already taken: " + username);
        }
    }

    // Helper methods for token generation and email sending
    private String generateVerificationToken() {
        return UUID.randomUUID().toString();
    }

    // private String generateSecureToken() {
    // return UUID.randomUUID().toString();
    // }

    private String generateSecureToken() {
        byte[] randomBytes = new byte[32];
        new SecureRandom().nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    // public User findByToken(String token) {
    // return userRepository.findByToken(token).orElse(null);
    // }

    public boolean verifyEmailToken(String token) {
        Optional<User> userOptional = userRepository.findByVerificationtoken(token);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setEmailVerified(true);
            user.setStatus(UserStatus.ACTIVE);
            user.setVerificationToken(null); // Remove token after verification
            userRepository.save(user);
            return true;
        }
        return false;
    }
}
