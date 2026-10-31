package com.learningplatform.backend.dto;

import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * What the profile form sends. Every field is optional - empty means "not filled in".
 * Name and email can't be changed here; phone is only accepted if none was given at sign-up.
 */
public record ProfileDetailsRequest(
        // Only used when the account has no phone yet
        @Pattern(regexp = "^$|^\\+?[0-9]{7,15}$") String phone,

        @Past LocalDate dateOfBirth,
        @Pattern(regexp = "^$|^(Female|Male|Non-binary|Prefer not to say)$") String gender,

        @Size(max = 120) String addressLine,
        @Size(max = 60) @Pattern(regexp = "^$|^[\\p{L} .'-]+$") String city,
        @Size(max = 60) @Pattern(regexp = "^$|^[\\p{L} .'-]+$") String state,
        @Pattern(regexp = "^$|^[A-Za-z0-9 -]{3,10}$") String postalCode,
        @Size(max = 60) @Pattern(regexp = "^$|^[\\p{L} .'-]+$") String country,

        @Pattern(regexp = "^$|^(Beginner|Intermediate|Expert)$") String experienceLevel,
        @Size(max = 60) @Pattern(regexp = "^$|^[\\p{L} .'-]+$") String emergencyContactName,
        @Pattern(regexp = "^$|^\\+?[0-9]{7,15}$") String emergencyContactPhone,

        @Size(max = 500) String bio
) {}
