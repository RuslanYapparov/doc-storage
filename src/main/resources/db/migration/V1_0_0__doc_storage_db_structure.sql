IF NOT EXISTS (
    SELECT *
    FROM sys.objects
    WHERE object_id = OBJECT_ID(N'[dbo].[users]')
    AND type = N'U'
)
BEGIN
    CREATE TABLE [dbo].[users] (
        user_id BIGINT IDENTITY(1,1) PRIMARY KEY,
        username NVARCHAR(255) NOT NULL UNIQUE,
        password NVARCHAR(255) NOT NULL,
        email NVARCHAR(255) NOT NULL UNIQUE,
        first_name NVARCHAR(255),
        last_name NVARCHAR(255)
    );
END