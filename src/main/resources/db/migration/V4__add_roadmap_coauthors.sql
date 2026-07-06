-- Add coauthors table for collaborative roadmap editing
CREATE TABLE roadmap_coauthors (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    roadmap_id UUID NOT NULL REFERENCES roadmaps(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    invited_by UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    invited_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(roadmap_id, user_id)
);

CREATE INDEX idx_roadmap_coauthors_roadmap ON roadmap_coauthors(roadmap_id);
CREATE INDEX idx_roadmap_coauthors_user ON roadmap_coauthors(user_id);
