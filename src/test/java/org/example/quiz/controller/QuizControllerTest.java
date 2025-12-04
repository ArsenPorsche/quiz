package org.example.quiz.controller;

import org.example.quiz.dto.QuizQuestionDto;
import org.example.quiz.dto.StartQuizRequest;
import org.example.quiz.service.QuizService;
import org.example.quiz.repository.QuestionRepository;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class QuizControllerTest {

    @Test
    void startQuiz_returnsQuestions() {
        QuizService service = mock(QuizService.class);
        QuestionRepository repo = mock(QuestionRepository.class);

        QuizQuestionDto q = new QuizQuestionDto(1L, "T1", "A", "B", "C", "D");
        when(service.startQuiz(2L, 5)).thenReturn(List.of(q));

        QuizController ctrl = new QuizController(service, repo);

        StartQuizRequest req = new StartQuizRequest(2L, 5);
        List<QuizQuestionDto> result = ctrl.startQuiz(req);

        assertEquals(1, result.size());
        assertEquals("T1", result.get(0).text());

        verify(service, times(1)).startQuiz(2L, 5);
    }
}
