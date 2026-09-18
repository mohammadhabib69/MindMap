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

    private static final String CREATE_NOTES_TABLE_SQL = """
            CREATE TABLE IF NOT EXISTS notes (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                title TEXT NOT NULL,
                content TEXT,
                subject TEXT,
                difficulty TEXT,
                created_at TEXT NOT NULL,
                updated_at TEXT NOT NULL
            );
            """;

    private DatabaseInitializer() {
        // Private constructor to prevent instantiation
    }

    /**
     * Initializes the database by opening a connection and creating the notes table if needed.
     *
     * @throws SQLException If database initialization fails.
     */
    public static void initialize() throws SQLException {
        LOGGER.log(Level.INFO, "Initializing database at: {0}", DatabaseManager.getDatabasePath());

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute(CREATE_NOTES_TABLE_SQL);
            LOGGER.log(Level.INFO, "Database initialized successfully with 'notes' table.");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize database: " + e.getMessage(), e);
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
