package org.example.quiz.repository;

import org.example.quiz.model.QuizResult;
import org.example.quiz.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
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
            WHERE R.FINISHED_AT >= :since
            ORDER BY R.SCORE_PERCENT DESC, R.FINISHED_AT ASC
            LIMIT :limit
            """, nativeQuery = true)
    List<Object[]> findGlobalLeaderboardSince(@Param("since") LocalDateTime since, @Param("limit") int limit);

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
            WHERE R.CATEGORY_ID = :categoryId AND R.FINISHED_AT >= :since
            ORDER BY R.SCORE_PERCENT DESC, R.FINISHED_AT ASC
            LIMIT :limit
            """, nativeQuery = true)
    List<Object[]> findCategoryLeaderboardSince(
            @Param("categoryId") Long categoryId,
            @Param("since") LocalDateTime since,
            @Param("limit") int limit);

    @Query(value = """
            SELECT 
                ROW_NUMBER() OVER (ORDER BY R.FINISHED_AT DESC),
                COALESCE(U.DISPLAY_NAME, U.USERNAME),
                R.SCORE_PERCENT,
                R.CORRECT_ANSWERS,
                R.TOTAL_QUESTIONS,
                COALESCE(R.CATEGORY_NAME, 'Mixed'),
                R.FINISHED_AT
            FROM QUIZ_RESULTS R
            JOIN USERS U ON R.USER_ID = U.ID
            WHERE R.SCORE_PERCENT >= 80
            ORDER BY R.FINISHED_AT DESC
            """, nativeQuery = true)
    List<Object[]> findRecentBestResults(Pageable pageable);

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
            WHERE R.USER_ID = :userId AND R.CATEGORY_ID = :categoryId
            ORDER BY R.SCORE_PERCENT DESC
            LIMIT 1
            """, nativeQuery = true)
    Optional<Object[]> findMyBestResultByCategory(@Param("userId") Long userId, @Param("categoryId") Long categoryId);

    @Query(value = """
            SELECT COUNT(*) + 1 FROM (
                SELECT DISTINCT ON (USER_ID) USER_ID, SCORE_PERCENT
                FROM QUIZ_RESULTS
                ORDER BY USER_ID, SCORE_PERCENT DESC
            ) AS best_scores
            WHERE SCORE_PERCENT > (
                SELECT MAX(SCORE_PERCENT) FROM QUIZ_RESULTS WHERE USER_ID = :userId
            )
            """, nativeQuery = true)
    Long findMyGlobalRank(@Param("userId") Long userId);

    @Query(value = """
            SELECT COUNT(*) + 1 FROM (
                SELECT DISTINCT ON (USER_ID) USER_ID, SCORE_PERCENT
                FROM QUIZ_RESULTS
                WHERE CATEGORY_ID = :categoryId
                ORDER BY USER_ID, SCORE_PERCENT DESC
            ) AS best_scores
            WHERE SCORE_PERCENT > (
                SELECT MAX(SCORE_PERCENT) FROM QUIZ_RESULTS 
                WHERE USER_ID = :userId AND CATEGORY_ID = :categoryId
            )
            """, nativeQuery = true)
    Long findMyCategoryRank(@Param("userId") Long userId, @Param("categoryId") Long categoryId);

    @Query(value = """
            SELECT 
                0 as rank,
                COALESCE(U.DISPLAY_NAME, U.USERNAME),
                R.SCORE_PERCENT,
                R.CORRECT_ANSWERS,
                R.TOTAL_QUESTIONS,
                COALESCE(R.CATEGORY_NAME, 'Mixed'),
                R.FINISHED_AT
            FROM QUIZ_RESULTS R
            JOIN USERS U ON R.USER_ID = U.ID
            WHERE R.USER_ID = :userId
                AND (:categoryId IS NULL OR R.CATEGORY_ID = :categoryId)
            ORDER BY R.FINISHED_AT DESC
            """, nativeQuery = true)
    List<Object[]> findMyRecentResults(
            @Param("userId") Long userId,
            @Param("categoryId") Long categoryId,
            Pageable pageable);
}