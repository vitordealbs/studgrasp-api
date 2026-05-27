CREATE TABLE groups (
                        id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                        class_id   UUID REFERENCES classes(id) ON DELETE CASCADE,
                        name       VARCHAR(100) NOT NULL,
                        created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE group_members (
                               group_id  UUID NOT NULL REFERENCES groups(id) ON DELETE CASCADE,
                               user_id   UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                               joined_at TIMESTAMP NOT NULL DEFAULT NOW(),
                               PRIMARY KEY (group_id, user_id)
);

CREATE TABLE messages (
                          id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                          group_id   UUID NOT NULL REFERENCES groups(id) ON DELETE CASCADE,
                          sender_id  UUID NOT NULL REFERENCES users(id),
                          content    TEXT NOT NULL,
                          created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_messages_group ON messages(group_id, created_at DESC);