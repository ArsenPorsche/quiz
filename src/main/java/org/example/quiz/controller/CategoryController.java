package org.example.quiz.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.quiz.dto.CategoryDto;
import org.example.quiz.dto.CategoryRequest;
import org.example.quiz.model.Category;
import org.example.quiz.repository.CategoryRepository;
import org.example.quiz.repository.QuestionRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Validated
public class CategoryController {

    private final CategoryRepository categoryRepository;
    private final QuestionRepository questionRepository;

    @GetMapping
    public ResponseEntity<List<CategoryDto>> getAll() {
        List<CategoryDto> dtos = categoryRepository.findAll()
                .stream()
                .map(c -> new CategoryDto(
                        c.getId(),
                        c.getName(),
                        (int) questionRepository.countByCategoryId(c.getId())
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryDto> getById(@PathVariable Long id) {
        return categoryRepository.findById(id)
                .map(c -> new CategoryDto(c.getId(), c.getName(),
                        (int) questionRepository.countByCategoryId(c.getId())))
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody CategoryRequest request) {
        if (categoryRepository.existsByName(request.name())) {
            return ResponseEntity.badRequest().body("Category with this name already exists");
        }
        Category created = Category.builder()
                .name(request.name())
                .build();
        Category saved = categoryRepository.save(created);
        return ResponseEntity.created(URI.create("/api/categories/" + saved.getId()))
                .body(new CategoryDto(saved.getId(), saved.getName(), 0));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody CategoryRequest request) {
        return categoryRepository.findById(id).map(cat -> {
            cat.setName(request.name());
            Category updated = categoryRepository.save(cat);
            return ResponseEntity.ok(new CategoryDto(updated.getId(), updated.getName(),
                    (int) questionRepository.countByCategoryId(updated.getId())));
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!categoryRepository.existsById(id)) return ResponseEntity.notFound().build();
        categoryRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}