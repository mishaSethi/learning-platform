package com.learningplatform.backend.controller;

import com.learningplatform.backend.dto.UserResponse;
import com.learningplatform.backend.service.AuthService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class UserController {

    private final AuthService authService;

    public UserController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/me")
    public UserResponse me(Authentication authentication) {
        return authService.getCurrentUser(authentication.getName());
    }
}
