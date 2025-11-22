package org.example.quiz.controller;

import org.example.quiz.dto.QuestionRequest;
import org.example.quiz.dto.QuestionResponse;
import org.example.quiz.model.Category;
import org.example.quiz.model.Question;
import org.example.quiz.repository.CategoryRepository;
import org.example.quiz.repository.QuestionRepository;
import org.junit.jupiter.api.Test;

import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminQuestionControllerTest {

    @Test
    void createQuestion_success() {
        QuestionRepository questionRepository = mock(QuestionRepository.class);
        CategoryRepository categoryRepository = mock(CategoryRepository.class);

        Category cat = Category.builder().id(1L).name("General").build();
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(cat));

        Question saved = Question.builder()
                .id(10L)
                .text("Q")
                .optionA("A")
                .optionB("B")
                .optionC("C")
                .optionD("D")
                .correctAnswer("A")
                .category(cat)
                .build();
        when(questionRepository.save(any(Question.class))).thenReturn(saved);

        AdminQuestionController ctrl = new AdminQuestionController(questionRepository, categoryRepository);

        QuestionRequest req = mock(QuestionRequest.class);
        when(req.categoryId()).thenReturn(1L);
        when(req.text()).thenReturn("Q");
        when(req.optionA()).thenReturn("A");
        when(req.optionB()).thenReturn("B");
        when(req.optionC()).thenReturn("C");
        when(req.optionD()).thenReturn("D");
        when(req.correctAnswer()).thenReturn("A");

        ResponseEntity<?> resp = ctrl.createQuestion(req);

        assertEquals(201, resp.getStatusCodeValue());
        assertTrue(resp.getBody() instanceof QuestionResponse);
        QuestionResponse qr = (QuestionResponse) resp.getBody();
        assertEquals(10L, qr.id());
        assertEquals("Q", qr.text());
        verify(questionRepository, times(1)).save(any(Question.class));
    }

    @Test
    void update_notFound_returns404() {
        QuestionRepository questionRepository = mock(QuestionRepository.class);
        CategoryRepository categoryRepository = mock(CategoryRepository.class);

        when(questionRepository.findById(5L)).thenReturn(Optional.empty());

        AdminQuestionController ctrl = new AdminQuestionController(questionRepository, categoryRepository);

        QuestionRequest req = mock(QuestionRequest.class);
        ResponseEntity<?> resp = ctrl.update(5L, req);

        assertEquals(404, resp.getStatusCodeValue());
        verify(questionRepository, never()).save(any());
    }

    @Test
    void delete_existing_deletesAndReturnsNoContent() {
        QuestionRepository questionRepository = mock(QuestionRepository.class);
        CategoryRepository categoryRepository = mock(CategoryRepository.class);

        when(questionRepository.existsById(3L)).thenReturn(true);

        AdminQuestionController ctrl = new AdminQuestionController(questionRepository, categoryRepository);

        ResponseEntity<Void> resp = ctrl.delete(3L);

        assertEquals(204, resp.getStatusCodeValue());
        verify(questionRepository, times(1)).deleteById(3L);
    }

    @Test
    void uploadCsv_validFile_savesAll() throws Exception {
        QuestionRepository questionRepository = mock(QuestionRepository.class);
        CategoryRepository categoryRepository = mock(CategoryRepository.class);

        Category cat = Category.builder().id(10L).name("CSV").build();
        when(categoryRepository.findById(10L)).thenReturn(Optional.of(cat));

        String csv = "text,optionA,optionB,optionC,optionD,correctAnswer,categoryId\n" +
                "CsvQ,A,B,C,D,A,10\n";
        MockMultipartFile file = new MockMultipartFile("file", "q.csv", "text/csv", csv.getBytes());

        when(questionRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        AdminQuestionController ctrl = new AdminQuestionController(questionRepository, categoryRepository);

        ResponseEntity<?> resp = ctrl.uploadCsv(file);

        assertEquals(201, resp.getStatusCodeValue());
        assertTrue(resp.getBody().toString().startsWith("Inserted: "));
        verify(questionRepository, times(1)).saveAll(anyList());
    }
}