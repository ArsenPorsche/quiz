package org.example.quiz.service;

import lombok.RequiredArgsConstructor;
import org.example.quiz.dto.*;
import org.example.quiz.model.Question;
import org.example.quiz.model.QuizResult;
import org.example.quiz.model.User;
import org.example.quiz.repository.QuestionRepository;
import org.example.quiz.repository.QuizResultRepository;
import org.example.quiz.repository.UserRepository;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuizService {

    private final QuestionRepository questionRepository;
    private final QuizResultRepository quizResultRepository;
    private final UserRepository userRepository;

    public List<QuizQuestionDto> startQuiz(Long categoryId, Integer questionsCount) {
        int count = questionsCount != null ? questionsCount : 10;

        List<Question> questions = categoryId == null
                ? questionRepository.findRandom(count)// losowy quiz
                : questionRepository.findRandomByCategoryId(categoryId, count);// z konkretnej kategorii

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

    @Transactional
    public QuizResultDto submitQuiz(SubmitQuizRequest request, String username) {
        var userAnswers = request.answers();
        if (userAnswers.isEmpty()) {
            throw new IllegalArgumentException("No answers provided");
        }


        var questionMap = questionRepository.findAllById(
                userAnswers.stream().map(SubmitQuizRequest.UserAnswer::questionId).toList()
        ).stream().collect(java.util.stream.Collectors.toMap(Question::getId, q -> q));

        int correct = 0;
        for (var ua : userAnswers) {
            Question q = questionMap.get(ua.questionId());
            if (q != null && q.getCorrectAnswer().equals(ua.selectedAnswer())) {
                correct++;
            }
        }

        int total = userAnswers.size();
        int scorePercent = total > 0 ? (correct * 100) / total : 0;

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String categoryName = "Random Quiz";
        Long categoryId = null;


        if (request.categoryId() != null) {
            categoryId = request.categoryId();
            categoryName = questionMap.values().stream()
                    .findFirst()
                    .flatMap(q -> java.util.Optional.ofNullable(q.getCategory()))
                    .map(c -> c.getName())
                    .orElse("Unknown Category");
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

    public Page<QuizResultDto> getMyResults(String username, int page, int size) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Pageable pageable = PageRequest.of(page, size, Sort.by("finishedAt").descending());
        Page<QuizResult> results = quizResultRepository.findByUser(user, pageable);

        return results.map(r -> new QuizResultDto(
                r.getTotalQuestions(),
                r.getCorrectAnswers(),
                r.getScorePercent(),
                "",
                r.getId(),
                r.getCategoryName() != null ? r.getCategoryName() : "Random Quiz",
                r.getFinishedAt()
        ));
    }

    public List<LeaderboardEntry> getGlobalLeaderboard(int size) {
        return quizResultRepository.findGlobalLeaderboardNative(PageRequest.of(0, size))
                .getContent()
                .stream()
                .map(this::toLeaderboardEntry)
                .toList();
    }

    public List<LeaderboardEntry> getCategoryLeaderboard(Long categoryId, int size) {
        return quizResultRepository.findCategoryLeaderboardNative(categoryId, PageRequest.of(0, size))
                .getContent()
                .stream()
                .map(this::toLeaderboardEntry)
                .toList();
    }

    private LeaderboardEntry toLeaderboardEntry(Object[] row) {
        LocalDateTime finishedAt = row[6] instanceof java.sql.Timestamp ts
                ? ts.toLocalDateTime()
                : (LocalDateTime) row[6];

        String category = row[5] != null ? (String) row[5] : "Random Quiz";

        return new LeaderboardEntry(
                (String) row[1],
                ((Number) row[2]).intValue(),
                ((Number) row[3]).intValue(),
                ((Number) row[4]).intValue(),
                category,
                finishedAt
        );
    }
}