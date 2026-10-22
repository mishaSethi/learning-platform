package com.learningplatform.backend.repository;

import com.learningplatform.backend.entity.QuizQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizQuestionRepository extends JpaRepository<QuizQuestion, Long> {
  
}