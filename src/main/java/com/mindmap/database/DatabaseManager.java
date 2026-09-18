package com.mindmap.database;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages SQLite database connections and ensures directory and PRAGMA settings.
 */
public class DatabaseManager {

    private static final Logger LOGGER = Logger.getLogger(DatabaseManager.class.getName());
    private static final String DEFAULT_DB_PATH = "data/mindmap.db";
    private static String databasePath = DEFAULT_DB_PATH;

    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.WARNING, "org.sqlite.JDBC class not found: " + e.getMessage(), e);
        }
    }

    private DatabaseManager() {
        // Private constructor to prevent instantiation
    }

    /**
     * Gets the current database relative or configurable path.
     *
     * @return Path to the SQLite database file.
     */
    public static synchronized String getDatabasePath() {
        return databasePath;
    }

    /**
     * Sets a custom database path (useful for testing or alternative environments).
     *
     * @param path The database file path to use.
     */
    public static synchronized void setDatabasePath(String path) {
        if (path == null || path.trim().isEmpty()) {
            databasePath = DEFAULT_DB_PATH;
        } else {
            databasePath = path.trim();
        }
    }

    /**
     * Resets the database path back to the default path ("data/mindmap.db").
     */
    public static synchronized void resetToDefaultPath() {
        databasePath = DEFAULT_DB_PATH;
    }

    /**
     * Creates and returns a new SQLite connection with foreign keys enabled.
     * Automatically ensures the parent directory exists before connecting.
     *
     * @return An active java.sql.Connection.
     * @throws SQLException If a database access error occurs.
     */
    public static Connection getConnection() throws SQLException {
        String currentPath = getDatabasePath();
        File dbFile = new File(currentPath);
        File parentDir = dbFile.getParentFile();

        if (parentDir != null && !parentDir.exists()) {
            boolean created = parentDir.mkdirs();
            if (created) {
                LOGGER.log(Level.INFO, "Created database directory: {0}", parentDir.getPath());
            }
        }

        String jdbcUrl = "jdbc:sqlite:" + currentPath;
        Connection connection = DriverManager.getConnection(jdbcUrl);

        // Enable foreign key constraints in SQLite
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON;");
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Failed to enable foreign keys for connection", e);
            connection.close();
            throw e;
        }

        return connection;
    }
}
