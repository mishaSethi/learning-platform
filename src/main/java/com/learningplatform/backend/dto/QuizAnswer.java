package com.learningplatform.backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

// selectedOption: 0-3, or null if the user skipped the question
public record QuizAnswer(
        @NotNull Long questionId,
        @Min(0) @Max(3) Integer selectedOption
) {}
