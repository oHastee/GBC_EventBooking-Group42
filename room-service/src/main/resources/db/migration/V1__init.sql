CREATE TABLE t_room(
        id SERIAL PRIMARY KEY,
        room_name VARCHAR(100) NOT NULL,
        capacity INT NOT NULL,
        features TEXT
)