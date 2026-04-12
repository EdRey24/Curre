CREATE TABLE safety_sessions (
                                 id INTEGER PRIMARY KEY AUTOINCREMENT,
                                 run_id INTEGER NOT NULL UNIQUE,
                                 user_id INTEGER NOT NULL,
                                 check_in_interval_seconds INTEGER NOT NULL,
                                 last_check_in TIMESTAMP NOT NULL,
                                 active BOOLEAN NOT NULL,
                                 FOREIGN KEY (run_id) REFERENCES runs(id) ON DELETE CASCADE,
                                 FOREIGN KEY (user_id) REFERENCES users(id)
);