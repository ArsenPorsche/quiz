package org.example.quiz.dto;

import java.time.LocalDateTime;

public record QuizResultDto(
        int totalQuestions,
        int correctAnswers,
        int scorePercent,
        String message,
        Long savedResultId,
        String categoryName,
        LocalDateTime finishedAt
) {}
