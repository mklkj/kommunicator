CREATE TABLE IF NOT EXISTS messages (
    id uuid PRIMARY KEY,
    "chatId" uuid NOT NULL,
    "userId" uuid NOT NULL,
    "timestamp" TIMESTAMP NOT NULL,
    "content" TEXT NOT NULL
)
