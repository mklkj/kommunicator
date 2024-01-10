CREATE TABLE IF NOT EXISTS contacts (
    uuid uuid NOT NULL,
    "contactUserId" uuid,
    "userId" uuid,
    CONSTRAINT pk_Contacts PRIMARY KEY ("userId", "contactUserId")
);
