package com.learningplatform.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "quiz_attempts")
public class QuizAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String userEmail;

    private int score;

    private int total;

    private LocalDateTime createdAt;

    public QuizAttempt() {
    }

    public QuizAttempt(String userEmail, int score, int total) {
        this.userEmail = userEmail;
        this.score = score;
        this.total = total;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getUserEmail() { return userEmail; }
    public int getScore() { return score; }
    public int getTotal() { return total; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
