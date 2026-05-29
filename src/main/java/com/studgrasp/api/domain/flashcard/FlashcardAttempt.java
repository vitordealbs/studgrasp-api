package com.studgrasp.api.domain.flashcard;

import com.studgrasp.api.domain.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "flashcard_attempts",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_flashcard_attempt",
        columnNames = {"flashcard_id", "user_id"}
    )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlashcardAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flashcard_id", nullable = false)
    private Flashcard flashcard;

    @Column(nullable = false)
    private boolean correct;

    @Column(name = "answered_at", nullable = false)
    private LocalDateTime answeredAt;

    @Column(name = "next_review_at", nullable = false)
    private LocalDateTime nextReviewAt;

    @Column(name = "ease_factor", nullable = false)
    @Builder.Default
    private double easeFactor = 2.5;

    @Column(name = "interval_days", nullable = false)
    @Builder.Default
    private int intervalDays = 0;

    @Column(name = "repetitions", nullable = false)
    @Builder.Default
    private int repetitions = 0;
}
