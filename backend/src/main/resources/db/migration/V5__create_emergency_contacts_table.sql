CREATE TABLE emergency_contacts (
                                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                                    name VARCHAR(255) NOT NULL,
                                    email VARCHAR(255) NOT NULL,
                                    phone VARCHAR(50),
                                    user_id INTEGER NOT NULL,
                                    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);