CREATE TABLE IF NOT EXISTS usertokens (
    id uuid PRIMARY KEY,
    "userId" uuid NOT NULL,
    "refreshToken" VARCHAR(16) NOT NULL,
    "timestamp" TIMESTAMP NOT NULL,
    "validTo" TIMESTAMP NOT NULL
)
