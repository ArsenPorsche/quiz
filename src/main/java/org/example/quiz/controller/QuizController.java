package org.example.quiz.controller;

import lombok.RequiredArgsConstructor;
import org.example.quiz.dto.QuizQuestionDto;
import org.example.quiz.dto.QuizResultDto;
import org.example.quiz.dto.StartQuizRequest;
import org.example.quiz.dto.SubmitQuizRequest;
import org.example.quiz.dto.SubmitQuizRequest.UserAnswer;
import org.example.quiz.model.Question;
import org.example.quiz.model.QuizResult;
import org.example.quiz.model.User;
import org.example.quiz.repository.QuestionRepository;
import org.example.quiz.repository.QuizResultRepository;
import org.example.quiz.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/quiz")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('PLAYER', 'ADMIN')")
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

        Optional<User> userOpt = userRepository.findByUsername(username);

        User user = userOpt.get();



        QuizResult result = QuizResult.builder()
                .user(user)
                .categoryId(firstQuestion != null ? firstQuestion.getCategory().getId() : null)
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
}