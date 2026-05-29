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
}
