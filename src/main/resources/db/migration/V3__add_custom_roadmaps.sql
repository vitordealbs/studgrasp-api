-- Add fields for custom user-created roadmaps
ALTER TABLE roadmaps
ADD COLUMN created_by UUID REFERENCES users(id) ON DELETE SET NULL,
ADD COLUMN is_public BOOLEAN NOT NULL DEFAULT false,
ADD COLUMN is_custom BOOLEAN NOT NULL DEFAULT false;

-- Create saved_roadmaps table for users to save public roadmaps
CREATE TABLE saved_roadmaps (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    roadmap_id UUID NOT NULL REFERENCES roadmaps(id) ON DELETE CASCADE,
    saved_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, roadmap_id)
);

CREATE INDEX idx_saved_roadmaps_user ON saved_roadmaps(user_id);
CREATE INDEX idx_saved_roadmaps_roadmap ON saved_roadmaps(roadmap_id);
CREATE INDEX idx_roadmaps_created_by ON roadmaps(created_by);
CREATE INDEX idx_roadmaps_public ON roadmaps(is_public) WHERE is_public = true;
