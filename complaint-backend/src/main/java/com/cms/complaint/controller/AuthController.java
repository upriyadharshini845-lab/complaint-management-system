package com.cms.complaint.controller;

import com.cms.complaint.dto.*;
import com.cms.complaint.security.AppUserDetails;
import com.cms.complaint.service.AuthService;
import com.cms.complaint.service.PasswordResetService;
import com.cms.complaint.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;
    private final PasswordResetService passwordResetService;

    public AuthController(AuthService authService, UserService userService, PasswordResetService passwordResetService) {
        this.authService = authService;
        this.userService = userService;
        this.passwordResetService = passwordResetService;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody RegisterRequest req) {
        UserResponse user = authService.register(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "Registered successfully", "user", user));
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest req) {
        return authService.login(req);
    }

    @PostMapping("/forgot-password")
    public Map<String, String> forgotPassword(@Valid @RequestBody ForgotPasswordRequest req) {
        passwordResetService.requestReset(req.email());
        return Map.of("message", "If the email exists, a reset link has been sent.");
    }

    @PostMapping("/reset-password")
    public Map<String, String> resetPassword(@Valid @RequestBody ResetPasswordRequest req) {
        passwordResetService.resetPassword(req.token(), req.newPassword());
        return Map.of("message", "Password updated successfully. Please login.");
    }

    @GetMapping("/me")
    public UserResponse me(@AuthenticationPrincipal AppUserDetails principal) {
        return userService.getProfile(principal.getId());
    }
}
