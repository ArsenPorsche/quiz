package org.example.quiz.dto;

public record QuizResultDto(
        int totalQuestions,
        int correctAnswers,
        int scorePercent,
        String message,
        Long savedResultId
) {}
