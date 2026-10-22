package com.learningplatform.backend.service;

import com.learningplatform.backend.dto.QuizAnswer;
import com.learningplatform.backend.dto.QuizAttemptResponse;
import com.learningplatform.backend.dto.QuizQuestionResponse;
import com.learningplatform.backend.dto.QuizResultResponse;
import com.learningplatform.backend.dto.QuizReviewItem;
import com.learningplatform.backend.dto.QuizSubmitRequest;
import com.learningplatform.backend.entity.QuizAttempt;
import com.learningplatform.backend.entity.QuizQuestion;
import com.learningplatform.backend.repository.QuizAttemptRepository;
import com.learningplatform.backend.repository.QuizQuestionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class QuizService {

    public static final int QUIZ_SIZE = 10;

    private final QuizQuestionRepository questionRepository;
    private final QuizAttemptRepository attemptRepository;

    public QuizService(QuizQuestionRepository questionRepository, QuizAttemptRepository attemptRepository) {
        this.questionRepository = questionRepository;
        this.attemptRepository = attemptRepository;
    }

    /** 10 random questions, WITHOUT the correct answers. */
    public List<QuizQuestionResponse> getRandomQuestions() {
        List<QuizQuestion> all = new ArrayList<>(questionRepository.findAll());
        Collections.shuffle(all);

        return all.stream()
                .limit(QUIZ_SIZE)
                .map(q -> new QuizQuestionResponse(q.getId(), q.getText(), q.getOptions()))
                .toList();
    }

    /** Marks the answers on the server, saves the attempt, returns the full review. */
    public QuizResultResponse submit(String email, QuizSubmitRequest request) {
        List<QuizAnswer> answers = request.answers();

        if (answers.size() != QUIZ_SIZE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A quiz has exactly " + QUIZ_SIZE + " answers");
        }

        List<Long> ids = answers.stream().map(QuizAnswer::questionId).toList();
        if (new HashSet<>(ids).size() != ids.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Each question can be answered only once");
        }

        Map<Long, QuizQuestion> questions = questionRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(QuizQuestion::getId, Function.identity()));
        if (questions.size() != ids.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown question");
        }

        List<QuizReviewItem> review = new ArrayList<>();
        int score = 0;
        for (QuizAnswer answer : answers) {
            QuizQuestion question = questions.get(answer.questionId());
            boolean correct = Objects.equals(answer.selectedOption(), question.getCorrectOption());
            if (correct) {
                score++;
            }
            review.add(new QuizReviewItem(question.getId(), question.getText(), question.getOptions(),
                    answer.selectedOption(), question.getCorrectOption(), correct, question.getExplanation()));
        }

        attemptRepository.save(new QuizAttempt(email, score, answers.size()));
        return new QuizResultResponse(score, answers.size(), review);
    }

    /** The user's 20 most recent attempts, newest first. */
    public List<QuizAttemptResponse> getMyAttempts(String email) {
        return attemptRepository.findTop20ByUserEmailOrderByCreatedAtDesc(email).stream()
                .map(a -> new QuizAttemptResponse(a.getId(), a.getScore(), a.getTotal(), a.getCreatedAt()))
                .toList();
    }
}
