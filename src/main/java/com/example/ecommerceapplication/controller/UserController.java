package com.example.ecommerceapplication.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.ecommerceapplication.dto.AdminRegistrationRequest;
import com.example.ecommerceapplication.dto.CustomerRegistrationRequest;
import com.example.ecommerceapplication.dto.ErrorResponse;
import com.example.ecommerceapplication.dto.FirebaseUserDto;
import com.example.ecommerceapplication.dto.InitialSuperAdminRequest;
import com.example.ecommerceapplication.dto.LoginRequest;
import com.example.ecommerceapplication.dto.LoginResponse;
import com.example.ecommerceapplication.dto.UserResponseDto;
import com.example.ecommerceapplication.exception.EmailAlreadyExistsException;
import com.example.ecommerceapplication.exception.MessageResponse;
import com.example.ecommerceapplication.exception.UsernameAlreadyExistsException;
import com.example.ecommerceapplication.model.User;
import com.example.ecommerceapplication.model.UserStatus;
import com.example.ecommerceapplication.repository.UserRepository;
import com.example.ecommerceapplication.service.JwtService;
import com.example.ecommerceapplication.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
@Validated
public class UserController {
    //private final TokenBlacklistService tokenBlacklistService;
    private final JwtService jwtService;

    public UserController(JwtService jwtService) {
        //this.tokenBlacklistService = tokenBlacklistService;
        this.jwtService = jwtService;
    }

    @Value("${jwt.access.expiry}")
    private long ACCESS_TOKEN_EXPIRY;

    @Value("${jwt.refresh.expiry}")
    private long REFRESH_TOKEN_EXPIRY;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // @Autowired
    // private AdminTokenRepository tokenRepository;

    @Autowired
    private UserRepository userRepository;

    // @Autowired
    // private JwtConfig jwtConfig;

    // // Get properties from jwtConfig
    // private String jwtSecret;
    // private long jwtExpirationMs;

    // @PostConstruct
    // public void init() {
    //     this.jwtSecret = jwtConfig.getJwtSecret();
    //     this.jwtExpirationMs = jwtConfig.getJwtExpirationMs();
    // }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        User user = userService.getUser(id);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/register")
    public ResponseEntity<User> registerCustomer(@Valid @RequestBody CustomerRegistrationRequest request) {
        validateNewUser(request.getEmail(), request.getUsername());
        // String verificationToken = generateVerificationToken();
        // User user = userService.registerCustomer(request, verificationToken);
        User user = userService.registerCustomer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    // @PostMapping("/login")
    // public ResponseEntity<?> loginUser(@Valid @RequestBody LoginRequest request)
    // {
    // try {
    // // Authenticate the user
    // Authentication authentication = authenticationManager.authenticate(
    // new UsernamePasswordAuthenticationToken(
    // request.getUsername(),
    // request.getPassword()));

    // // Set authentication in security context
    // SecurityContextHolder.getContext().setAuthentication(authentication);

    // // Generate JWT token
    // String jwt = jwtTokenProvider.generateToken(authentication);

    // // Get user details
    // UserDetails userDetails = (UserDetails) authentication.getPrincipal();
    // User user = userService.findByUsername(userDetails.getUsername());

    // // Create response with token and user information
    // LoginResponse response = new LoginResponse(
    // jwt,
    // user.getId(),
    // user.getUsername(),
    // user.getEmail(),
    // user.getRole());

    // return ResponseEntity.ok(response);
    // } catch (AuthenticationException e) {
    // return ResponseEntity
    // .status(HttpStatus.UNAUTHORIZED)
    // .body(new ResourceNotFoundException("Invalid username or password"));
    // }
    // }

    // @PostMapping("/login")
    // public ResponseEntity<?> loginUser(@Valid @RequestBody LoginRequest request)
    // {
    // try {
    // // Find user by username
    // User user = userService.findByEmail(request.getEmail());

    // if (user == null) {
    // return ResponseEntity
    // .status(HttpStatus.UNAUTHORIZED)
    // .body(new MessageResponse("Invalid username or password"));
    // }

    // // Check if user account is active
    // if (user.getStatus() != UserStatus.ACTIVE) {
    // return ResponseEntity
    // .status(HttpStatus.UNAUTHORIZED)
    // .body(new MessageResponse("Account is not active. Please verify your
    // email."));
    // }

    // // Verify password (assuming you have a passwordEncoder bean)
    // if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
    // return ResponseEntity
    // .status(HttpStatus.UNAUTHORIZED)
    // .body(new MessageResponse("Invalid username or password"));
    // }

    // // Create session or token (simplified for now)
    // String sessionToken = UUID.randomUUID().toString();

    // // Create response with user information
    // LoginResponse response = new LoginResponse(
    // sessionToken,
    // user.getId(),
    // user.getUsername(),
    // user.getEmail(),
    // user.getRole());

    // return ResponseEntity.ok(response);
    // } catch (Exception e) {
    // return ResponseEntity
    // .status(HttpStatus.INTERNAL_SERVER_ERROR)
    // .body(new MessageResponse("An error occurred during login: " +
    // e.getMessage()));
    // }
    // }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@Valid @RequestBody LoginRequest request, HttpServletResponse httpresponse) {
        try {
            // Find user by username
            User user = userService.findByEmail(request.getEmail());

            if (user == null) {
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(new MessageResponse("Invalid email or password"));
            }

            // Check if user account is active
            if (user.getStatus() != UserStatus.ACTIVE) {
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(new MessageResponse("Account is not active. Please verify your email."));
            }

            // Verify password
            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(new MessageResponse("Invalid email or password"));
            }

            // Generate JWT token
            // String jwtToken = jwtService.generateJwtToken(user);
            String accessToken = jwtService.generateJwtToken(user, ACCESS_TOKEN_EXPIRY);
            String refreshToken = jwtService.generateJwtToken(user, REFRESH_TOKEN_EXPIRY);

            // Store refresh token in HttpOnly cookie
            ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refreshToken)
                    .httpOnly(true)
                    .secure(true)
                    .path("/")
                    .maxAge(REFRESH_TOKEN_EXPIRY / 1000)
                    .build();

