CREATE TABLE IF NOT EXISTS chats (
    id uuid PRIMARY KEY,
    "customName" VARCHAR(64) NULL
);

CREATE TABLE IF NOT EXISTS chatparticipants (
    id uuid NOT NULL,
    "chatId" uuid,
    "userId" uuid,
    "customName" VARCHAR(64) NOT NULL,
    CONSTRAINT pk_ChatParticipants PRIMARY KEY ("chatId", "userId")
)
