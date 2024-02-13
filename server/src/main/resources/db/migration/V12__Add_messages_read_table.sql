CREATE TABLE IF NOT EXISTS messages_read (
    message_id uuid NOT NULL,
    participant_id uuid NOT NULL,
    read_at TIMESTAMP NOT NULL
);
ALTER TABLE messages_read ADD CONSTRAINT message_read_participant UNIQUE (message_id, participant_id);
