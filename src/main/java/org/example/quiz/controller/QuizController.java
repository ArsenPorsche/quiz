package org.example.quiz.controller;

import lombok.RequiredArgsConstructor;
import org.example.quiz.dto.*;
import org.example.quiz.repository.QuestionRepository;
import org.example.quiz.service.QuizService;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/quiz")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;
    private final QuestionRepository questionRepository;

    // Rozpoczęcie quizu – zwraca listę pytań
    @PostMapping("/start")
    public List<QuizQuestionDto> startQuiz(@RequestBody StartQuizRequest request) {
        return quizService.startQuiz(request.categoryId(), request.questionsCount());
    }

    // Zakończenie quizu – przyjmuje odpowiedzi i zapisuje wynik
    @PostMapping("/submit")
    public QuizResultDto submitQuiz(@RequestBody SubmitQuizRequest request,
                                    Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }
        return quizService.submitQuiz(request, authentication.getName());
    }

    // Moje wyniki – tylko dla zalogowanego gracza
    @GetMapping("/results")
    public Page<QuizResultDto> getMyResults(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication auth) {

        if (auth == null || auth.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        return quizService.getMyResults(auth.getName(), page, size);
    }

    // Globalny ranking – wszystkie kategorie razem
    @GetMapping("/leaderboard/global")
    public List<LeaderboardEntry> getGlobalLeaderboard(@RequestParam(defaultValue = "20") int size) {
        return quizService.getGlobalLeaderboard(size);
    }

    // Ranking w konkretnej kategorii
    @GetMapping("/leaderboard/category/{categoryId}")
    public List<LeaderboardEntry> getCategoryLeaderboard(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "20") int size) {
        return quizService.getCategoryLeaderboard(categoryId, size);
    }

    // Ilość pytań w każdej kategorii + ogółem (do wyboru liczby pytań)
    @GetMapping("/questions/counts")
    public ResponseEntity<Map<String, Integer>> getQuestionCounts() {
        Map<String, Integer> counts = new HashMap<>();

        counts.put("total", (int) questionRepository.count());

        for (int i = 1; i <= 5; i++) {
            int count = (int) questionRepository.countByCategoryId((long) i);
            counts.put(String.valueOf(i), count);
        }

        return ResponseEntity.ok(counts);
    }
}