package org.example.quiz.dto;

import java.util.List;

public record SubmitQuizRequest(
        List<UserAnswer> answers,
        Long categoryId
) {
    public record UserAnswer(
            Long questionId,
            String selectedAnswer
    ) {}
}