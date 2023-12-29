TRUNCATE users;
ALTER TABLE users ADD CONSTRAINT "Unique username constraint" UNIQUE (username);
