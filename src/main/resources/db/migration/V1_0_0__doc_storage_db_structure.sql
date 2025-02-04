CREATE TABLE users (
    user_id INT IDENTITY(1,1) PRIMARY KEY,
    username NVARCHAR(20) NOT NULL UNIQUE,
    password NVARCHAR(255) NOT NULL,
    email NVARCHAR(255) NOT NULL UNIQUE,
    first_name NVARCHAR(50),
    last_name NVARCHAR(50),
    is_enabled BIT NOT NULL
);

CREATE TABLE verification_tokens (
    token UNIQUEIDENTIFIER NOT NULL PRIMARY KEY,
    user_id INT NOT NULL,
    expiry_date DATETIME NULL,
    CONSTRAINT fk_verification_user FOREIGN KEY (user_id) REFERENCES users(user_id)
);

CREATE TABLE documents (
    document_id INT IDENTITY(1,1) PRIMARY KEY,
    title NVARCHAR(255) NOT NULL,
    created_at DATE DEFAULT GETDATE(),
    owner_id INT NOT NULL,
    file_path NVARCHAR(255) NOT NULL UNIQUE,
    description NVARCHAR(1000),
    common_access_type NVARCHAR(10) CHECK (common_access_type IN ('READ_ONLY', 'EDIT', 'REMOVE')),
    updated_by NVARCHAR(20),
    updated_at DATETIME,
    FOREIGN KEY (owner_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (updated_by) REFERENCES users(username) ON DELETE NO ACTION
);

CREATE TABLE users_documents (
    document_id INT NOT NULL,
    username NVARCHAR(20) NOT NULL,
    access_type NVARCHAR(10) NOT NULL CHECK (access_type IN ('READ_ONLY', 'EDIT', 'REMOVE')),
    PRIMARY KEY (document_id, username),
    FOREIGN KEY (document_id) REFERENCES documents(document_id) ON DELETE CASCADE,
    FOREIGN KEY (username) REFERENCES users(username) ON DELETE NO ACTION
);

CREATE INDEX idx_users_username ON users(username);

CREATE INDEX idx_documents_title ON documents(title);

CREATE INDEX idx_documents_created_at ON documents(created_at);