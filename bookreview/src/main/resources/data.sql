CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE,
    email VARCHAR(100),
    password VARCHAR(255),
    role VARCHAR(20)
    );

CREATE TABLE IF NOT EXISTS books (
    id SERIAL PRIMARY KEY,
    title VARCHAR(255),
    author VARCHAR(255),
    description TEXT,
    cover_url VARCHAR(255),
    is_active BOOLEAN DEFAULT TRUE
    );

CREATE TABLE IF NOT EXISTS reviews (
    id SERIAL PRIMARY KEY,
    text TEXT,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    user_id BIGINT REFERENCES users(id),
    book_id BIGINT REFERENCES books(id)
    );

CREATE TABLE IF NOT EXISTS likes (
    id SERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id),
    review_id BIGINT REFERENCES reviews(id),
    UNIQUE(user_id, review_id)
    );
