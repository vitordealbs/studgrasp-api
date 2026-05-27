CREATE TABLE classes (
                         id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                         name         VARCHAR(100) NOT NULL,
                         description  TEXT,
                         advisor_id   UUID NOT NULL REFERENCES users(id),
                         invite_code  VARCHAR(20) NOT NULL UNIQUE,
                         created_at   TIMESTAMP NOT NULL DEFAULT NOW(),
                         updated_at   TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE class_members (
                               class_id   UUID NOT NULL REFERENCES classes(id) ON DELETE CASCADE,
                               user_id    UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                               joined_at  TIMESTAMP NOT NULL DEFAULT NOW(),
                               PRIMARY KEY (class_id, user_id)
);

CREATE INDEX idx_class_members_user ON class_members(user_id);