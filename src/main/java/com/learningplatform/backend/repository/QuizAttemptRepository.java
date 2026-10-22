package com.learningplatform.backend.repository;

import com.learningplatform.backend.entity.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long>{
  List<QuizAttempt> findTop20ByUserEmailOrderByCreatedAtDesc(String userEmail);
}