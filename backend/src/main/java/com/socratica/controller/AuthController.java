package com.socratica.controller;

import com.socratica.dto.AuthResponse;
import com.socratica.dto.ErrorResponse;
import com.socratica.dto.ForgotPasswordRequest;
import com.socratica.dto.LoginRequest;
import com.socratica.dto.ResetPasswordRequest;
import com.socratica.dto.SignUpRequest;
import com.socratica.service.AuthService;
import com.socratica.service.OAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final OAuthService oAuthService;

    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(Map.of("status", "ok", "message", "Auth controller is working"));
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signUp(@Valid @RequestBody SignUpRequest request) {
        try {
            log.info("Signup request received for email: {}", request.getEmail());
            AuthResponse response = authService.signUp(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            log.error("Signup error: {}", e.getMessage(), e);
            ErrorResponse errorResponse = ErrorResponse.builder()
                    .message(e.getMessage() != null ? e.getMessage() : "Sign up failed. Please try again.")
                    .error("Bad Request")
                    .build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            log.error("Unexpected signup error: {}", e.getMessage(), e);
            ErrorResponse errorResponse = ErrorResponse.builder()
                    .message("An unexpected error occurred. Please try again.")
                    .error("Internal Server Error")
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            ErrorResponse errorResponse = ErrorResponse.builder()
                    .message(e.getMessage() != null ? e.getMessage() : "Login failed. Please check your credentials.")
                    .error("Unauthorized")
                    .build();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
    }

    @GetMapping("/oauth/google/url")
    public ResponseEntity<?> getGoogleAuthUrl(@RequestParam(value = "frontendOrigin", required = false) String frontendOrigin) {
        try {
            log.info("Generating Google OAuth URL...");
            String url = oAuthService.getGoogleAuthUrl(frontendOrigin);
            log.info("Google OAuth URL generated successfully: {}", url.substring(0, Math.min(100, url.length())) + "...");
            return ResponseEntity.ok(new AuthResponse.AuthUrlResponse(url));
        } catch (IllegalStateException e) {
            log.error("Configuration error generating Google OAuth URL: {}", e.getMessage(), e);
            ErrorResponse errorResponse = ErrorResponse.builder()
                    .message(e.getMessage())
                    .error("Configuration Error")
                    .build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (NullPointerException e) {
            log.error("Null pointer error generating Google OAuth URL: {}", e.getMessage(), e);
            ErrorResponse errorResponse = ErrorResponse.builder()
                    .message("OAuth configuration is not properly initialized: " + e.getMessage())
                    .error("Configuration Error")
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        } catch (Exception e) {
            log.error("Error generating Google OAuth URL: {}", e.getMessage(), e);
            e.printStackTrace(); // Print full stack trace for debugging
            ErrorResponse errorResponse = ErrorResponse.builder()
                    .message("Failed to generate Google OAuth URL: " + e.getMessage())
                    .error("Internal Server Error")
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/oauth/google/callback")
    public ResponseEntity<?> handleGoogleCallback(
            @RequestParam("code") String code,
            @RequestParam(value = "state", required = false) String state
    ) {
        try {
            AuthResponse response = oAuthService.handleGoogleCallback(code);
            String frontendUrl = oAuthService.resolveFrontendUrl(state);
            // Redirect to frontend with token and user info
            String redirectUrl = String.format("%s/auth/callback?token=%s&provider=google&userId=%s&email=%s&name=%s&surname=%s",
                    frontendUrl,
                    response.getToken(),
                    response.getUser().getId(),
                    java.net.URLEncoder.encode(response.getUser().getEmail(), java.nio.charset.StandardCharsets.UTF_8),
                    java.net.URLEncoder.encode(response.getUser().getName() != null ? response.getUser().getName() : "", java.nio.charset.StandardCharsets.UTF_8),
                    java.net.URLEncoder.encode(response.getUser().getSurname() != null ? response.getUser().getSurname() : "", java.nio.charset.StandardCharsets.UTF_8));
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header("Location", redirectUrl)
                    .build();
        } catch (IOException e) {
            log.error("Error handling Google OAuth callback: {}", e.getMessage(), e);
            ErrorResponse errorResponse = ErrorResponse.builder()
                    .message("Failed to authenticate with Google")
                    .error("OAuth Error")
                    .build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            log.error("Unexpected error in Google OAuth callback: {}", e.getMessage(), e);
            ErrorResponse errorResponse = ErrorResponse.builder()
                    .message("An unexpected error occurred during Google authentication")
                    .error("Internal Server Error")
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        try {
            log.info("Password reset request received for email: {}", request.getEmail());
            authService.requestPasswordReset(request);
            return ResponseEntity.ok(Map.of(
                "message", "If an account with that email exists, a password reset link has been sent."
            ));
        } catch (RuntimeException e) {
            // Don't reveal if email exists or not (security best practice)
            log.info("Password reset request for email: {} - {}", request.getEmail(), e.getMessage());
            return ResponseEntity.ok(Map.of(
                "message", "If an account with that email exists, a password reset link has been sent."
            ));
        } catch (Exception e) {
            log.error("Error processing password reset request: {}", e.getMessage(), e);
            ErrorResponse errorResponse = ErrorResponse.builder()
                    .message("An error occurred. Please try again later.")
                    .error("Internal Server Error")
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        try {
            log.info("Password reset attempt with token");
            authService.resetPassword(request);
            return ResponseEntity.ok(Map.of(
                "message", "Password has been reset successfully. You can now login with your new password."
            ));
        } catch (RuntimeException e) {
            log.error("Password reset error: {}", e.getMessage());
            ErrorResponse errorResponse = ErrorResponse.builder()
                    .message(e.getMessage() != null ? e.getMessage() : "Failed to reset password")
                    .error("Bad Request")
                    .build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            log.error("Unexpected error resetting password: {}", e.getMessage(), e);
            ErrorResponse errorResponse = ErrorResponse.builder()
                    .message("An unexpected error occurred. Please try again.")
                    .error("Internal Server Error")
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
