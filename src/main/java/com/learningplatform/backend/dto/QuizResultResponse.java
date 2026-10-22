package com.learningplatform.backend.dto;

import java.util.List;

public record QuizResultResponse(int score, int total, List<QuizReviewItem> review) {}
