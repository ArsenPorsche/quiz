package org.example.quiz.dto;

public record QuizQuestionDto(
        Long id,
        String text,
        String optionA,
        String optionB,
        String optionC,
        String optionD
) {}