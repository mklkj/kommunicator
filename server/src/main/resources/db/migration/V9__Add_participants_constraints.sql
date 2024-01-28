ALTER TABLE chat_participants DROP CONSTRAINT pk_chatparticipants;
ALTER TABLE chat_participants ADD CONSTRAINT pk_chat_participants PRIMARY KEY (chat_id, user_id);
ALTER TABLE chat_participants ADD CONSTRAINT participants_unique UNIQUE (chat_id, user_id);
