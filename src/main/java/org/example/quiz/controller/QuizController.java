package org.example.quiz.controller;

import lombok.RequiredArgsConstructor;
import org.example.quiz.dto.*;
import org.example.quiz.dto.SubmitQuizRequest.UserAnswer;
import org.example.quiz.model.Question;
import org.example.quiz.model.QuizResult;
import org.example.quiz.model.User;
import org.example.quiz.repository.QuestionRepository;
import org.example.quiz.repository.QuizResultRepository;
import org.example.quiz.repository.UserRepository;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/quiz")
@RequiredArgsConstructor
public class QuizController {

    private final QuestionRepository questionRepository;
    private final QuizResultRepository quizResultRepository;
    private final UserRepository userRepository;


    @PostMapping("/start")
    public List<QuizQuestionDto> startQuiz(@RequestBody StartQuizRequest request) {
        Long categoryId = request.categoryId();
        int count = request.questionsCount() != null ? request.questionsCount() : 10;

        List<Question> questions = categoryId == null
                ? questionRepository.findRandom(count)
                : questionRepository.findRandomByCategoryId(categoryId, count);

        return questions.stream()
                .map(q -> new QuizQuestionDto(
                        q.getId(),
                        q.getText(),
                        q.getOptionA(),
                        q.getOptionB(),
                        q.getOptionC(),
                        q.getOptionD()
                ))
                .toList();
    }

    @PostMapping("/submit")
    public QuizResultDto submitQuiz(@RequestBody SubmitQuizRequest request,
                                    Authentication authentication) {

        var userAnswers = request.answers();

        Set<Long> questionIds = userAnswers.stream()
                .map(UserAnswer::questionId)
                .collect(Collectors.toSet());

        Map<Long, Question> questionMap = questionRepository.findAllById(questionIds)
                .stream()
                .collect(Collectors.toMap(Question::getId, q -> q));

        int correct = 0;
        Question firstQuestion = userAnswers.isEmpty() ? null : questionMap.get(userAnswers.get(0).questionId());
        String categoryName = firstQuestion != null && firstQuestion.getCategory() != null
                ? firstQuestion.getCategory().getName()
                : null;

        for (UserAnswer ua : userAnswers) {
            Question q = questionMap.get(ua.questionId());
            if (q == null) continue;

//            String correctAnswerText = switch (q.getCorrectAnswer()) {
//                case 'A' -> q.getOptionA();
//                case 'B' -> q.getOptionB();
//                case 'C' -> q.getOptionC();
//                case 'D' -> q.getOptionD();
//                default -> null;
//            };

            if ( q.getCorrectAnswer().equals(ua.selectedAnswer())) {
                correct++;
            }
        }

        int total = userAnswers.size();
        int scorePercent = total > 0 ? (correct * 100) / total : 0;

        String username = authentication == null ? null : authentication.getName();

//        Optional<User> userOpt = userRepository.findByUsername(username);
//
//        User user = userOpt.get();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Long categoryId = (firstQuestion != null && firstQuestion.getCategory() != null)
                ? firstQuestion.getCategory().getId()
                : null;

        QuizResult result = QuizResult.builder()
                .user(user)
//                .categoryId(firstQuestion != null ? firstQuestion.getCategory().getId() : null)
                .categoryId(categoryId)
                .categoryName(categoryName)
                .correctAnswers(correct)
                .totalQuestions(total)
                .scorePercent(scorePercent)
                .finishedAt(LocalDateTime.now())
                .build();

        quizResultRepository.save(result);

        return new QuizResultDto(
                total,
                correct,
                scorePercent,
                "Quiz completed successfully!",
                result.getId()
        );
    }
    @GetMapping("/results")
    public Page<QuizResultDto> getMyResults(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long categoryId,
            Authentication auth) {

        String username = auth.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Pageable pageable = PageRequest.of(page, size, Sort.by("finishedAt").descending());

        Page<QuizResult> results = categoryId == null
                ? quizResultRepository.findByUser(user, pageable)
                : quizResultRepository.findByUserAndCategoryId(user, categoryId, pageable);

        return results.map(r -> new QuizResultDto(
                r.getTotalQuestions(),
                r.getCorrectAnswers(),
                r.getScorePercent(),
                "Success",
                r.getId()
        ));
    }

    @GetMapping("/results/{id}")
    public QuizResultDto getResult(@PathVariable Long id, Authentication auth) {
        String username = auth.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        QuizResult result = quizResultRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Result not found"));

        return new QuizResultDto(
                result.getTotalQuestions(),
                result.getCorrectAnswers(),
                result.getScorePercent(),
                "Success",
                result.getId()
        );
    }

    @GetMapping("/leaderboard/global")
    public List<LeaderboardEntry> getGlobalLeaderboard(@RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(0, size);
        return quizResultRepository.findGlobalLeaderboardNative(pageable)
                .getContent()
                .stream()
                .map(this::toLeaderboardEntry)
                .toList();
    }

    @GetMapping("/leaderboard/me")
    public Map<String, Object> getMyPosition(Authentication auth) {
        User user = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Optional<Object[]> best = quizResultRepository.findMyBestResultNative(user.getId());

        if (best.isEmpty()) {
            return Map.of("message", "No results yet");
        }

        LeaderboardEntry entry = toLeaderboardEntry(best.get());
        return Map.of("rank", entry.rank(), "bestResult", entry);
    }

    @GetMapping("/leaderboard/category/{categoryId}")
    public List<LeaderboardEntry> getCategoryLeaderboard(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(0, size);
        return quizResultRepository.findCategoryLeaderboardNative(categoryId, pageable)
                .getContent()
                .stream()
                .map(this::toLeaderboardEntry)
                .toList();
    }

    private LeaderboardEntry toLeaderboardEntry(Object[] row) {
        Object timestamp = row[6];
        LocalDateTime finishedAt = timestamp instanceof java.sql.Timestamp ts
                ? ts.toLocalDateTime()
                : (LocalDateTime) timestamp;

        return new LeaderboardEntry(
                ((Number) row[0]).longValue(),
                (String) row[1],
                ((Number) row[2]).intValue(),
                ((Number) row[3]).intValue(),
                ((Number) row[4]).intValue(),
                (String) row[5],
                finishedAt
        );
    }
}