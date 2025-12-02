package org.example.quiz.controller;

import org.example.quiz.model.Category;
import org.example.quiz.model.Question;
import org.example.quiz.repository.CategoryRepository;
import org.example.quiz.repository.QuestionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Test integracyjny kontrolera kategorii
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class CategoryControllerIntegrationTest {

    // MockMvc do wykonywania zapytan HTTP
    @Autowired
    MockMvc mockMvc;

    // Repo kategorii
    @Autowired
    CategoryRepository categoryRepository;

    // Repo pytan
    @Autowired
    QuestionRepository questionRepository;

    // Przygotowanie danych przed kazdym testem
    @BeforeEach
    void setUp() {
        questionRepository.deleteAll();
        categoryRepository.deleteAll();

        Category math = categoryRepository.save(Category.builder().name("Math").build());
        Category movies = categoryRepository.save(Category.builder().name("Movies").build());

        questionRepository.save(Question.builder()
                .text("2+2?")
                .optionA("3").optionB("4").optionC("5").optionD("6")
                .correctAnswer("B")
                .category(math)
                .build());

        questionRepository.save(Question.builder()
                .text("3*3?")
                .optionA("6").optionB("7").optionC("8").optionD("9")
                .correctAnswer("D")
                .category(math)
                .build());

        questionRepository.save(Question.builder()
                .text("Film z astronauta?")
                .optionA("Interstellar").optionB("Titanic").optionC("Matrix").optionD("Alien")
                .correctAnswer("A")
                .category(movies)
                .build());
    }

    // Sprawdza liste kategorii z liczbami pytan
    @Test
    @DisplayName("GET /api/categories lista z countami")
    void getAll_returnsCategoryListWithQuestionCounts() throws Exception {
        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Math"))
                .andExpect(jsonPath("$[0].questionCount").value(2))
                .andExpect(jsonPath("$[1].name").value("Movies"))
                .andExpect(jsonPath("$[1].questionCount").value(1));
    }

    // Sprawdza pojedyncza kategorie
    @Test
    @DisplayName("GET /api/categories/{id} pojedyncza kategoria")
    void getById_returnsSingleCategory() throws Exception {
        Long id = categoryRepository.findAll().stream()
                .filter(c -> c.getName().equals("Math"))
                .findFirst().orElseThrow().getId();

        mockMvc.perform(get("/api/categories/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Math"))
                .andExpect(jsonPath("$.questionCount").value(2));
    }

    // Sprawdza blad 404 dla nieistniejacej kategorii
    @Test
    @DisplayName("GET /api/categories/{id} 404 gdy brak")
    void getById_notFound() throws Exception {
        mockMvc.perform(get("/api/categories/99999"))
                .andExpect(status().isNotFound());
    }

    // Sprawdza tworzenie nowej kategorii
    @Test
    @DisplayName("POST /api/categories tworzenie")
    void createCategory_success() throws Exception {
        String body = """
                {
                  "name": "Science"
                }
                """;

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.name").value("Science"))
                .andExpect(jsonPath("$.questionCount").value(0));

        assertThat(categoryRepository.existsByName("Science")).isTrue();
    }

    // Sprawdza duplikat nazwy
    @Test
    @DisplayName("POST /api/categories duplikat 400")
    void createCategory_duplicateName() throws Exception {
        String body = """
                {
                  "name": "Math"
                }
                """;

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }
}
