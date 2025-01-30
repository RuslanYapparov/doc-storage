CREATE TABLE users (
    user_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    username NVARCHAR(20) NOT NULL UNIQUE,
    password NVARCHAR(255) NOT NULL,
    email NVARCHAR(255) NOT NULL UNIQUE,
    first_name NVARCHAR(50),
    last_name NVARCHAR(50)
);

CREATE INDEX idx_users_username ON users (username);