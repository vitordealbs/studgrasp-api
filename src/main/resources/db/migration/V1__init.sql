CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ── Users ─────────────────────────────────────────────────────────────────────

CREATE TABLE users (
    id            UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    name          VARCHAR(100) NOT NULL,
    email         VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role          VARCHAR(20)  NOT NULL DEFAULT 'STUDENT',
    avatar_path   VARCHAR(255),
    created_at    TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_users_email ON users(email);

-- ── Classrooms ────────────────────────────────────────────────────────────────

CREATE TABLE classes (
    id          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(100) NOT NULL,
    description TEXT,
    advisor_id  UUID         NOT NULL REFERENCES users(id),
    invite_code VARCHAR(20)  NOT NULL UNIQUE,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE class_members (
    class_id  UUID      NOT NULL REFERENCES classes(id) ON DELETE CASCADE,
    user_id   UUID      NOT NULL REFERENCES users(id)   ON DELETE CASCADE,
    joined_at TIMESTAMP NOT NULL DEFAULT NOW(),
    PRIMARY KEY (class_id, user_id)
);

CREATE INDEX idx_class_members_user ON class_members(user_id);

-- ── Groups & Messages ─────────────────────────────────────────────────────────

CREATE TABLE groups (
    id         UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    class_id   UUID         REFERENCES classes(id) ON DELETE CASCADE,
    name       VARCHAR(100) NOT NULL,
    created_at TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE group_members (
    group_id  UUID      NOT NULL REFERENCES groups(id) ON DELETE CASCADE,
    user_id   UUID      NOT NULL REFERENCES users(id)  ON DELETE CASCADE,
    joined_at TIMESTAMP NOT NULL DEFAULT NOW(),
    PRIMARY KEY (group_id, user_id)
);

CREATE TABLE messages (
    id         UUID      PRIMARY KEY DEFAULT gen_random_uuid(),
    group_id   UUID      NOT NULL REFERENCES groups(id) ON DELETE CASCADE,
    sender_id  UUID      NOT NULL REFERENCES users(id),
    content    TEXT      NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_messages_group ON messages(group_id, created_at DESC);

-- ── Roadmaps ──────────────────────────────────────────────────────────────────

CREATE TABLE roadmaps (
    id          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    title       VARCHAR(100) NOT NULL,
    career_type VARCHAR(50)  NOT NULL,
    source_url  VARCHAR(255),
    scraped_at  TIMESTAMP,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE roadmap_nodes (
    id          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    roadmap_id  UUID         NOT NULL REFERENCES roadmaps(id) ON DELETE CASCADE,
    parent_id   UUID         REFERENCES roadmap_nodes(id),
    title       VARCHAR(150) NOT NULL,
    description TEXT,
    node_order  INT          NOT NULL DEFAULT 0,
    node_type   VARCHAR(20)  NOT NULL DEFAULT 'TOPIC',
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_roadmap_nodes_roadmap ON roadmap_nodes(roadmap_id);
CREATE INDEX idx_roadmap_nodes_parent  ON roadmap_nodes(parent_id);

-- ── Flashcards ────────────────────────────────────────────────────────────────

CREATE TABLE flashcards (
    id           UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    node_id      UUID        NOT NULL REFERENCES roadmap_nodes(id) ON DELETE CASCADE,
    question     TEXT        NOT NULL,
    answer       TEXT        NOT NULL,
    difficulty   VARCHAR(10) NOT NULL DEFAULT 'MEDIUM',
    ai_generated BOOLEAN     NOT NULL DEFAULT FALSE,
    created_by   UUID        REFERENCES users(id),
    created_at   TIMESTAMP   NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_flashcards_node ON flashcards(node_id);

-- ── Progress & Study Sessions ─────────────────────────────────────────────────

CREATE TABLE user_progress (
    id         UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id    UUID        NOT NULL REFERENCES users(id)          ON DELETE CASCADE,
    node_id    UUID        NOT NULL REFERENCES roadmap_nodes(id)  ON DELETE CASCADE,
    status     VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    updated_at TIMESTAMP   NOT NULL DEFAULT NOW(),
    UNIQUE (user_id, node_id)
);

CREATE TABLE study_sessions (
    id         UUID      PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id    UUID      NOT NULL REFERENCES users(id)         ON DELETE CASCADE,
    node_id    UUID      REFERENCES roadmap_nodes(id),
    started_at TIMESTAMP NOT NULL,
    ended_at   TIMESTAMP,
    duration_s INT
);

CREATE INDEX idx_user_progress_user  ON user_progress(user_id);
CREATE INDEX idx_study_sessions_user ON study_sessions(user_id, node_id);

-- ── Flashcard Attempts (SM-2) ─────────────────────────────────────────────────

CREATE TABLE flashcard_attempts (
    id             UUID      PRIMARY KEY DEFAULT gen_random_uuid(),
    flashcard_id   UUID      NOT NULL REFERENCES flashcards(id) ON DELETE CASCADE,
    user_id        UUID      NOT NULL REFERENCES users(id)      ON DELETE CASCADE,
    correct        BOOLEAN   NOT NULL,
    answered_at    TIMESTAMP NOT NULL DEFAULT NOW(),
    next_review_at TIMESTAMP NOT NULL DEFAULT NOW(),
    ease_factor    FLOAT     NOT NULL DEFAULT 2.5,
    interval_days  INT       NOT NULL DEFAULT 0,
    repetitions    INT       NOT NULL DEFAULT 0,
    quality        INT,
    CONSTRAINT uq_flashcard_attempt UNIQUE (flashcard_id, user_id)
);

CREATE INDEX idx_attempts_user_review ON flashcard_attempts(user_id, next_review_at);
