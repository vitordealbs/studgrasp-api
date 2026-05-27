CREATE TABLE user_progress (
                               id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                               user_id     UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                               node_id     UUID NOT NULL REFERENCES roadmap_nodes(id) ON DELETE CASCADE,
                               status      VARCHAR(20) NOT NULL DEFAULT 'PENDING',
                               updated_at  TIMESTAMP NOT NULL DEFAULT NOW(),
                               UNIQUE (user_id, node_id)
);

CREATE TABLE flashcard_attempts (
                                    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                    user_id         UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                    flashcard_id    UUID NOT NULL REFERENCES flashcards(id) ON DELETE CASCADE,
                                    correct         BOOLEAN NOT NULL,
                                    answered_at     TIMESTAMP NOT NULL DEFAULT NOW(),
                                    next_review_at  TIMESTAMP
);

CREATE TABLE study_sessions (
                                id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                user_id     UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                node_id     UUID REFERENCES roadmap_nodes(id),
                                started_at  TIMESTAMP NOT NULL,
                                ended_at    TIMESTAMP,
                                duration_s  INT
);

CREATE INDEX idx_user_progress_user     ON user_progress(user_id);
CREATE INDEX idx_flashcard_attempts_user ON flashcard_attempts(user_id, answered_at DESC);
CREATE INDEX idx_study_sessions_user    ON study_sessions(user_id, node_id);