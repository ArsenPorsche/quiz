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
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
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
        if (userAnswers.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No answers provided");
        }

        var questionMap = questionRepository.findAllById(
                userAnswers.stream().map(UserAnswer::questionId).toList()
        ).stream().collect(Collectors.toMap(Question::getId, q -> q));

        int correct = 0;
        for (UserAnswer ua : userAnswers) {
            Question q = questionMap.get(ua.questionId());
            if (q != null && q.getCorrectAnswer().equals(ua.selectedAnswer())) {
                correct++;
            }
        }

        int total = userAnswers.size();
        int scorePercent = total > 0 ? (correct * 100) / total : 0;

        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean isRandomQuiz = request.categoryId() == null;
        Long categoryId = isRandomQuiz ? null : request.categoryId();
        String categoryName = isRandomQuiz ? "Random Quiz" : null;

        if (!isRandomQuiz) {
            Question anyQuestion = questionMap.values().stream().findFirst().orElse(null);
            if (anyQuestion != null && anyQuestion.getCategory() != null) {
                categoryName = anyQuestion.getCategory().getName();
            }
        }

        QuizResult result = QuizResult.builder()
                .user(user)
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
                result.getId(),
                categoryName,
                result.getFinishedAt()
        );
    }

    @GetMapping("/results")
    public Page<QuizResultDto> getMyResults(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication auth) {

        User user = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Pageable pageable = PageRequest.of(page, size, Sort.by("finishedAt").descending());
        Page<QuizResult> results = quizResultRepository.findByUser(user, pageable);

        return results.map(r -> new QuizResultDto(
                r.getTotalQuestions(),
                r.getCorrectAnswers(),
                r.getScorePercent(),
                "",
                r.getId(),
                "Random Quiz",
                r.getFinishedAt()
        ));
    }

    @GetMapping("/leaderboard/global")
    public List<LeaderboardEntry> getGlobalLeaderboard(@RequestParam(defaultValue = "20") int size) {
        return quizResultRepository.findGlobalLeaderboardNative(PageRequest.of(0, size))
                .getContent()
                .stream()
                .map(this::toLeaderboardEntry)
                .toList();
    }

    private LeaderboardEntry toLeaderboardEntry(Object[] row) {
        LocalDateTime finishedAt = row[6] instanceof java.sql.Timestamp ts
                ? ts.toLocalDateTime()
                : (LocalDateTime) row[6];

        return new LeaderboardEntry(
                (String) row[1],
                ((Number) row[2]).intValue(),
                ((Number) row[3]).intValue(),
                ((Number) row[4]).intValue(),
                row[5] != null ? (String) row[5] : "Random",
                finishedAt
        );
    }
}