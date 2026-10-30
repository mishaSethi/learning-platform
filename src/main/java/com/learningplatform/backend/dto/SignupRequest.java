package com.learningplatform.backend.dto;

import com.learningplatform.backend.validation.StrongPassword;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record SignupRequest(
  @NotBlank String name,
  @NotBlank @Email String email,
  @StrongPassword String password,
  String phone
){}
