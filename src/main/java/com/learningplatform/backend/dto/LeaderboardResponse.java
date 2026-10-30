package com.learningplatform.backend.dto;

import java.util.List;

// top = the leaderboard rows; me = the current user's own row;
// totalUsers = everyone registered, for "my rank / all users" (e.g. 3/12)
public record LeaderboardResponse(List<LeaderboardEntry> top, LeaderboardEntry me, int totalUsers) {}
