CREATE TABLE t_users (
                         id SERIAL PRIMARY KEY,
                         name VARCHAR(50) NOT NULL,
                         email VARCHAR(50) NOT NULL,
                         role VARCHAR(20) NOT NULL
);
