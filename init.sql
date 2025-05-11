-- 1) Таблица пользователей
CREATE TABLE IF NOT EXISTS users (
                                     id SERIAL PRIMARY KEY,
                                     login VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL
    );

-- 2) Конфигурация OTP
CREATE TABLE IF NOT EXISTS otp_code_config (
                                               id INT PRIMARY KEY,
                                               code_length INT NOT NULL,
                                               life_time_sec INT NOT NULL
);
INSERT INTO otp_code_config (id, code_length, life_time_sec)
VALUES (1, 6, 300)
    ON CONFLICT (id) DO NOTHING;

-- 3) Таблица кодов: expires_at — теперь nullable
CREATE TABLE IF NOT EXISTS otp_codes (
                                         id           SERIAL      PRIMARY KEY,
                                         user_id      INT         REFERENCES users(id) ON DELETE CASCADE,
    operation_id VARCHAR(255) NOT NULL,
    code         VARCHAR(255) NOT NULL,
    status       VARCHAR(50)  NOT NULL,
    created_at   TIMESTAMP    NOT NULL DEFAULT now(),
    expires_at   TIMESTAMP      -- разрешаем NULL, чтобы DAO мог вставлять только первые 5 полей
    );
