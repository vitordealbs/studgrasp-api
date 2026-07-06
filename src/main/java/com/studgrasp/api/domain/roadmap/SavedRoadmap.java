package com.studgrasp.api.domain.roadmap;

import com.studgrasp.api.domain.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "saved_roadmaps", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "roadmap_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SavedRoadmap {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "roadmap_id", nullable = false)
    private Roadmap roadmap;

    @CreationTimestamp
    @Column(name = "saved_at", nullable = false, updatable = false)
    private LocalDateTime savedAt;
}
