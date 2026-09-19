package com.mindmap.repository;

import com.mindmap.database.DatabaseException;
import com.mindmap.database.DatabaseManager;
import com.mindmap.model.LearningEvent;
import com.mindmap.model.Note;
import com.mindmap.model.TimelineEvent;
import com.mindmap.util.DateUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
     * Retrieves all timeline events with their Note details joined in a single query.
     * Ordered chronologically by event_date DESC.
     */
    public List<TimelineEvent> findTimelineEvents() {
        String sql = """
                SELECT
                    le.id,
                    le.note_id,
                    le.event_type,
                    le.event_date,
                    le.description,
                    n.id AS n_id,
                    n.title AS n_title,
                    n.content AS n_content,
                    n.subject AS n_subject,
                    n.difficulty AS n_difficulty,
                    n.created_at AS n_created_at,
                    n.updated_at AS n_updated_at
                FROM learning_events le
                LEFT JOIN notes n ON le.note_id = n.id
                ORDER BY le.event_date DESC;
                """;
        List<TimelineEvent> events = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                events.add(mapJoinedResultSet(rs));
            }
            return events;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding timeline events with notes: " + e.getMessage(), e);
            throw new DatabaseException("Failed to query timeline events with notes", e);
        }
    }

    /**
     * Retrieves timeline events filtered by event type, date range, and search query.
     * Uses a single parameterized JOIN query.
     */
    public List<TimelineEvent> findTimelineEventsFiltered(String eventType, LocalDate fromDate, LocalDate toDate, String searchQuery) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    le.id,
                    le.note_id,
                    le.event_type,
                    le.event_date,
                    le.description,
                    n.id AS n_id,
                    n.title AS n_title,
                    n.content AS n_content,
                    n.subject AS n_subject,
                    n.difficulty AS n_difficulty,
                    n.created_at AS n_created_at,
                    n.updated_at AS n_updated_at
                FROM learning_events le
                LEFT JOIN notes n ON le.note_id = n.id
                WHERE 1=1
                """);

        List<Object> params = new ArrayList<>();

        if (eventType != null && !eventType.trim().isEmpty() && !eventType.equalsIgnoreCase("ALL")) {
            sql.append(" AND UPPER(le.event_type) = ?");
            params.add(eventType.trim().toUpperCase());
        }

        if (fromDate != null) {
            sql.append(" AND le.event_date >= ?");
            params.add(DateUtil.formatDateTime(fromDate.atStartOfDay()));
        }

        if (toDate != null) {
            sql.append(" AND le.event_date <= ?");
            params.add(DateUtil.formatDateTime(toDate.atTime(LocalTime.MAX)));
        }

        if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            String pattern = "%" + searchQuery.trim().toLowerCase() + "%";
            sql.append(" AND (LOWER(COALESCE(n.title, '')) LIKE ? OR LOWER(COALESCE(le.description, '')) LIKE ? OR LOWER(COALESCE(n.subject, '')) LIKE ?)");
            params.add(pattern);
            params.add(pattern);
            params.add(pattern);
        }

        sql.append(" ORDER BY le.event_date DESC;");

        List<TimelineEvent> events = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    events.add(mapJoinedResultSet(rs));
                }
            }
            return events;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding filtered timeline events: " + e.getMessage(), e);
            throw new DatabaseException("Failed to query filtered timeline events", e);
        }
    }

    /**
     * Finds timeline events associated with a specific note ID with note details joined.
     */
    public List<TimelineEvent> findTimelineEventsByNoteId(int noteId) {
        String sql = """
                SELECT
                    le.id,
                    le.note_id,
                    le.event_type,
                    le.event_date,
                    le.description,
                    n.id AS n_id,
                    n.title AS n_title,
                    n.content AS n_content,
                    n.subject AS n_subject,
                    n.difficulty AS n_difficulty,
                    n.created_at AS n_created_at,
                    n.updated_at AS n_updated_at
                FROM learning_events le
                LEFT JOIN notes n ON le.note_id = n.id
                WHERE le.note_id = ?
                ORDER BY le.event_date DESC;
                """;
        List<TimelineEvent> events = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, noteId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    events.add(mapJoinedResultSet(rs));
                }
            }
            return events;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding timeline events for note: " + e.getMessage(), e);
            throw new DatabaseException("Failed to query timeline events for note ID " + noteId, e);
        }
    }

    /**
     * Counts total learning events in database.
     */
    public int countEvents() {
        String sql = "SELECT COUNT(*) FROM learning_events;";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error counting learning events: " + e.getMessage(), e);
            throw new DatabaseException("Failed to count learning events", e);
        }
    }

    /**
     * Counts learning events of a specific type.
     */
    public int countEventsByType(String eventType) {
        String sql = "SELECT COUNT(*) FROM learning_events WHERE UPPER(event_type) = ?;";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, eventType.trim().toUpperCase());
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error counting events by type: " + e.getMessage(), e);
            throw new DatabaseException("Failed to count events by type " + eventType, e);
        }
    }

    /**
     * Counts review completions for a specific calendar date.
     */
    public int countCompletedReviewsForDate(LocalDate date) {
        String sql = """
                SELECT COUNT(*) FROM learning_events
                WHERE UPPER(event_type) = 'NOTE_REVIEWED'
                  AND event_date >= ? AND event_date <= ?;
                """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, DateUtil.formatDateTime(date.atStartOfDay()));
            stmt.setString(2, DateUtil.formatDateTime(date.atTime(LocalTime.MAX)));

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error counting completed reviews for date: " + e.getMessage(), e);
            throw new DatabaseException("Failed to count completed reviews for date", e);
        }
    }

    /**
     * Retrieves the most recent timeline events up to a given limit using a single JOIN query.
     */
    public List<TimelineEvent> findRecentTimelineEvents(int limit) {
        String sql = """
                SELECT
                    le.id,
                    le.note_id,
                    le.event_type,
                    le.event_date,
                    le.description,
                    n.id AS n_id,
                    n.title AS n_title,
                    n.content AS n_content,
                    n.subject AS n_subject,
                    n.difficulty AS n_difficulty,
                    n.created_at AS n_created_at,
                    n.updated_at AS n_updated_at
                FROM learning_events le
                LEFT JOIN notes n ON le.note_id = n.id
                ORDER BY le.event_date DESC
                LIMIT ?;
                """;
        List<TimelineEvent> events = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, limit);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    events.add(mapJoinedResultSet(rs));
                }
            }
            return events;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding recent timeline events: " + e.getMessage(), e);
            throw new DatabaseException("Failed to query recent timeline events", e);
        }
    }

    private TimelineEvent mapJoinedResultSet(ResultSet rs) throws SQLException {
        int noteIdVal = rs.getInt("note_id");
        Integer noteId = rs.wasNull() ? null : noteIdVal;

        LearningEvent le = new LearningEvent(
                rs.getInt("id"),
                noteId,
                rs.getString("event_type"),
                DateUtil.parseDateTime(rs.getString("event_date")),
                rs.getString("description")
        );

        int nIdVal = rs.getInt("n_id");
        Note note = null;
        if (!rs.wasNull() && nIdVal > 0) {
            note = new Note();
            note.setId(nIdVal);
            note.setTitle(rs.getString("n_title"));
            note.setContent(rs.getString("n_content"));
            note.setSubject(rs.getString("n_subject"));
            note.setDifficulty(rs.getString("n_difficulty"));
            note.setCreatedAt(DateUtil.parseDateTime(rs.getString("n_created_at")));
            note.setUpdatedAt(DateUtil.parseDateTime(rs.getString("n_updated_at")));
        }

        return new TimelineEvent(le, note);
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
