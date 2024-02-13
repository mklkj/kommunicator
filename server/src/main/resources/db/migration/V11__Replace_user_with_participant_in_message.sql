ALTER TABLE messages RENAME COLUMN user_id TO participant_id;
TRUNCATE messages;
