package com.learningplatform.backend.dto;

import java.util.List;

// One line of the result screen: what the user picked, what was right, and why
public record QuizReviewItem(
        Long questionId,
        String text,
        List<String> options,
        Integer selectedOption,
        int correctOption,
        boolean correct,
        String explanation
) {}
