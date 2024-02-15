ALTER TABLE user_push_tokens DROP CONSTRAINT token_device_unique;
ALTER TABLE user_push_tokens ADD CONSTRAINT token_device_unique UNIQUE (device_id_hash);
