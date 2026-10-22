package com.learningplatform.backend.dto;

import java.time.LocalDateTime;

public record QuizAttemptResponse(Long id, int score, int total, LocalDateTime createdAt) {}
