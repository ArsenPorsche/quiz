package org.example.quiz.util;

import com.opencsv.CSVReaderHeaderAware;
import org.example.quiz.model.Category;
import org.example.quiz.model.Question;
import org.example.quiz.repository.CategoryRepository;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CsvUtils {

    public static List<Question> parseQuestions(MultipartFile file, CategoryRepository categoryRepository) {
        try (Reader reader = new InputStreamReader(file.getInputStream());
             CSVReaderHeaderAware csv = new CSVReaderHeaderAware(reader)) {

            List<Question> result = new ArrayList<>();
            Map<String, String> row;

            while ((row = csv.readMap()) != null) {
                String text = safeTrim(row.getOrDefault("text", row.getOrDefault("question", "")));
                String optionA = safeTrim(row.getOrDefault("optionA", ""));
                String optionB = safeTrim(row.getOrDefault("optionB", ""));
                String optionC = safeTrim(row.getOrDefault("optionC", ""));
                String optionD = safeTrim(row.getOrDefault("optionD", ""));
                String correct = safeTrim(row.getOrDefault("correctAnswer", row.getOrDefault("correct", "")));
                String catIdStr = safeTrim(row.getOrDefault("categoryId", ""));
                String catName = safeTrim(row.getOrDefault("categoryName", row.getOrDefault("category", "")));

                if (text.isEmpty()
                        || optionA.isEmpty()
                        || optionB.isEmpty()
                        || optionC.isEmpty()
                        || optionD.isEmpty()
                        || correct.isEmpty()) {
                    throw new IllegalArgumentException("CSV row missing required fields (text/options/correctAnswer)");
                }

                if (!correct.matches("[A-D]")) {
                    throw new IllegalArgumentException("correctAnswer must be one of A, B, C or D");
                }

                Category category = null;
                if (!catIdStr.isEmpty()) {
                    try {
                        Long catId = Long.parseLong(catIdStr);
                        category = categoryRepository.findById(catId)
                                .orElseThrow(() -> new IllegalArgumentException("Category with id " + catId + " not found"));
                    } catch (NumberFormatException nfe) {
                        throw new IllegalArgumentException("categoryId is not a valid number: " + catIdStr);
                    }
                } else if (!catName.isEmpty()) {
                    Optional<Category> opt = categoryRepository.findAll().stream()
                            .filter(c -> c.getName().equalsIgnoreCase(catName))
                            .findFirst();
                    if (opt.isPresent()) category = opt.get();
                    else throw new IllegalArgumentException("Category with name '" + catName + "' not found");
                } else {
                    throw new IllegalArgumentException("Either categoryId or categoryName must be provided");
                }

                Question q = Question.builder()
                        .text(text)
                        .optionA(optionA)
                        .optionB(optionB)
                        .optionC(optionC)
                        .optionD(optionD)
                        .correctAnswer(correct)
                        .category(category)
                        .build();

                result.add(q);
            }
            return result;
        }
        catch (Exception e) {
            throw new RuntimeException("Failed to parse CSV: " + e.getMessage(), e);
        }


    }

    private static String safeTrim(String s) {
        return s == null ? "" : s.trim();
    }
}