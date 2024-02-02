CREATE TABLE IF NOT EXISTS user_push_tokens (
    token VARCHAR(200) NOT NULL,
    device_id_hash VARCHAR(64) NOT NULL,
    device_name VARCHAR(32) NULL,
    user_id uuid NOT NULL
);
ALTER TABLE user_push_tokens ADD CONSTRAINT token_device_unique UNIQUE (token, device_id_hash, user_id);
