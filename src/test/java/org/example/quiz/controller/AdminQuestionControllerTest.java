package org.example.quiz.controller;

import org.example.quiz.dto.QuestionRequest;
import org.example.quiz.dto.QuestionResponse;
import org.example.quiz.service.AdminQuestionService;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminQuestionControllerTest {

    @Test
    void createQuestion_success() {
        AdminQuestionService service = mock(AdminQuestionService.class);

        QuestionResponse response = new QuestionResponse(
                10L, "Q", "A", "B", "C", "D", "A", 1L, "General"
        );
        when(service.createQuestion(any(QuestionRequest.class))).thenReturn(response);

        AdminQuestionController ctrl = new AdminQuestionController(service);

        QuestionRequest req = mock(QuestionRequest.class);
        ResponseEntity<?> resp = ctrl.createQuestion(req);

        assertEquals(201, resp.getStatusCodeValue());
        assertTrue(resp.getBody() instanceof QuestionResponse);
        QuestionResponse qr = (QuestionResponse) resp.getBody();
        assertEquals(10L, qr.id());
        assertEquals("Q", qr.text());
        verify(service, times(1)).createQuestion(req);
    }

    @Test
    void update_success() {
        AdminQuestionService service = mock(AdminQuestionService.class);

        QuestionResponse updated = new QuestionResponse(
                5L, "Updated Q", "A", "B", "C", "D", "B", 1L, "General"
        );
        when(service.update(eq(5L), any(QuestionRequest.class))).thenReturn(updated);

        AdminQuestionController ctrl = new AdminQuestionController(service);

        QuestionRequest req = mock(QuestionRequest.class);
        ResponseEntity<?> resp = ctrl.update(5L, req);

        assertEquals(200, resp.getStatusCodeValue());
        assertEquals(updated, resp.getBody());
        verify(service, times(1)).update(5L, req);
    }

    @Test
    void update_notFound_returns404() {
        AdminQuestionService service = mock(AdminQuestionService.class);
        when(service.update(eq(5L), any(QuestionRequest.class))).thenReturn(null);

        AdminQuestionController ctrl = new AdminQuestionController(service);
        QuestionRequest req = mock(QuestionRequest.class);
        ResponseEntity<?> resp = ctrl.update(5L, req);

        assertEquals(404, resp.getStatusCodeValue());
        verify(service, times(1)).update(5L, req);
    }

    @Test
    void delete_existing_deletesAndReturnsNoContent() {
        AdminQuestionService service = mock(AdminQuestionService.class);
        when(service.delete(3L)).thenReturn(true);

        AdminQuestionController ctrl = new AdminQuestionController(service);

        ResponseEntity<Void> resp = ctrl.delete(3L);

        assertEquals(204, resp.getStatusCodeValue());
        verify(service, times(1)).delete(3L);
    }

    @Test
    void delete_notFound_returns404() {
        AdminQuestionService service = mock(AdminQuestionService.class);
        when(service.delete(3L)).thenReturn(false);

        AdminQuestionController ctrl = new AdminQuestionController(service);

        ResponseEntity<Void> resp = ctrl.delete(3L);

        assertEquals(404, resp.getStatusCodeValue());
        verify(service, times(1)).delete(3L);
    }

    @Test
    void uploadCsv_validFile_savesAll() throws Exception {
        AdminQuestionService service = mock(AdminQuestionService.class);
        MockMultipartFile file = new MockMultipartFile("file", "q.csv", "text/csv",
                "text,optionA,optionB,optionC,optionD,correctAnswer,categoryId\nCsvQ,A,B,C,D,A,10".getBytes());

        when(service.uploadCsv(file)).thenReturn(1);

        AdminQuestionController ctrl = new AdminQuestionController(service);

        ResponseEntity<?> resp = ctrl.uploadCsv(file);

        assertEquals(201, resp.getStatusCodeValue());
        assertTrue(resp.getBody().toString().startsWith("Inserted: "));
        verify(service, times(1)).uploadCsv(file);
    }

    @Test
    void random_returnsList() {
        AdminQuestionService service = mock(AdminQuestionService.class);
        List<QuestionResponse> questions = List.of(
                new QuestionResponse(1L,"Q1","A","B","C","D","A",1L,"General")
        );
        when(service.random(10)).thenReturn(questions);

        AdminQuestionController ctrl = new AdminQuestionController(service);

        ResponseEntity<List<QuestionResponse>> resp = ctrl.random(10);

        assertEquals(200, resp.getStatusCodeValue());
        assertEquals(questions, resp.getBody());
        verify(service, times(1)).random(10);
    }

    @Test
    void list_returnsFiltered() {
        AdminQuestionService service = mock(AdminQuestionService.class);
        List<QuestionResponse> questions = List.of(
                new QuestionResponse(1L,"Q1","A","B","C","D","A",1L,"General")
        );
        when(service.list(1L)).thenReturn(questions);

        AdminQuestionController ctrl = new AdminQuestionController(service);

        ResponseEntity<List<QuestionResponse>> resp = ctrl.list(1L);

        assertEquals(200, resp.getStatusCodeValue());
        assertEquals(questions, resp.getBody());
        verify(service, times(1)).list(1L);
    }
}
