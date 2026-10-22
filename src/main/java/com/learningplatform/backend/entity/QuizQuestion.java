package com.learningplatform.backend.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "quiz_questions")
public class QuizQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String text;

    // The 4 answer choices, kept in order (A, B, C, D)
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "quiz_question_options", joinColumns = @JoinColumn(name = "question_id"))
    @OrderColumn(name = "position")
    @Column(name = "option_text", nullable = false)
    private List<String> options = new ArrayList<>();

    // Index of the right option: 0 = first, 3 = last
    private int correctOption;

    @Column(length = 1000)
    private String explanation;

    public QuizQuestion() {
    }

    public QuizQuestion(String text, List<String> options, int correctOption, String explanation) {
        this.text = text;
        this.options = new ArrayList<>(options);
        this.correctOption = correctOption;
        this.explanation = explanation;
    }

    public Long getId() { return id; }
    public String getText() { return text; }
    public List<String> getOptions() { return options; }
    public int getCorrectOption() { return correctOption; }
    public String getExplanation() { return explanation; }
}
