CREATE TABLE runs (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    started_at BIGINT NOT NULL,
    ended_at BIGINT NOT NULL,
    distance_miles REAL NOT NULL,
    duration_seconds INTEGER NOT NULL,
    avg_pace_secs_per_mile REAL NOT NULL
);