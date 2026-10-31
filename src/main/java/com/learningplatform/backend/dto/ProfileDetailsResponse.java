package com.learningplatform.backend.dto;

import java.time.LocalDate;

/**
 * The full profile shown on the My Profile page. Name and email are read-only;
 * phoneLocked tells the page whether the phone can still be added.
 */
public record ProfileDetailsResponse(
        String name,
        String email,
        String phone,
        boolean phoneLocked,
        LocalDate dateOfBirth,
        String gender,
        String addressLine,
        String city,
        String state,
        String postalCode,
        String country,
        String experienceLevel,
        String emergencyContactName,
        String emergencyContactPhone,
        String bio
) {}
