package com.learningplatform.backend.service;

import com.learningplatform.backend.dto.LeaderboardEntry;
import com.learningplatform.backend.dto.LeaderboardResponse;
import com.learningplatform.backend.dto.QuizAnswer;
import com.learningplatform.backend.dto.QuizAttemptResponse;
import com.learningplatform.backend.dto.QuizQuestionResponse;
import com.learningplatform.backend.dto.QuizResultResponse;
import com.learningplatform.backend.dto.QuizReviewItem;
import com.learningplatform.backend.dto.QuizSubmitRequest;
import com.learningplatform.backend.entity.QuizAttempt;
import com.learningplatform.backend.entity.QuizQuestion;
import com.learningplatform.backend.entity.User;
import com.learningplatform.backend.repository.QuizAttemptRepository;
import com.learningplatform.backend.repository.QuizQuestionRepository;
import com.learningplatform.backend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class QuizService {

    public static final int QUIZ_SIZE = 10;
    private static final int LEADERBOARD_SIZE = 50;

    private final QuizQuestionRepository questionRepository;
    private final QuizAttemptRepository attemptRepository;
    private final UserRepository userRepository;

    public QuizService(QuizQuestionRepository questionRepository, QuizAttemptRepository attemptRepository,
                       UserRepository userRepository) {
        this.questionRepository = questionRepository;
        this.attemptRepository = attemptRepository;
        this.userRepository = userRepository;
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

    /**
     * Every registered user. Players are ranked by best score - equal best scores share a rank
     * (1, 1, 3...) - and users who haven't taken the quiz yet follow, unranked, by name.
     * Also returns the current user's own row, so they always see where they stand.
     */
    public LeaderboardResponse getLeaderboard(String email) {
        List<QuizAttemptRepository.LeaderboardRow> rows = attemptRepository.findLeaderboard();
        List<User> users = userRepository.findAll();

        Map<String, String> names = users.stream()
                .collect(Collectors.toMap(User::getEmail, User::getName));

        List<LeaderboardEntry> ranked = new ArrayList<>();
        int rank = 0;
        Integer previousScore = null;
        for (int i = 0; i < rows.size(); i++) {
            QuizAttemptRepository.LeaderboardRow row = rows.get(i);
            if (!row.getBestScore().equals(previousScore)) {
                rank = i + 1;
                previousScore = row.getBestScore();
            }
            ranked.add(new LeaderboardEntry(rank, names.getOrDefault(row.getEmail(), "Former climber"),
                    row.getBestScore(), row.getAttempts(), row.getEmail().equals(email)));
        }

        // Registered users with no attempts: listed after the players, no rank or score yet
        Set<String> played = rows.stream()
                .map(QuizAttemptRepository.LeaderboardRow::getEmail)
                .collect(Collectors.toSet());
        users.stream()
                .filter(u -> !played.contains(u.getEmail()))
                .sorted(Comparator.comparing(User::getName, String.CASE_INSENSITIVE_ORDER))
                .forEach(u -> ranked.add(new LeaderboardEntry(null, u.getName(), null, 0,
                        u.getEmail().equals(email))));

        LeaderboardEntry me = ranked.stream().filter(LeaderboardEntry::you).findFirst().orElse(null);
        return new LeaderboardResponse(ranked.stream().limit(LEADERBOARD_SIZE).toList(), me, users.size());
    }
}
