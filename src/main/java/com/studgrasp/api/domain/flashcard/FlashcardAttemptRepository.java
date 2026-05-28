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
    List<FlashcardAttempt> findByUserId(UUID userId);

    @Query("SELECT fa FROM FlashcardAttempt fa " +
           "WHERE fa.user.id = :userId AND fa.nextReviewAt <= :now " +
           "ORDER BY fa.nextReviewAt ASC")
    List<FlashcardAttempt> findDueForReview(@Param("userId") UUID userId,
                                            @Param("now") LocalDateTime now);

    @Query("SELECT fa FROM FlashcardAttempt fa " +
           "WHERE fa.user.id = :userId AND fa.flashcard.id = :flashcardId " +
           "ORDER BY fa.answeredAt DESC")
    Optional<FlashcardAttempt> findLatestByUserAndFlashcard(@Param("userId") UUID userId,
                                                            @Param("flashcardId") UUID flashcardId);
}
