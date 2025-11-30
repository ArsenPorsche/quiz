package org.example.quiz.repository;

import org.example.quiz.model.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    long countByCategoryId(Long categoryId);

    @Query(value = "SELECT * FROM questions WHERE category_id = :categoryId ORDER BY RAND() LIMIT :limit", nativeQuery = true)
    List<Question> findRandomByCategoryId(@Param("categoryId") Long categoryId, @Param("limit") int limit);

    @Query(value = "SELECT * FROM questions ORDER BY RAND() LIMIT :limit", nativeQuery = true)
    List<Question> findRandom(@Param("limit") int limit);
}