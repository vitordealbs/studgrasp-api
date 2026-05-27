package com.studgrasp.api.domain.flashcard;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FlashcardAttemptRepository extends JpaRepository<FlashcardAttempt, UUID> {
    List<FlashcardAttempt> findByUserId(UUID userId);
}