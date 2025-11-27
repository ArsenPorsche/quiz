package org.example.quiz.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.quiz.dto.CategoryDto;
import org.example.quiz.dto.CategoryRequest;
import org.example.quiz.model.Category;
import org.example.quiz.repository.CategoryRepository;
import org.example.quiz.repository.QuestionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final QuestionRepository questionRepository;

    public List<CategoryDto> getAll() {
        return categoryRepository.findAll()
                .stream()
                .map(c -> new CategoryDto(
                        c.getId(),
                        c.getName(),
                        (int) questionRepository.countByCategoryId(c.getId())
                ))
                .toList();
    }

    public CategoryDto getById(Long id) {
        return categoryRepository.findById(id)
                .map(c -> new CategoryDto(
                        c.getId(),
                        c.getName(),
                        (int) questionRepository.countByCategoryId(c.getId())
                ))
                .orElse(null);
    }

    public CategoryDto create(CategoryRequest request) {
        if (categoryRepository.existsByName(request.name())) {
            throw new IllegalArgumentException("Category with this name already exists");
        }

        Category created = Category.builder()
                .name(request.name())
                .build();

        Category saved = categoryRepository.save(created);

        return new CategoryDto(saved.getId(), saved.getName(), 0);
    }

    public CategoryDto update(Long id, CategoryRequest request) {
        return categoryRepository.findById(id)
                .map(cat -> {
                    cat.setName(request.name());
                    Category updated = categoryRepository.save(cat);
                    return new CategoryDto(
                            updated.getId(),
                            updated.getName(),
                            (int) questionRepository.countByCategoryId(updated.getId())
                    );
                })
                .orElse(null);
    }

    public boolean delete(Long id) {
        if (!categoryRepository.existsById(id)) return false;
        categoryRepository.deleteById(id);
        return true;
    }
}
