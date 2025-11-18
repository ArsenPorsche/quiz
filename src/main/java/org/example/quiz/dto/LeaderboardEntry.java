package org.example.quiz.dto;

import java.time.LocalDateTime;

public record LeaderboardEntry(
        Long rank,
        String displayName,
        int scorePercent,
        int correctAnswers,
        int totalQuestions,
        String categoryName,
        LocalDateTime finishedAt
) {}