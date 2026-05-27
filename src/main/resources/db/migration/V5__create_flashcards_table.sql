CREATE TABLE flashcards (
                            id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                            node_id        UUID NOT NULL REFERENCES roadmap_nodes(id) ON DELETE CASCADE,
                            question       TEXT NOT NULL,
                            answer         TEXT NOT NULL,
                            difficulty     VARCHAR(10) NOT NULL DEFAULT 'MEDIUM',
                            ai_generated   BOOLEAN NOT NULL DEFAULT FALSE,
                            created_at     TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_flashcards_node ON flashcards(node_id);