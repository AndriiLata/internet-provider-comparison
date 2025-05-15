CREATE TABLE service_rating
(
    id          BIGSERIAL PRIMARY KEY,
    service_name VARCHAR(255) NOT NULL,
    user_name    VARCHAR(255) NOT NULL,
    email        VARCHAR(255) NOT NULL,
    ranking      INT          NOT NULL CHECK (ranking BETWEEN 1 AND 5),
    comment      TEXT,
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
