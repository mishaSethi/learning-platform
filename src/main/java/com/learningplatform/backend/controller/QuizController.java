package com.learningplatform.backend.controller;

import com.learningplatform.backend.dto.QuizAttemptResponse;
import com.learningplatform.backend.dto.QuizQuestionResponse;
import com.learningplatform.backend.dto.QuizResultResponse;
import com.learningplatform.backend.dto.QuizSubmitRequest;
import com.learningplatform.backend.service.QuizService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quiz")
public class QuizController {

    private final QuizService quizService;

    public QuizController(QuizService quizService) {
        this.quizService = quizService;
    }

    @GetMapping("/questions")
    public List<QuizQuestionResponse> questions() {
        return quizService.getRandomQuestions();
    }

    @PostMapping("/submit")
    public QuizResultResponse submit(@Valid @RequestBody QuizSubmitRequest request, Authentication authentication) {
        return quizService.submit(authentication.getName(), request);
    }

    @GetMapping("/attempts")
    public List<QuizAttemptResponse> attempts(Authentication authentication) {
        return quizService.getMyAttempts(authentication.getName());
    }
}
