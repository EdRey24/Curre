-- 1. Create the Users table first
CREATE TABLE users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL
);

-- 2. Create the Runs table
CREATE TABLE runs (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER REFERENCES users(id),
    started_at BIGINT NOT NULL,
    ended_at BIGINT NOT NULL,
    distance_miles REAL NOT NULL,
    duration_seconds INTEGER NOT NULL,
    avg_pace_secs_per_mile REAL NOT NULL,
    calories INTEGER NOT NULL DEFAULT 0
);

-- 3. Create the Emergency Contacts table
CREATE TABLE emergency_contacts (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(50),
    user_id INTEGER NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 4. Create the Safety Sessions table
CREATE TABLE safety_sessions (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    run_id INTEGER NOT NULL UNIQUE,
    user_id INTEGER NOT NULL,
    check_in_interval_seconds INTEGER NOT NULL,
    last_check_in BIGINT NOT NULL,
    active BOOLEAN NOT NULL,
    FOREIGN KEY (run_id) REFERENCES runs(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- 5. Create the Route Points table for the Maps
CREATE TABLE route_points (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    run_id INTEGER NOT NULL,
    latitude REAL NOT NULL,
    longitude REAL NOT NULL,
    timestamp_millis INTEGER NOT NULL,
    FOREIGN KEY (run_id) REFERENCES runs(id) ON DELETE CASCADE
);