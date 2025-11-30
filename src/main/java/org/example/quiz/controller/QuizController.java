package org.example.quiz.controller;

import lombok.RequiredArgsConstructor;
import org.example.quiz.dto.*;
import org.example.quiz.service.QuizService;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/quiz")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;

    @PostMapping("/start")
    public List<QuizQuestionDto> startQuiz(@RequestBody StartQuizRequest request) {
        return quizService.startQuiz(request.categoryId(), request.questionsCount());
    }

    @PostMapping("/submit")
    public QuizResultDto submitQuiz(@RequestBody SubmitQuizRequest request,
                                    Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }
        return quizService.submitQuiz(request, authentication.getName());
    }

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


    @GetMapping("/leaderboard/global")
    public List<LeaderboardEntry> getGlobalLeaderboard(@RequestParam(defaultValue = "20") int size) {
        return quizService.getGlobalLeaderboard(size);
    }

    @GetMapping("/leaderboard/category/{categoryId}")
    public List<LeaderboardEntry> getCategoryLeaderboard(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "20") int size) {
        return quizService.getCategoryLeaderboard(categoryId, size);
    }
}