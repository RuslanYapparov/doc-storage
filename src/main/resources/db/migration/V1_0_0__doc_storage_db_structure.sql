CREATE TABLE users (
    user_id INT IDENTITY(1,1) PRIMARY KEY,
    username NVARCHAR(20) NOT NULL UNIQUE,
    password NVARCHAR(255) NOT NULL,
    email NVARCHAR(255) NOT NULL UNIQUE,
    first_name NVARCHAR(50),
    last_name NVARCHAR(50)
);

CREATE TABLE documents (
    document_id INT IDENTITY(1,1) PRIMARY KEY,
    title NVARCHAR(255) NOT NULL,
    created_at DATE NOT NULL,
    author_id INT NOT NULL,
    file_path NVARCHAR(255) NOT NULL UNIQUE,
    description NVARCHAR(1000),
    FOREIGN KEY (author_id) REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE TABLE users_documents (
    user_id INT NOT NULL,
    document_id INT NOT NULL,
    access_type NVARCHAR(10) NOT NULL CHECK (access_type IN ('READ_ONLY', 'EDIT', 'REMOVE')),
    PRIMARY KEY (user_id, document_id),
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (document_id) REFERENCES documents(document_id) ON DELETE NO ACTION
);

CREATE INDEX idx_users_username ON users (username);