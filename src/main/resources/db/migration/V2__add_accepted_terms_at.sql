-- Add accepted_terms_at column to users table
ALTER TABLE users ADD COLUMN accepted_terms_at TIMESTAMP;

-- Set accepted_terms_at to created_at for existing users (they implicitly accepted)
UPDATE users SET accepted_terms_at = created_at WHERE accepted_terms_at IS NULL;
