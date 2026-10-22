package com.learningplatform.backend.dto;

import java.util.List;

// A quiz question as the browser sees it - deliberately has NO correct answer
public record QuizQuestionResponse(Long id, String text, List<String> options) {}
