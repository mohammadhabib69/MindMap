package com.mindmap.repository;

import com.mindmap.database.DatabaseException;
import com.mindmap.database.DatabaseManager;
import com.mindmap.model.Connection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Repository handling SQLite database operations for Connection entities (graph edges).
 * <p>
 * Uses {@code java.sql.Connection} explicitly for database connectivity.
 */
public class ConnectionRepository {

    private static final Logger LOGGER = Logger.getLogger(ConnectionRepository.class.getName());

    /**
     * Persists a connection between two notes.
     *
     * @param connection The connection entity.
     * @return The saved Connection with generated ID.
     */
    public Connection create(Connection connection) {
        String sql = "INSERT INTO connections (from_note_id, to_note_id, relation) VALUES (?, ?, ?);";

        try (java.sql.Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, connection.getFromNoteId());
            stmt.setInt(2, connection.getToNoteId());
            stmt.setString(3, connection.getRelation());

            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    connection.setId(generatedKeys.getInt(1));
                } else {
                    throw new DatabaseException("Creating connection failed, no ID obtained.");
                }
            }

            return connection;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating note connection: " + e.getMessage(), e);
            throw new DatabaseException("Failed to create connection between notes", e);
        }
    }

    /**
     * Finds a connection by its ID.
     */
    public Optional<Connection> findById(int id) {
        String sql = "SELECT id, from_note_id, to_note_id, relation FROM connections WHERE id = ?;";

        try (java.sql.Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new Connection(
                            rs.getInt("id"),
                            rs.getInt("from_note_id"),
                            rs.getInt("to_note_id"),
                            rs.getString("relation")
                    ));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding connection by ID: " + id, e);
            throw new DatabaseException("Failed to query connection with ID " + id, e);
        }
    }

    /**
     * Retrieves all connections in the database.
     */
    public List<Connection> findAll() {
        String sql = "SELECT id, from_note_id, to_note_id, relation FROM connections;";
        List<Connection> connections = new ArrayList<>();

        try (java.sql.Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                connections.add(new Connection(
                        rs.getInt("id"),
                        rs.getInt("from_note_id"),
                        rs.getInt("to_note_id"),
                        rs.getString("relation")
                ));
            }
            return connections;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error querying all connections: " + e.getMessage(), e);
            throw new DatabaseException("Failed to query all connections", e);
        }
    }

    /**
     * Retrieves all connections involving the specified note (either as source or target).
     */
    public List<Connection> findByNoteId(int noteId) {
        String sql = "SELECT id, from_note_id, to_note_id, relation FROM connections WHERE from_note_id = ? OR to_note_id = ?;";
        List<Connection> connections = new ArrayList<>();

        try (java.sql.Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, noteId);
            stmt.setInt(2, noteId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    connections.add(new Connection(
                            rs.getInt("id"),
                            rs.getInt("from_note_id"),
                            rs.getInt("to_note_id"),
                            rs.getString("relation")
                    ));
                }
            }
            return connections;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error querying connections for note ID: " + noteId, e);
            throw new DatabaseException("Failed to query connections for note ID " + noteId, e);
        }
    }

    /**
     * Deletes a connection by its ID.
     */
    public boolean delete(int id) {
        String sql = "DELETE FROM connections WHERE id = ?;";

        try (java.sql.Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting connection ID: " + id, e);
            throw new DatabaseException("Failed to delete connection with ID " + id, e);
        }
    }

    /**
     * Deletes a directed relationship between two notes.
     */
    public boolean deleteBetween(int fromNoteId, int toNoteId) {
        String sql = "DELETE FROM connections WHERE from_note_id = ? AND to_note_id = ?;";

        try (java.sql.Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, fromNoteId);
            stmt.setInt(2, toNoteId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting connection between " + fromNoteId + " and " + toNoteId, e);
            throw new DatabaseException("Failed to delete connection between notes", e);
        }
    }
}
