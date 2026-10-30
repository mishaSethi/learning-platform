package com.learningplatform.backend.dto;

// One leaderboard row. Only the name is shown - never another user's email.
// rank and bestScore are null for users who haven't taken the quiz yet.
public record LeaderboardEntry(Integer rank, String name, Integer bestScore, long attempts, boolean you) {}
