CREATE TABLE IF NOT EXISTS game (
                                    id          INTEGER PRIMARY KEY AUTOINCREMENT,
                                    started_at  TEXT    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS serve (
                                     id              INTEGER PRIMARY KEY AUTOINCREMENT,
                                     game_id         INTEGER NOT NULL REFERENCES game(id),
    receiver_count  INTEGER NOT NULL CHECK (receiver_count BETWEEN 1 AND 6),
    aim_x           REAL    NOT NULL CHECK (aim_x BETWEEN 0.0 AND 1.0),
    aim_y           REAL    NOT NULL CHECK (aim_y BETWEEN 0.0 AND 1.0),
    outcome         TEXT    NOT NULL,
    created_at      TEXT    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    serve_type      TEXT    NOT NULL
    );

CREATE INDEX IF NOT EXISTS idx_serve_game ON serve(game_id);