package org.example.quiz.controller;

import lombok.RequiredArgsConstructor;
import org.example.quiz.dto.QuestionRequest;
import org.example.quiz.dto.QuestionResponse;
import org.example.quiz.model.Category;
import org.example.quiz.model.Question;
import org.example.quiz.repository.CategoryRepository;
import org.example.quiz.repository.QuestionRepository;
import org.example.quiz.util.CsvUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/questions")
@RequiredArgsConstructor
public class AdminQuestionController {

    private final QuestionRepository questionRepository;
    private final CategoryRepository categoryRepository;

    @PostMapping
    public ResponseEntity<?> createQuestion(@Valid @RequestBody QuestionRequest request) {
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));

        Question q = Question.builder()
                .text(request.text())
                .optionA(request.optionA())
                .optionB(request.optionB())
                .optionC(request.optionC())
                .optionD(request.optionD())
                .correctAnswer(request.correctAnswer())
                .category(category)
                .build();

        Question saved = questionRepository.save(q);
        QuestionResponse resp = new QuestionResponse(
                saved.getId(),
                saved.getText(),
                saved.getOptionA(),
                saved.getOptionB(),
                saved.getOptionC(),
                saved.getOptionD(),
                String.valueOf(saved.getCorrectAnswer()),
                saved.getCategory().getId(),
                saved.getCategory().getName()
        );

        return ResponseEntity.created(URI.create("/api/admin/questions/" + saved.getId())).body(resp);
    }

    @PostMapping("/upload-csv")
    public ResponseEntity<?> uploadCsv(@RequestParam("file") MultipartFile file) {
        try {
            List<Question> toSave = CsvUtils.parseQuestions(file, categoryRepository);
            List<Question> saved = questionRepository.saveAll(toSave);
            return ResponseEntity.status(201).body("Inserted: " + saved.size());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("CSV parsing error: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody QuestionRequest request) {
        return questionRepository.findById(id).map(q -> {
            Category category = categoryRepository.findById(request.categoryId())
                    .orElseThrow(() -> new IllegalArgumentException("Category not found"));
            q.setText(request.text());
            q.setOptionA(request.optionA());
            q.setOptionB(request.optionB());
            q.setOptionC(request.optionC());
            q.setOptionD(request.optionD());
            q.setCorrectAnswer(request.correctAnswer());
            q.setCategory(category);
            Question updated = questionRepository.save(q);
            QuestionResponse resp = new QuestionResponse(
                    updated.getId(),
                    updated.getText(),
                    updated.getOptionA(),
                    updated.getOptionB(),
                    updated.getOptionC(),
                    updated.getOptionD(),
                    String.valueOf(updated.getCorrectAnswer()),
                    updated.getCategory().getId(),
                    updated.getCategory().getName()
            );
            return ResponseEntity.ok(resp);
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!questionRepository.existsById(id)) return ResponseEntity.notFound().build();
        questionRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/random")
    public ResponseEntity<List<QuestionResponse>> random(@RequestParam(name = "count", required = false, defaultValue = "10") int count) {
        List<Question> all = questionRepository.findAll();
        if (all.isEmpty()) return ResponseEntity.ok(Collections.emptyList());
        Collections.shuffle(all);
        List<Question> chosen = all.subList(0, Math.min(count, all.size()));
        List<QuestionResponse> resp = chosen.stream().map(q -> new QuestionResponse(
                q.getId(),
                q.getText(),
                q.getOptionA(),
                q.getOptionB(),
                q.getOptionC(),
                q.getOptionD(),
                String.valueOf(q.getCorrectAnswer()),
                q.getCategory().getId(),
                q.getCategory().getName()
        )).collect(Collectors.toList());
        return ResponseEntity.ok(resp);
    }

    @GetMapping
    public ResponseEntity<List<QuestionResponse>> list(@RequestParam(name = "categoryId", required = false) Long categoryId) {

        List<Question> all = questionRepository.findAll();

        List<Question> filtered = (categoryId == null)
                ? all
                : all.stream()
                .filter(q -> q.getCategory() != null && q.getCategory().getId().equals(categoryId))
                .collect(Collectors.toList());

        List<QuestionResponse> resp = filtered.stream().map(q -> new QuestionResponse(
                q.getId(),
                q.getText(),
                q.getOptionA(),
                q.getOptionB(),
                q.getOptionC(),
                q.getOptionD(),
                String.valueOf(q.getCorrectAnswer()),
                q.getCategory() != null ? q.getCategory().getId() : null,
                q.getCategory() != null ? q.getCategory().getName() : null
        )).collect(Collectors.toList());

        return ResponseEntity.ok(resp);
    }
}