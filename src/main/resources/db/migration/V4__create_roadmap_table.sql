CREATE TABLE roadmaps (
                          id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                          title       VARCHAR(100) NOT NULL,
                          career_type VARCHAR(50) NOT NULL,
                          source_url  VARCHAR(255),
                          scraped_at  TIMESTAMP,
                          created_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE roadmap_nodes (
                               id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                               roadmap_id  UUID NOT NULL REFERENCES roadmaps(id) ON DELETE CASCADE,
                               parent_id   UUID REFERENCES roadmap_nodes(id),
                               title       VARCHAR(150) NOT NULL,
                               description TEXT,
                               node_order  INT NOT NULL DEFAULT 0,
                               node_type   VARCHAR(20) NOT NULL DEFAULT 'TOPIC',
                               created_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_roadmap_nodes_roadmap ON roadmap_nodes(roadmap_id);
CREATE INDEX idx_roadmap_nodes_parent  ON roadmap_nodes(parent_id);