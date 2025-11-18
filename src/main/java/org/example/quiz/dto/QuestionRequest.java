package org.example.quiz.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record QuestionRequest(
        @NotBlank(message = "Question text is required")
        String text,

        @NotBlank(message = "Option A is required")
        String optionA,

        @NotBlank(message = "Option B is required")
        String optionB,

        @NotBlank(message = "Option C is required")
        String optionC,

        @NotBlank(message = "Option D is required")
        String optionD,

        @NotNull(message = "Correct answer is required")
        @Pattern(regexp = "[A-D]", message = "Correct answer must be A, B, C, or D")
        String correctAnswer,

        @NotNull(message = "Category ID is required")
        Long categoryId
) {}