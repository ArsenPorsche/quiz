package org.example.quiz.dto;

import java.time.LocalDateTime;

public record LeaderboardEntry(
        String displayName,
        int scorePercent,
        int correctAnswers,
        int totalQuestions,
        String categoryName,
        LocalDateTime finishedAt
) {}