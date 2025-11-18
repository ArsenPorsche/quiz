package org.example.quiz.dto;

public record StartQuizRequest(
        Long categoryId,
        Integer questionsCount
) {}
