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

@WebMvcTest(QuizController.class)
@AutoConfigureMockMvc(addFilters = false)
class QuizControllerIntegrationTest {

    @Autowired
    MockMvc mvc;

    @MockBean
    QuizService quizService;

    @MockBean
    QuestionRepository questionRepository;

    @Test
    void getQuestionCounts_returnsMap() throws Exception {

        when(questionRepository.count()).thenReturn(20L);
        when(questionRepository.countByCategoryId(1L)).thenReturn(3L);
        when(questionRepository.countByCategoryId(2L)).thenReturn(4L);
        when(questionRepository.countByCategoryId(3L)).thenReturn(5L);
        when(questionRepository.countByCategoryId(4L)).thenReturn(6L);
        when(questionRepository.countByCategoryId(5L)).thenReturn(7L);

        mvc.perform(get("/api/quiz/questions/counts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$['total']").value(20))
                .andExpect(jsonPath("$['1']").value(3))
                .andExpect(jsonPath("$['2']").value(4))
                .andExpect(jsonPath("$['3']").value(5))
                .andExpect(jsonPath("$['4']").value(6))
                .andExpect(jsonPath("$['5']").value(7));

        verify(questionRepository).count();
        verify(questionRepository).countByCategoryId(1L);
    }
}
