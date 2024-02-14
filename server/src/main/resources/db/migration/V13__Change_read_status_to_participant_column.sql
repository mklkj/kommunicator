DROP TABLE messages_read;
ALTER TABLE chat_participants ADD read_at TIMESTAMP NULL;
