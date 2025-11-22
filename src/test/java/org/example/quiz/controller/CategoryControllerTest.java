package org.example.quiz.controller;

import org.example.quiz.dto.CategoryDto;
import org.example.quiz.model.Category;
import org.example.quiz.repository.CategoryRepository;
import org.example.quiz.repository.QuestionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CategoryControllerTest {

    @Test
    void getAll_returnsDtosWithCounts() {
        CategoryRepository catRepo = mock(CategoryRepository.class);
        QuestionRepository qRepo = mock(QuestionRepository.class);

        Category c = Category.builder().id(2L).name("Math").build();
        when(catRepo.findAll()).thenReturn(List.of(c));
        when(qRepo.countByCategoryId(2L)).thenReturn(5L);

        CategoryController ctrl = new CategoryController(catRepo, qRepo);

        ResponseEntity<List<CategoryDto>> resp = ctrl.getAll();

        assertEquals(200, resp.getStatusCodeValue());
        List<CategoryDto> body = resp.getBody();
        assertNotNull(body);
        assertEquals(1, body.size());
        assertEquals(5, body.get(0).questionCount());
    }

    @Test
    void getById_foundAndNotFound() {
        CategoryRepository catRepo = mock(CategoryRepository.class);
        QuestionRepository qRepo = mock(QuestionRepository.class);

        Category c = Category.builder().id(3L).name("History").build();
        when(catRepo.findById(3L)).thenReturn(Optional.of(c));
        when(qRepo.countByCategoryId(3L)).thenReturn(2L);

        CategoryController ctrl = new CategoryController(catRepo, qRepo);

        ResponseEntity<CategoryDto> found = ctrl.getById(3L);
        assertEquals(200, found.getStatusCodeValue());
        assertNotNull(found.getBody());
        assertEquals(2, found.getBody().questionCount());

        when(catRepo.findById(99L)).thenReturn(Optional.empty());
        ResponseEntity<CategoryDto> notFound = ctrl.getById(99L);
        assertEquals(404, notFound.getStatusCodeValue());
    }
}