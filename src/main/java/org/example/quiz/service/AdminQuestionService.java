package org.example.quiz.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.quiz.dto.QuestionRequest;
import org.example.quiz.dto.QuestionResponse;
import org.example.quiz.model.Category;
import org.example.quiz.model.Question;
import org.example.quiz.repository.CategoryRepository;
import org.example.quiz.repository.QuestionRepository;
import org.example.quiz.util.CsvUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;

// Serwis administracyjny pytan
@Service
@RequiredArgsConstructor
@Transactional
public class AdminQuestionService {

    // Repo pytan
    private final QuestionRepository questionRepository;
    // Repo kategorii
    private final CategoryRepository categoryRepository;

    // Tworzy nowe pytanie
    public QuestionResponse createQuestion(QuestionRequest request) {
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
        return toResponse(saved);
    }

    // Masowy upload pytan z CSV
    public int uploadCsv(MultipartFile file) {
        List<Question> toSave = CsvUtils.parseQuestions(file, categoryRepository);
        List<Question> saved = questionRepository.saveAll(toSave);
        return saved.size();
    }

    // Aktualizacja istniejacego pytania
    public QuestionResponse update(Long id, QuestionRequest request) {
        return questionRepository.findById(id)
                .map(q -> {
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
                    return toResponse(updated);
                })
                .orElse(null);
    }

    // Usuwa pytanie
    public boolean delete(Long id) {
        if (!questionRepository.existsById(id)) return false;
        questionRepository.deleteById(id);
        return true;
    }

    // Losowe pytania (tasowanie w pamieci)
    public List<QuestionResponse> random(int count) {
        List<Question> all = questionRepository.findAll();
        if (all.isEmpty()) return Collections.emptyList();

        Collections.shuffle(all);
        List<Question> chosen = all.subList(0, Math.min(count, all.size()));

        return chosen.stream().map(this::toResponse).toList();
    }

    // Lista pytan z opcjonalnym filtrem kategorii
    public List<QuestionResponse> list(Long categoryId) {

        List<Question> all = questionRepository.findAll();

        List<Question> filtered = (categoryId == null)
                ? all
                : all.stream()
                .filter(q -> q.getCategory() != null && q.getCategory().getId().equals(categoryId))
                .toList();

        return filtered.stream().map(this::toResponse).toList();
    }

    // Konwersja encji na DTO
    private QuestionResponse toResponse(Question q) {
        return new QuestionResponse(
                q.getId(),
                q.getText(),
                q.getOptionA(),
                q.getOptionB(),
                q.getOptionC(),
                q.getOptionD(),
                String.valueOf(q.getCorrectAnswer()),
                q.getCategory() != null ? q.getCategory().getId() : null,
                q.getCategory() != null ? q.getCategory().getName() : null
        );
    }
}
