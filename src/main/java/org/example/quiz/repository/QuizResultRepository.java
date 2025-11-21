package org.example.quiz.repository;

import org.example.quiz.dto.LeaderboardEntry;
import org.example.quiz.model.QuizResult;
import org.example.quiz.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface QuizResultRepository extends JpaRepository<QuizResult, Long> {

    Page<QuizResult> findByUser(User user, Pageable pageable);
    Page<QuizResult> findByUserAndCategoryId(User user, Long categoryId, Pageable pageable);
    Optional<QuizResult> findByIdAndUser(Long id, User user);

    @Query(value = """
    SELECT 
        ROW_NUMBER() OVER (ORDER BY R.SCORE_PERCENT DESC, R.FINISHED_AT ASC),
        COALESCE(U.DISPLAY_NAME, U.USERNAME),
        R.SCORE_PERCENT,
        R.CORRECT_ANSWERS,
        R.TOTAL_QUESTIONS,
        COALESCE(R.CATEGORY_NAME, 'Mixed'),
        R.FINISHED_AT
    FROM QUIZ_RESULTS R
    JOIN USERS U ON R.USER_ID = U.ID
    ORDER BY R.SCORE_PERCENT DESC, R.FINISHED_AT ASC
    """,
            countQuery = "SELECT COUNT(*) FROM QUIZ_RESULTS",
            nativeQuery = true)
    Page<Object[]> findGlobalLeaderboardNative(Pageable pageable);

    @Query(value = """
    SELECT 
        ROW_NUMBER() OVER (ORDER BY R.SCORE_PERCENT DESC, R.FINISHED_AT ASC),
        COALESCE(U.DISPLAY_NAME, U.USERNAME),
        R.SCORE_PERCENT,
        R.CORRECT_ANSWERS,
        R.TOTAL_QUESTIONS,
        COALESCE(R.CATEGORY_NAME, 'Mixed'),
        R.FINISHED_AT
    FROM QUIZ_RESULTS R
    JOIN USERS U ON R.USER_ID = U.ID
    WHERE R.CATEGORY_ID = :categoryId
    ORDER BY R.SCORE_PERCENT DESC, R.FINISHED_AT ASC
    """,
            countQuery = "SELECT COUNT(*) FROM QUIZ_RESULTS WHERE CATEGORY_ID = :categoryId",
            nativeQuery = true)
    Page<Object[]> findCategoryLeaderboardNative(@Param("categoryId") Long categoryId, Pageable pageable);

    @Query(value = """
    SELECT 
        ROW_NUMBER() OVER (ORDER BY R.SCORE_PERCENT DESC, R.FINISHED_AT ASC),
        COALESCE(U.DISPLAY_NAME, U.USERNAME),
        R.SCORE_PERCENT,
        R.CORRECT_ANSWERS,
        R.TOTAL_QUESTIONS,
        COALESCE(R.CATEGORY_NAME, 'Mixed'),
        R.FINISHED_AT
    FROM QUIZ_RESULTS R
    JOIN USERS U ON R.USER_ID = U.ID
    WHERE R.USER_ID = :userId
    ORDER BY R.SCORE_PERCENT DESC
    LIMIT 1
    """, nativeQuery = true)
    Optional<Object[]> findMyBestResultNative(@Param("userId") Long userId);
}
