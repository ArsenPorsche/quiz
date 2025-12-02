package org.example.quiz.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.quiz.dto.QuestionRequest;
import org.example.quiz.dto.QuestionResponse;
import org.example.quiz.service.AdminQuestionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.List;

// Kontroler do operacji administracyjnych na pytaniach
@RestController
@RequestMapping("/api/admin/questions")
@RequiredArgsConstructor
public class AdminQuestionController {

    // Serwis z logika pytan
    private final AdminQuestionService questionService;

    // Tworzy nowe pytanie
    @PostMapping
    public ResponseEntity<?> createQuestion(@Valid @RequestBody QuestionRequest request) {
        QuestionResponse created = questionService.createQuestion(request);
        return ResponseEntity.created(URI.create("/api/admin/questions/" + created.id()))
                .body(created);
    }

    // Upload wielu pytan z pliku CSV
    @PostMapping("/upload-csv")
    public ResponseEntity<?> uploadCsv(@RequestParam("file") MultipartFile file) {
        try {
            int count = questionService.uploadCsv(file);
            return ResponseEntity.status(201).body("Inserted: " + count);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("CSV parsing error: " + e.getMessage());
        }
    }

    // Aktualizacja pytania po id
    @PutMapping("/{id}")
    public ResponseEntity<?> update(
            @PathVariable Long id,
            @Valid @RequestBody QuestionRequest request
    ) {
        QuestionResponse updated = questionService.update(id, request);
        if (updated == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(updated);
    }

    // Usuwanie pytania
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        boolean deleted = questionService.delete(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    // Pobranie losowych pytan (podglad)
    @GetMapping("/random")
    public ResponseEntity<List<QuestionResponse>> random(
            @RequestParam(name = "count", required = false, defaultValue = "10") int count
    ) {
        return ResponseEntity.ok(questionService.random(count));
    }

    // Lista pytan opcjonalnie filtrowana po kategorii
    @GetMapping
    public ResponseEntity<List<QuestionResponse>> list(
            @RequestParam(name = "categoryId", required = false) Long categoryId
    ) {
        return ResponseEntity.ok(questionService.list(categoryId));
    }
}
