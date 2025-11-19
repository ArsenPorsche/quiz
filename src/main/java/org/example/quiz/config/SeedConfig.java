package org.example.quiz.config;

import lombok.RequiredArgsConstructor;
import org.example.quiz.model.Category;
import org.example.quiz.model.Question;
import org.example.quiz.model.User;
import org.example.quiz.model.Role;
import org.example.quiz.repository.CategoryRepository;
import org.example.quiz.repository.QuestionRepository;
import org.example.quiz.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SeedConfig implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final QuestionRepository questionRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() == 0) {
            Category general = Category.builder().name("General Knowledge").build();
            Category tech = Category.builder().name("Technology").build();
            categoryRepository.saveAll(List.of(general, tech));

            User admin = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin"))
                    .displayName("Administrator")
                    .role(Role.ADMIN)
                    .enabled(true)
                    .build();

            User player = User.builder()
                    .username("player")
                    .password(passwordEncoder.encode("password"))
                    .displayName("Player")
                    .role(Role.PLAYER)
                    .enabled(true)
                    .build();

            userRepository.saveAll(List.of(admin, player));

            Question q1 = Question.builder()
                    .text("What is the capital of France?")
                    .optionA("London")
                    .optionB("Paris")
                    .optionC("Berlin")
                    .optionD("Madrid")
                    .correctAnswer('B')
                    .category(general)
                    .build();

            Question q2 = Question.builder()
                    .text("What is Java primarily used for?")
                    .optionA("Drinking")
                    .optionB("Programming")
                    .optionC("Driving")
                    .optionD("Painting")
                    .correctAnswer('B')
                    .category(tech)
                    .build();

            Question q3 = Question.builder()
                    .text("Who wrote 'Romeo and Juliet'?")
                    .optionA("Charles Dickens")
                    .optionB("William Shakespeare")
                    .optionC("Jane Austen")
                    .optionD("Mark Twain")
                    .correctAnswer('B')
                    .category(general)
                    .build();

            questionRepository.saveAll(List.of(q1, q2, q3));

            System.out.println("Seed data created successfully!");
            System.out.println("Login as: player / password");
            System.out.println("Login as: admin / admin");
        }
    }
}