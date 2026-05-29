package com.studgrasp.api.domain.flashcard;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FlashcardAttemptRepository extends JpaRepository<FlashcardAttempt, UUID> {

    Optional<FlashcardAttempt> findByFlashcardIdAndUserId(UUID flashcardId, UUID userId);

    @Query("SELECT fa FROM FlashcardAttempt fa " +
           "JOIN FETCH fa.flashcard " +
           "WHERE fa.user.id = :userId AND fa.nextReviewAt <= :now " +
           "ORDER BY fa.nextReviewAt ASC")
    List<FlashcardAttempt> findDueForReview(@Param("userId") UUID userId,
                                            @Param("now") LocalDateTime now);

    @Query(value = """
            SELECT rn.id::text        AS nodeId,
                   rn.title           AS nodeTitle,
                   COUNT(fa.id)       AS totalAttempts,
                   SUM(CASE WHEN fa.correct = false THEN 1 ELSE 0 END) AS wrongAttempts
            FROM flashcard_attempts fa
            JOIN flashcards f ON fa.flashcard_id = f.id
            JOIN roadmap_nodes rn ON f.node_id = rn.id
            WHERE fa.user_id = :userId
            GROUP BY rn.id, rn.title
            HAVING COUNT(fa.id) > 0
            ORDER BY (SUM(CASE WHEN fa.correct = false THEN 1 ELSE 0 END)::float / COUNT(fa.id)) DESC
            """, nativeQuery = true)
    List<Object[]> findWeakTopicsByUserId(@Param("userId") UUID userId);

    @Query("SELECT fa FROM FlashcardAttempt fa " +
           "JOIN FETCH fa.flashcard " +
           "WHERE fa.user.id = :userId AND fa.flashcard.id = :flashcardId " +
           "ORDER BY fa.answeredAt DESC")
    Optional<FlashcardAttempt> findLatestByUserAndFlashcard(@Param("userId") UUID userId,
                                                            @Param("flashcardId") UUID flashcardId);

    @Query(value = """
            SELECT
                COUNT(CASE WHEN DATE(fa.answered_at) = CURRENT_DATE THEN 1 END)
                    AS reviewed_today,
                COUNT(CASE WHEN DATE(fa.answered_at) = CURRENT_DATE
                                AND fa.quality >= 3 THEN 1 END)
                    AS correct_today,
                COUNT(DISTINCT CASE WHEN fa.next_review_at <= NOW()
                                    THEN fa.flashcard_id END)
                    AS due_now,
                MIN(CASE WHEN fa.next_review_at > NOW()
                         THEN fa.next_review_at END)
                    AS next_review_at,
                COUNT(DISTINCT CASE WHEN fa.repetitions >= 2
                                    THEN fa.flashcard_id END)
                    AS total_mastered,
                CASE WHEN COUNT(*) FILTER (
                          WHERE fa.answered_at >= NOW() - INTERVAL '30 days') > 0
                     THEN COUNT(*) FILTER (
                              WHERE fa.answered_at >= NOW() - INTERVAL '30 days'
                                AND fa.quality >= 3)::float
                          / COUNT(*) FILTER (
                              WHERE fa.answered_at >= NOW() - INTERVAL '30 days')
                     ELSE NULL
                END AS retention_rate
            FROM flashcard_attempts fa
            WHERE fa.user_id = :userId
            """, nativeQuery = true)
    Object[] findRawStatsByUserId(@Param("userId") UUID userId);

    @Query(value = """
            SELECT DISTINCT DATE(fa.answered_at) AS activity_date
            FROM flashcard_attempts fa
            WHERE fa.user_id = :userId
              AND fa.answered_at >= CURRENT_DATE - INTERVAL '365 days'
            ORDER BY activity_date DESC
            """, nativeQuery = true)
    List<java.sql.Date> findActivityDatesByUserId(@Param("userId") UUID userId);

    @Query(value = """
            SELECT
                u.id::text                      AS user_id,
                u.name                          AS user_name,
                COUNT(CASE WHEN DATE(fa.answered_at) = CURRENT_DATE THEN 1 END)
                                                AS reviewed_today,
                CASE WHEN COUNT(fa.id) FILTER (
                          WHERE fa.answered_at >= NOW() - INTERVAL '30 days') > 0
                     THEN COUNT(fa.id) FILTER (
                              WHERE fa.answered_at >= NOW() - INTERVAL '30 days'
                                AND fa.quality >= 3)::float
                          / COUNT(fa.id) FILTER (
                              WHERE fa.answered_at >= NOW() - INTERVAL '30 days')
                     ELSE NULL
                END                             AS retention_rate
            FROM class_members cm
            JOIN users u ON cm.user_id = u.id
            LEFT JOIN flashcard_attempts fa ON fa.user_id = u.id
            WHERE cm.class_id = :classId
            GROUP BY u.id, u.name
            ORDER BY retention_rate DESC NULLS LAST
            """, nativeQuery = true)
    List<Object[]> findStudentStatsByClassId(@Param("classId") UUID classId);

    @Query(value = """
            SELECT rn.id::text        AS nodeId,
                   rn.title           AS nodeTitle,
                   COUNT(fa.id)       AS totalAttempts,
                   SUM(CASE WHEN fa.correct = false THEN 1 ELSE 0 END) AS wrongAttempts
            FROM flashcard_attempts fa
            JOIN flashcards f ON fa.flashcard_id = f.id
            JOIN roadmap_nodes rn ON f.node_id = rn.id
            WHERE fa.user_id IN (
                SELECT cm.user_id FROM class_members cm WHERE cm.class_id = :classId
            )
            GROUP BY rn.id, rn.title
            HAVING COUNT(fa.id) > 0
            ORDER BY (SUM(CASE WHEN fa.correct = false THEN 1 ELSE 0 END)::float / COUNT(fa.id)) DESC
            """, nativeQuery = true)
    List<Object[]> findWeakTopicsByClassId(@Param("classId") UUID classId);
}
