package com.mindmap.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles database schema initialization for Phase 1.
 */
public class DatabaseInitializer {

    private static final Logger LOGGER = Logger.getLogger(DatabaseInitializer.class.getName());

    private static final String[] SCHEMA_STATEMENTS = {
            """
            CREATE TABLE IF NOT EXISTS notes (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                title TEXT NOT NULL,
                content TEXT,
                subject TEXT,
                difficulty TEXT,
                created_at TEXT NOT NULL,
                updated_at TEXT NOT NULL,
    is_private INTEGER DEFAULT 0,
    pin TEXT
            );
            """,
            """
            CREATE TABLE IF NOT EXISTS tags (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL UNIQUE
            );
            """,
            """
            CREATE TABLE IF NOT EXISTS note_tags (
                note_id INTEGER NOT NULL,
                tag_id INTEGER NOT NULL,
                PRIMARY KEY (note_id, tag_id),
                FOREIGN KEY (note_id) REFERENCES notes(id) ON DELETE CASCADE,
                FOREIGN KEY (tag_id) REFERENCES tags(id) ON DELETE CASCADE
            );
            """,
            """
            CREATE TABLE IF NOT EXISTS connections (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                from_note_id INTEGER NOT NULL,
                to_note_id INTEGER NOT NULL,
                relation TEXT,
                FOREIGN KEY (from_note_id) REFERENCES notes(id) ON DELETE CASCADE,
                FOREIGN KEY (to_note_id) REFERENCES notes(id) ON DELETE CASCADE,
                UNIQUE(from_note_id, to_note_id)
            );
            """,
            """
            CREATE TABLE IF NOT EXISTS revisions (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                note_id INTEGER NOT NULL,
                review_date TEXT NOT NULL,
                status TEXT,
                interval_days INTEGER,
                created_at TEXT NOT NULL,
                FOREIGN KEY (note_id) REFERENCES notes(id) ON DELETE CASCADE
            );
            """,
            """
            CREATE TABLE IF NOT EXISTS learning_events (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                note_id INTEGER,
                event_type TEXT,
                event_date TEXT NOT NULL,
                description TEXT,
                FOREIGN KEY (note_id) REFERENCES notes(id) ON DELETE SET NULL
            );
            """
    };

    private DatabaseInitializer() {
        // Private constructor to prevent instantiation
    }

    /**
     * Initializes the database by opening a connection and creating all required schema tables if needed.
     *
     * @throws SQLException If database initialization fails.
     */
    public static void initialize() throws SQLException {
        LOGGER.log(Level.INFO, "Initializing database at: {0}", DatabaseManager.getDatabasePath());

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement()) {


            for (String sql : SCHEMA_STATEMENTS) {
                stmt.execute(sql);
            }
            
            // Safe Migrations for Phase 18
            try {
                stmt.execute("ALTER TABLE notes ADD COLUMN is_favorite INTEGER DEFAULT 0;");
                LOGGER.info("Added is_favorite column to notes table.");
            } catch (SQLException ignored) {}
            try {
                stmt.execute("ALTER TABLE notes ADD COLUMN last_viewed_at TEXT;");
                LOGGER.info("Added last_viewed_at column to notes table.");
            } catch (SQLException ignored) {}
            try {
                stmt.execute("ALTER TABLE notes ADD COLUMN is_private INTEGER DEFAULT 0;");
            } catch (SQLException ignored) {}
            try {
                stmt.execute("ALTER TABLE notes ADD COLUMN pin TEXT;");
            } catch (SQLException ignored) {}

            LOGGER.log(Level.INFO, "Database schema initialized successfully (notes, tags, note_tags, connections, revisions, learning_events).");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize database schema: " + e.getMessage(), e);
            throw e;
        }
    }

    public static void main(String[] args) {
        try {
            initialize();
            System.out.println("Database initialization verification succeeded.");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Initialization failed in main: " + e.getMessage(), e);
            System.exit(1);
        }
    }
}
