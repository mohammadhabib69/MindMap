package com.mindmap.repository;

import com.mindmap.database.DatabaseException;
import com.mindmap.database.DatabaseManager;
import com.mindmap.model.LearningEvent;
import com.mindmap.util.DateUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Repository handling SQLite database operations for LearningEvent entities (timeline events).
 */
public class LearningEventRepository {

    private static final Logger LOGGER = Logger.getLogger(LearningEventRepository.class.getName());

    /**
     * Creates and records a learning event in the timeline.
     */
    public LearningEvent create(LearningEvent event) {
        String sql = """
                INSERT INTO learning_events (note_id, event_type, event_date, description)
                VALUES (?, ?, ?, ?);
                """;

        if (event.getEventDate() == null) {
            event.setEventDate(LocalDateTime.now());
        }

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            if (event.getNoteId() != null) {
                stmt.setInt(1, event.getNoteId());
            } else {
                stmt.setNull(1, Types.INTEGER);
            }

            stmt.setString(2, event.getEventType());
            stmt.setString(3, DateUtil.formatDateTime(event.getEventDate()));
            stmt.setString(4, event.getDescription());

            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    event.setId(generatedKeys.getInt(1));
                } else {
                    throw new DatabaseException("Creating learning event failed, no ID obtained.");
                }
            }

            return event;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating learning event: " + e.getMessage(), e);
            throw new DatabaseException("Failed to insert learning event into database", e);
        }
    }

    /**
     * Finds a learning event by its ID.
     */
    public Optional<LearningEvent> findById(int id) {
        String sql = "SELECT id, note_id, event_type, event_date, description FROM learning_events WHERE id = ?;";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToLearningEvent(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding learning event by ID: " + id, e);
            throw new DatabaseException("Failed to query learning event with ID " + id, e);
        }
    }

    /**
     * Retrieves all learning events ordered by event_date descending.
     */
    public List<LearningEvent> findAll() {
        String sql = "SELECT id, note_id, event_type, event_date, description FROM learning_events ORDER BY event_date DESC;";
        List<LearningEvent> events = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                events.add(mapResultSetToLearningEvent(rs));
            }
            return events;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding all learning events: " + e.getMessage(), e);
            throw new DatabaseException("Failed to query all learning events", e);
        }
    }

    /**
     * Finds learning events associated with a specific note.
     */
    public List<LearningEvent> findByNoteId(int noteId) {
        String sql = "SELECT id, note_id, event_type, event_date, description FROM learning_events WHERE note_id = ? ORDER BY event_date DESC;";
        List<LearningEvent> events = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, noteId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    events.add(mapResultSetToLearningEvent(rs));
                }
            }
            return events;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding learning events for note ID: " + noteId, e);
            throw new DatabaseException("Failed to query learning events for note ID " + noteId, e);
        }
    }

    /**
     * Finds learning events occurring within a specific date-time range.
     */
    public List<LearningEvent> findBetween(LocalDateTime from, LocalDateTime to) {
        String sql = """
                SELECT id, note_id, event_type, event_date, description
                FROM learning_events
                WHERE event_date BETWEEN ? AND ?
                ORDER BY event_date DESC;
                """;
        List<LearningEvent> events = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, DateUtil.formatDateTime(from));
            stmt.setString(2, DateUtil.formatDateTime(to));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    events.add(mapResultSetToLearningEvent(rs));
                }
            }
            return events;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding learning events between dates: " + e.getMessage(), e);
            throw new DatabaseException("Failed to query learning events in time range", e);
        }
    }

    /**
     * Deletes a learning event by its ID.
     */
    public boolean delete(int id) {
        String sql = "DELETE FROM learning_events WHERE id = ?;";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting learning event ID: " + id, e);
            throw new DatabaseException("Failed to delete learning event with ID " + id, e);
        }
    }

    private LearningEvent mapResultSetToLearningEvent(ResultSet rs) throws SQLException {
        int noteIdVal = rs.getInt("note_id");
        Integer noteId = rs.wasNull() ? null : noteIdVal;

        return new LearningEvent(
                rs.getInt("id"),
                noteId,
                rs.getString("event_type"),
                DateUtil.parseDateTime(rs.getString("event_date")),
                rs.getString("description")
        );
    }
}
