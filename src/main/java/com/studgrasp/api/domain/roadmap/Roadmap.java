package com.studgrasp.api.domain.roadmap;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "roadmaps")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Roadmap {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(name = "career_type", nullable = false, length = 50)
    private String careerType;

    @Column(name = "source_url", length = 255)
    private String sourceUrl;

    @Column(name = "scraped_at")
    private LocalDateTime scrapedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "roadmap", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RoadmapNode> nodes = new ArrayList<>();
}