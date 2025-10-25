-- schema + table
DROP table IF EXISTS users.Contact;
CREATE SCHEMA IF NOT EXISTS users;

CREATE TABLE IF NOT EXISTS users.contact (
    id          TEXT PRIMARY KEY,
    userId     TEXT NOT NULL,
    email       TEXT,
    phone       TEXT,
    first_name  TEXT,
    last_name   TEXT,
    is_active   BOOLEAN NOT NULL DEFAULT TRUE,
    created_at  TEXT,
    age         INTEGER
);

CREATE INDEX IF NOT EXISTS idx_users_email ON users.Contact (email);
CREATE INDEX IF NOT EXISTS idx_users_created_at ON users.Contact (created_at DESC);
CREATE INDEX IF NOT EXISTS idx_users_is_active ON users.Contact (is_active);

INSERT INTO users.Contact (id, userId, email, phone, first_name, last_name, is_active, created_at, age)
VALUES
  ('1',
   'u-22',
   'mstauroy@gmail.com',
   '2109456738',
   'Manthos',
   'Staurou',
   TRUE,
   '2025-10-19T21:00:00Z'::timestamptz,
   35)
ON CONFLICT (id) DO NOTHING;
