package com.learningplatform.backend.controller;

import com.learningplatform.backend.dto.AuthResponse;
import com.learningplatform.backend.dto.ForgotPasswordRequest;
import com.learningplatform.backend.dto.LoginRequest;
import com.learningplatform.backend.dto.MessageResponse;
import com.learningplatform.backend.dto.ResetPasswordRequest;
import com.learningplatform.backend.dto.SignupRequest;
import com.learningplatform.backend.dto.UserResponse;
import com.learningplatform.backend.dto.VerifyTwoFactorRequest;
import com.learningplatform.backend.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse signup(@Valid @RequestBody SignupRequest request) {
        return authService.signup(request);
    }

    @PostMapping("/login")
    public MessageResponse login(@Valid @RequestBody LoginRequest request) {
        authService.login(request);
        return new MessageResponse("We've sent a login code to your email.");
    }

    @PostMapping("/verify-2fa")
    public AuthResponse verifyTwoFactor(@Valid @RequestBody VerifyTwoFactorRequest request) {
        return authService.verifyTwoFactor(request);
    }

    @PostMapping("/forgot-password")
    public MessageResponse forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return new MessageResponse("If this email is registered, we've sent a reset code.");
    }

    @PostMapping("/reset-password")
    public MessageResponse resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return new MessageResponse("Password updated. You can now log in.");
    }
}
