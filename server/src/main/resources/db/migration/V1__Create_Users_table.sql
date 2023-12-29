CREATE TYPE UserGender AS ENUM ('MALE', 'FEMALE');

CREATE TABLE IF NOT EXISTS users (
    uuid uuid PRIMARY KEY,
    email VARCHAR(64) NOT NULL,
    username VARCHAR(64) NOT NULL,
    "password" VARCHAR(64) NOT NULL,
    "firstName" VARCHAR(64) NOT NULL,
    "lastName" VARCHAR(64) NOT NULL,
    "dateOfBirth" DATE NOT NULL,
    gender UserGender NOT NULL
)
