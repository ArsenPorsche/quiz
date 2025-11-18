package org.example.quiz.dto;

import java.util.List;

public record SubmitQuizRequest(
        List<UserAnswer> answers
) {
    public record UserAnswer(
            Long questionId,
            String selectedAnswer
    ) {}
}
