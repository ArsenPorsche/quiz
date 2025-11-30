package org.example.quiz.controller;

import org.example.quiz.repository.QuestionRepository;
import org.example.quiz.service.QuizService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Testuje warstwę web tylko dla QuizController
@WebMvcTest(QuizController.class)
// Wyłącza filtry Spring Security, żeby test nie zwracał 401
@AutoConfigureMockMvc(addFilters = false)
class QuizControllerIntegrationTest {

    @Autowired
    MockMvc mvc;

    // Tworzy mock serwisu QuizService
    @MockBean
    QuizService quizService;

    // Tworzy mok repozytorium QuestionRepository
    @MockBean
    QuestionRepository questionRepository;

    @Test
    void getQuestionCounts_returnsMap() throws Exception {

        // Ustawienie danych zwracanych przez mocki
        when(questionRepository.count()).thenReturn(20L);
        when(questionRepository.countByCategoryId(1L)).thenReturn(3L);
        when(questionRepository.countByCategoryId(2L)).thenReturn(4L);
        when(questionRepository.countByCategoryId(3L)).thenReturn(5L);
        when(questionRepository.countByCategoryId(4L)).thenReturn(6L);
        when(questionRepository.countByCategoryId(5L)).thenReturn(7L);

        // Wykonanie GET i oczekiwanie poprawnej odpowiedzi JSON
        mvc.perform(get("/api/quiz/questions/counts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$['total']").value(20))
                .andExpect(jsonPath("$['1']").value(3))
                .andExpect(jsonPath("$['2']").value(4))
                .andExpect(jsonPath("$['3']").value(5))
                .andExpect(jsonPath("$['4']").value(6))
                .andExpect(jsonPath("$['5']").value(7));

        // Sprawdzenie, czy metody repozytorium zostały wywołane
        verify(questionRepository).count();
        verify(questionRepository).countByCategoryId(1L);
    }
}
