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
    public void run(String... args) {
        if (categoryRepository.count() > 0) {
            System.out.println("Database already seeded – skipping.");
            return;
        }

        System.out.println("Seeding initial data...");

        var general = categoryRepository.save(Category.builder().name("General Knowledge").build());
        var java    = categoryRepository.save(Category.builder().name("Java & Programming").build());
        var science = categoryRepository.save(Category.builder().name("Science").build());

        userRepository.saveAll(List.of(
                User.builder().username("arsen").password(passwordEncoder.encode("123"))
                        .displayName("Arsen").role(Role.PLAYER).enabled(true).build(),
                User.builder().username("admin").password(passwordEncoder.encode("admin"))
                        .displayName("Administrator").role(Role.ADMIN).enabled(true).build(),
                User.builder().username("palina").password(passwordEncoder.encode("123"))
                        .displayName("Palina").role(Role.PLAYER).enabled(true).build()
        ));

        questionRepository.saveAll(List.of(
                Question.builder().text("What is the capital of France?").optionA("London").optionB("Paris").optionC("Berlin").optionD("Madrid").correctAnswer("B").category(general).build(),
                Question.builder().text("Who wrote Harry Potter?").optionA("Tolkien").optionB("Rowling").optionC("King").optionD("Orwell").correctAnswer("B").category(general).build(),
                Question.builder().text("2 + 2 × 2 = ?").optionA("8").optionB("6").optionC("4").optionD("10").correctAnswer("B").category(science).build(),
                Question.builder().text("What does JVM stand for?").optionA("Java Visual Machine").optionB("Java Virtual Machine").optionC("Java Variable Machine").optionD("Java Void Machine").correctAnswer("B").category(java).build(),
                Question.builder().text("Largest planet in the Solar System?").optionA("Earth").optionB("Jupiter").optionC("Saturn").optionD("Mars").correctAnswer("B").category(science).build(),
                Question.builder().text("Who created Java?").optionA("James Gosling").optionB("Linus Torvalds").optionC("Dennis Ritchie").optionD("Bjarne Stroustrup").correctAnswer("A").category(java).build(),
                Question.builder().text("How many bits in a byte?").optionA("4").optionB("8").optionC("16").optionD("32").correctAnswer("B").category(java).build(),
                Question.builder().text("Who wrote War and Peace?").optionA("Dostoevsky").optionB("Tolstoy").optionC("Chekhov").optionD("Pushkin").correctAnswer("B").category(general).build(),
                Question.builder().text("How many continents are there?").optionA("5").optionB("6").optionC("7").optionD("8").correctAnswer("C").category(general).build(),
                Question.builder().text("Text data type in Java?").optionA("int").optionB("boolean").optionC("String").optionD("char").correctAnswer("C").category(java).build(),
                Question.builder().text("How many sides does a hexagon have?").optionA("5").optionB("6").optionC("7").optionD("8").correctAnswer("B").category(science).build(),
                Question.builder().text("Who invented the telephone?").optionA("Edison").optionB("Bell").optionC("Tesla").optionD("Marconi").correctAnswer("B").category(general).build(),
                Question.builder().text("Chemical symbol for gold?").optionA("Go").optionB("Gd").optionC("Au").optionD("Ag").correctAnswer("C").category(science).build(),
                Question.builder().text("How many hours in a day?").optionA("22").optionB("24").optionC("26").optionD("28").correctAnswer("B").category(general).build(),
                Question.builder().text("Spring Framework is written in:").optionA("JavaScript").optionB("Python").optionC("Java").optionD("C#").correctAnswer("C").category(java).build()
        ));

        System.out.println("Seed completed! 15 questions, 3 categories, 3 users created.");
        System.out.println("Login → arsen / 123   |  palina  / 123   |   admin / admin");
    }
}