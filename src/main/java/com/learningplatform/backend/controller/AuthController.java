package com.learningplatform.backend.controller;

import com.learningplatform.backend.dto.AuthResponse;
import com.learningplatform.backend.dto.LoginRequest;
import com.learningplatform.backend.dto.SignupRequest;
import com.learningplatform.backend.dto.UserResponse;
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
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }
}
