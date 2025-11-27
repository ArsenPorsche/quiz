package org.example.quiz.repository;

import org.example.quiz.model.QuizResult;
import org.example.quiz.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface QuizResultRepository extends JpaRepository<QuizResult, Long> {

    Page<QuizResult> findByUser(User user, Pageable pageable);

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
}