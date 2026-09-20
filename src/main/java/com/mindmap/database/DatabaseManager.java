package com.mindmap.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages SQLite database connections using the configured database URL.
 */
public class DatabaseManager {

    private static final Logger LOGGER = Logger.getLogger(DatabaseManager.class.getName());
    public static final String JDBC_URL = "jdbc:sqlite:mindmap.sqlite";

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
     * Returns the exact JDBC URL for the SQLite database.
     *
     * @return The JDBC URL string.
     */
    public static String getJdbcUrl() {
        return JDBC_URL;
    }

    /**
     * Gets the database identifier/path.
     *
     * @return Database file name.
     */
    public static String getDatabasePath() {
        return "mindmap.sqlite";
    }

    /**
     * Creates and returns a new SQLite connection using the exact JDBC URL
     * with foreign keys enabled.
     *
     * @return An active java.sql.Connection.
     * @throws SQLException If a database access error occurs.
     */
    public static Connection getConnection() throws SQLException {
        Connection connection = DriverManager.getConnection(JDBC_URL);

        // Enable foreign key constraints and busy timeout for concurrent threads in SQLite
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON;");
            stmt.execute("PRAGMA busy_timeout = 5000;");
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Failed to configure pragmas for connection", e);
            connection.close();
            throw e;
        }

        return connection;
    }
}
