package com.studgrasp.api.domain.roadmap;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "roadmap_nodes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoadmapNode {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "roadmap_id", nullable = false)
    private Roadmap roadmap;

    @Column(name = "parent_id")
    private UUID parentId;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "node_order", nullable = false)
    private Integer nodeOrder;

    @Column(name = "node_type", nullable = false, length = 20)
    private String nodeType;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}