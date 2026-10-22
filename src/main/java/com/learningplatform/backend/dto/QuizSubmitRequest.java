package com.learningplatform.backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record QuizSubmitRequest(
        @NotNull List<@Valid QuizAnswer> answers
) {}