            httpresponse.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
            // Update user's token directly using repository
            user.setToken(refreshToken);
            user.setTokenExpired(false);
            userRepository.save(user);

            // Create response with token and user information
            LoginResponse response = new LoginResponse(
                    accessToken,
                    user.getId(),
                    user.getUsername(),
                    user.getEmail(),
                    user.getRole());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("An error occurred during login: " + e.getMessage()));
        }
    }

    // @PostMapping("/google-auth")
    // public ResponseEntity<?> handleGoogleAuth(@RequestBody GoogleAuthDto googleAuthDto) {
    //     try {
    //         // Check if user exists by firebaseUid
    //         Optional<User> existingUser = userService.findByFirebaseUid(googleAuthDto.getFirebaseUid());
            
    //         User user;
    //         if (existingUser.isPresent()) {
    //             // Update existing user
    //             user = existingUser.get();
    //             user.setEmail(googleAuthDto.getEmail());
    //             user.setDisplayName(googleAuthDto.getDisplayName());
    //             user.setPhotoUrl(googleAuthDto.getPhotoURL());
    //         } else {
    //             // Create new user
    //             user = new User();
    //             user.setFirebaseUid(googleAuthDto.getFirebaseUid());
    //             user.setEmail(googleAuthDto.getEmail());
    //             user.setDisplayName(googleAuthDto.getDisplayName());
    //             user.setPhotoUrl(googleAuthDto.getPhotoURL());
    //             //user.setAuthProvider("google");
    //         }
            
    //         // Save user
    //         User savedUser = userService.saveUser(user);
            
    //         return ResponseEntity.ok(savedUser);
    //     } catch (Exception e) {
    //         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
    //                 .body("Error processing Google auth: " + e.getMessage());
    //     }
    // }

    @PostMapping("/google")
    public ResponseEntity<?> handleGoogleAuth(@RequestBody FirebaseUserDto firebaseUserDto) {
        try {
            User user = userService.registerGoogleUser(firebaseUserDto);
            
            // You may want to include cart info in response
            UserResponseDto response = new UserResponseDto(user);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing Google auth: " + e.getMessage());
        }
    }
    
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@CookieValue(value = "refreshToken", required = false) String refreshToken) {
        try {
            if (refreshToken == null || !jwtService.isTokenExpired(refreshToken)) {
                return ResponseEntity.status(401).body("Invalid refresh token");
            }

            String username = jwtService.getUsernameFromToken(refreshToken);
            User user = userService.findByEmail(username);
            String newAccessToken = jwtService.generateJwtToken(user, ACCESS_TOKEN_EXPIRY);

            LoginResponse response = new LoginResponse(
                    newAccessToken,
                    user.getId(),
                    user.getUsername(),
                    user.getEmail(),
                    user.getRole());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("An error occurred during login: " + e.getMessage()));
        }
    }

    // @PostMapping("/logout")
    // public ResponseEntity<Object> logout(
    //         @RequestHeader(value = "Authorization", required = false) String authHeader,
    //         HttpServletRequest request) {

    //     // Try to get token from header parameter first
    //     String jwt = null;

    //     if (authHeader != null && authHeader.startsWith("Bearer ")) {
    //         jwt = authHeader.substring(7);
    //     } else {
    //         // Fallback to request header
    //         String headerAuth = request.getHeader("Authorization");
    //         if (headerAuth != null && headerAuth.startsWith("Bearer ")) {
    //             jwt = headerAuth.substring(7);
    //         }
    //     }

    //     // If we have a token, blacklist it
    //     if (jwt != null && !jwt.isEmpty()) {
    //         tokenBlacklistService.blacklistToken(jwt);

    //         // Clear the security context
    //         SecurityContextHolder.clearContext();

    //         return ResponseEntity.ok().body(new MessageResponse("Logout successful"));
    //     }

    //     // If we're here but the user is authenticated, they might be using a different
    //     // auth method
    //     Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    //     if (auth != null && auth.isAuthenticated()) {
    //         SecurityContextHolder.clearContext();
    //         return ResponseEntity.ok().body(new MessageResponse("Logout successful, but no JWT token was found"));
    //     }

    //     return ResponseEntity.badRequest().body(
    //             new MessageResponse("No token found. Make sure to include 'Authorization: Bearer YOUR_TOKEN' header"));
    // }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        ResponseCookie expiredCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, expiredCookie.toString());
        return ResponseEntity.ok().body(new MessageResponse("Logout successful"));
    }


    // private String extractJwtFromRequest(HttpServletRequest request) {
    // String headerAuth = request.getHeader("Authorization");

    // if (headerAuth != null && headerAuth.startsWith("Bearer ")) {
    // return headerAuth.substring(7);
    // }

    // return null;
    // }

    // Helper methods for JWT operations

    // private String getUsernameFromToken(String token) {
    // try {
    // // Create signing key
    // Key signingKey = Keys.hmacShaKeyFor(jwtSecret.getBytes());

    // // Parse claims from token
    // return Jwts.parserBuilder()
    // .setSigningKey(signingKey)
    // .build()
    // .parseClaimsJws(token)
    // .getBody()
    // .getSubject();
    // } catch (Exception e) {
    // return null;
    // }
    // }
    @GetMapping("/verify")
    public ResponseEntity<String> verifyEmail(@RequestParam("token") String token) {
        boolean isVerified = userService.verifyEmailToken(token);

        if (isVerified) {
            return ResponseEntity
                    .ok("<html><body style='text-align: center; padding: 50px; font-family: Arial, sans-serif;'>"
                            + "<h1 style='color: green;'>Email Verified Successfully! 🎉</h1>"
                            + "<p>Your email has been successfully verified. You can now log in.</p>"
                            + "<a href='/' style='background-color: blue; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px;'>Go to Login</a>"
                            + "</body></html>");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("<html><body style='text-align: center; padding: 50px; font-family: Arial, sans-serif;'>"
                            + "<h1 style='color: red;'>Verification Failed ❌</h1>"
                            + "<p>Invalid or expired token. Please request a new verification email.</p>"
                            + "<a href='/resend-verification' style='background-color: red; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px;'>Resend Email</a>"
                            + "</body></html>");
        }
    }

    @PostMapping("/admin/invite")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> createAdminInvitation(@Valid @RequestParam String email) {
        userService.createAdminInvitation(email);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/admin/register")
    public ResponseEntity<User> registerAdmin(@Valid @RequestBody AdminRegistrationRequest request) {
        validateNewUser(request.getEmail(), request.getUsername());
        User user = userService.registerAdmin(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @PostMapping("/super-admin/initialize")
    public ResponseEntity<User> createInitialSuperAdmin(@Valid @RequestBody InitialSuperAdminRequest request) {
        validateNewUser(request.getEmail(), request.getUsername());
        User user = userService.createInitialSuperAdmin(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    private void validateNewUser(String email, String username) {
        if (userService.existsByEmail(email)) {
            throw new EmailAlreadyExistsException("Email already registered: " + email);
        }
        if (userService.existsByUsername(username)) {
            throw new UsernameAlreadyExistsException("Username already taken: " + username);
        }
    }

    // private String generateVerificationToken() {
    // return generateSecureToken();
    // }

    // private String generateSecureToken() {
    // byte[] randomBytes = new byte[32];
    // new SecureRandom().nextBytes(randomBytes);
    // return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    // }

    // Exception handlers
    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleEmailAlreadyExists(EmailAlreadyExistsException ex) {
        ErrorResponse error = new ErrorResponse(HttpStatus.CONFLICT.value(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(UsernameAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleUsernameAlreadyExists(UsernameAlreadyExistsException ex) {
        ErrorResponse error = new ErrorResponse(HttpStatus.CONFLICT.value(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Validation failed", errors);
        return ResponseEntity.badRequest().body(error);
    }
}
