ALTER TABLE users ALTER COLUMN date_of_birth TYPE DATE, ALTER COLUMN date_of_birth DROP NOT NULL;
ALTER TABLE users ALTER COLUMN gender TYPE UserGender, ALTER COLUMN gender DROP NOT NULL;
