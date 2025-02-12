CREATE DATABASE docstorage_db
COLLATE Russian_CI_AS;

USE docstorage_db;

CREATE LOGIN docstorage_dev WITH PASSWORD = 'ruyappy777';
CREATE USER docstorage_dev FOR LOGIN docstorage_dev;

ALTER ROLE db_owner ADD MEMBER docstorage_dev;