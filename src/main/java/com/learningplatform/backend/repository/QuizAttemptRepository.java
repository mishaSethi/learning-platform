package com.learningplatform.backend.repository;

import com.learningplatform.backend.entity.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long>{
  List<QuizAttempt> findTop20ByUserEmailOrderByCreatedAtDesc(String userEmail);

  // One row per user: their best score and how many quizzes they have taken,
  // best scores first; on a tie, fewer attempts ranks higher
  @Query("""
      select a.userEmail as email, max(a.score) as bestScore, count(a) as attempts
      from QuizAttempt a
      group by a.userEmail
      order by max(a.score) desc, count(a) asc
      """)
  List<LeaderboardRow> findLeaderboard();

  interface LeaderboardRow {
    String getEmail();
    Integer getBestScore();
    Long getAttempts();
  }
}
