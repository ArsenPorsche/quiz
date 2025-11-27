package org.example.quiz.controller;

import org.example.quiz.dto.CategoryDto;
import org.example.quiz.service.CategoryService;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CategoryControllerTest {

    @Test
    void getAll_returnsDtosWithCounts() {
        CategoryService service = mock(CategoryService.class);

        CategoryDto dto = new CategoryDto(2L, "Math", 5);
        when(service.getAll()).thenReturn(List.of(dto));

        CategoryController ctrl = new CategoryController(service);

        ResponseEntity<List<CategoryDto>> resp = ctrl.getAll();

        assertEquals(200, resp.getStatusCodeValue());
        List<CategoryDto> body = resp.getBody();
        assertNotNull(body);
        assertEquals(1, body.size());
        assertEquals(5, body.get(0).questionCount());
        verify(service, times(1)).getAll();
    }

    @Test
    void getById_found() {
        CategoryService service = mock(CategoryService.class);
        CategoryDto dto = new CategoryDto(3L, "History", 2);
        when(service.getById(3L)).thenReturn(dto);

        CategoryController ctrl = new CategoryController(service);

        ResponseEntity<CategoryDto> resp = ctrl.getById(3L);
        assertEquals(200, resp.getStatusCodeValue());
        assertNotNull(resp.getBody());
        assertEquals(2, resp.getBody().questionCount());
        verify(service, times(1)).getById(3L);
    }

    @Test
    void getById_notFound() {
        CategoryService service = mock(CategoryService.class);
        when(service.getById(99L)).thenReturn(null);

        CategoryController ctrl = new CategoryController(service);

        ResponseEntity<CategoryDto> resp = ctrl.getById(99L);
        assertEquals(404, resp.getStatusCodeValue());
        verify(service, times(1)).getById(99L);
    }
}



