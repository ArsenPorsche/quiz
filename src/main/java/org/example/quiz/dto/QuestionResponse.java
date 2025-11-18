package org.example.quiz.dto;

public record QuestionResponse(
        Long id,
        String text,
        String optionA,
        String optionB,
        String optionC,
        String optionD,
        String correctAnswer,
        Long categoryId,
        String categoryName
) {}