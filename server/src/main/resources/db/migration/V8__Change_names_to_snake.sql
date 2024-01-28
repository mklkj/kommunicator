ALTER TABLE chatparticipants RENAME TO chat_participants;
ALTER TABLE chat_participants RENAME COLUMN "chatId" TO chat_id;
ALTER TABLE chat_participants RENAME COLUMN "userId" TO user_id;
ALTER TABLE chat_participants RENAME COLUMN "customName" TO custom_name;

ALTER TABLE chats RENAME COLUMN "customName" TO custom_name;

ALTER TABLE contacts RENAME COLUMN "uuid" TO id;
ALTER TABLE contacts RENAME COLUMN "contactUserId" TO contact_user_id;
ALTER TABLE contacts RENAME COLUMN "userId" TO user_id;

ALTER TABLE messages RENAME COLUMN "chatId" TO chat_id;
ALTER TABLE messages RENAME COLUMN "userId" TO user_id;
ALTER TABLE messages RENAME COLUMN "timestamp" TO created_at;

ALTER TABLE users RENAME COLUMN "uuid" TO id;
ALTER TABLE users RENAME COLUMN "firstName" TO first_name;
ALTER TABLE users RENAME COLUMN "lastName" TO last_name;
ALTER TABLE users RENAME COLUMN "dateOfBirth" TO date_of_birth;

ALTER TABLE usertokens RENAME TO users_tokens;
ALTER TABLE users_tokens RENAME COLUMN "userId" TO user_id;
ALTER TABLE users_tokens RENAME COLUMN "refreshToken" TO refresh_token;
ALTER TABLE users_tokens RENAME COLUMN "timestamp" TO created_at;
ALTER TABLE users_tokens RENAME COLUMN "validTo" TO valid_to;
