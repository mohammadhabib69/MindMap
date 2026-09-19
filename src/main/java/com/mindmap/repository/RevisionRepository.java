package com.mindmap.repository;

import com.mindmap.database.DatabaseException;
import com.mindmap.database.DatabaseManager;
import com.mindmap.model.Revision;
import com.mindmap.util.DateUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Repository handling SQLite database operations for Revision entities.
 */
public class RevisionRepository {

    private static final Logger LOGGER = Logger.getLogger(RevisionRepository.class.getName());

    /**
     * Creates and persists a new Revision.
     */
    public Revision create(Revision revision) {
        String sql = """
                INSERT INTO revisions (note_id, review_date, status, interval_days, created_at)
                VALUES (?, ?, ?, ?, ?);
                """;

        if (revision.getCreatedAt() == null) {
            revision.setCreatedAt(LocalDateTime.now());
        }

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, revision.getNoteId());
            stmt.setString(2, DateUtil.formatDate(revision.getReviewDate()));
            stmt.setString(3, revision.getStatus());
            stmt.setInt(4, revision.getIntervalDays());
            stmt.setString(5, DateUtil.formatDateTime(revision.getCreatedAt()));

            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    revision.setId(generatedKeys.getInt(1));
                } else {
                    throw new DatabaseException("Creating revision failed, no ID obtained.");
                }
            }

            return revision;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating revision: " + e.getMessage(), e);
            throw new DatabaseException("Failed to insert revision into database", e);
        }
    }

    /**
     * Finds a revision by its ID.
     */
    public Optional<Revision> findById(int id) {
        String sql = "SELECT id, note_id, review_date, status, interval_days, created_at FROM revisions WHERE id = ?;";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToRevision(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding revision by ID: " + id, e);
            throw new DatabaseException("Failed to query revision with ID " + id, e);
        }
    }

    /**
     * Finds all revisions for a given note.
     */
    public List<Revision> findByNoteId(int noteId) {
        String sql = "SELECT id, note_id, review_date, status, interval_days, created_at FROM revisions WHERE note_id = ? ORDER BY review_date ASC;";
        List<Revision> revisions = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, noteId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    revisions.add(mapResultSetToRevision(rs));
                }
            }
            return revisions;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding revisions for note ID: " + noteId, e);
            throw new DatabaseException("Failed to query revisions for note ID " + noteId, e);
        }
    }

    /**
     * Finds all revisions due on or before a specific date that are PENDING.
     */
    public List<Revision> findDue(LocalDate date) {
        String sql = """
                SELECT id, note_id, review_date, status, interval_days, created_at
                FROM revisions
                WHERE review_date <= ? AND (status IS NULL OR status = 'PENDING')
                ORDER BY review_date ASC;
                """;
        List<Revision> revisions = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, DateUtil.formatDate(date));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    revisions.add(mapResultSetToRevision(rs));
                }
            }
            return revisions;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding due revisions: " + e.getMessage(), e);
            throw new DatabaseException("Failed to query due revisions", e);
        }
    }

    /**
     * Finds upcoming revisions scheduled between two dates inclusive.
     */
    public List<Revision> findUpcoming(LocalDate from, LocalDate to) {
        String sql = """
                SELECT id, note_id, review_date, status, interval_days, created_at
                FROM revisions
                WHERE review_date BETWEEN ? AND ?
                ORDER BY review_date ASC;
                """;
        List<Revision> revisions = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, DateUtil.formatDate(from));
            stmt.setString(2, DateUtil.formatDate(to));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    revisions.add(mapResultSetToRevision(rs));
                }
            }
            return revisions;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding upcoming revisions: " + e.getMessage(), e);
            throw new DatabaseException("Failed to query upcoming revisions", e);
        }
    }

    /**
     * Finds all upcoming revisions scheduled after a specific date that are PENDING.
     */
    public List<Revision> findUpcomingAfter(LocalDate date) {
        String sql = """
                SELECT id, note_id, review_date, status, interval_days, created_at
                FROM revisions
                WHERE review_date > ? AND (status IS NULL OR status = 'PENDING')
                ORDER BY review_date ASC;
                """;
        List<Revision> revisions = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, DateUtil.formatDate(date));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    revisions.add(mapResultSetToRevision(rs));
                }
            }
            return revisions;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding upcoming revisions after date: " + e.getMessage(), e);
            throw new DatabaseException("Failed to query upcoming revisions", e);
        }
    }

    /**
     * Finds an active (PENDING) revision for a note, if any.
     */
    public Optional<Revision> findActiveByNoteId(int noteId) {
        String sql = """
                SELECT id, note_id, review_date, status, interval_days, created_at
                FROM revisions
                WHERE note_id = ? AND (status IS NULL OR status = 'PENDING')
                ORDER BY review_date ASC
                LIMIT 1;
                """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, noteId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToRevision(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding active revision for note ID: " + noteId, e);
            throw new DatabaseException("Failed to query active revision for note ID " + noteId, e);
        }
    }

    /**
     * Returns the count of revisions due on or before the given date.
     */
    public int countDue(LocalDate date) {
        String sql = """
                SELECT COUNT(*)
                FROM revisions
                WHERE review_date <= ? AND (status IS NULL OR status = 'PENDING');
                """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, DateUtil.formatDate(date));

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            return 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error counting due revisions: " + e.getMessage(), e);
            throw new DatabaseException("Failed to count due revisions", e);
        }
    }

    /**
     * Returns the count of upcoming revisions scheduled after the given date.
     */
    public int countUpcoming(LocalDate date) {
        String sql = """
                SELECT COUNT(*)
                FROM revisions
                WHERE review_date > ? AND (status IS NULL OR status = 'PENDING');
                """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, DateUtil.formatDate(date));

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            return 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error counting upcoming revisions: " + e.getMessage(), e);
            throw new DatabaseException("Failed to count upcoming revisions", e);
        }
    }

    /**
     * Returns the total count of active/pending scheduled revisions.
     */
    public int countTotalScheduled() {
        String sql = """
                SELECT COUNT(*)
                FROM revisions
                WHERE status IS NULL OR status = 'PENDING';
                """;

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error counting total scheduled revisions: " + e.getMessage(), e);
            throw new DatabaseException("Failed to count total scheduled revisions", e);
        }
    }

    /**
     * Updates an existing revision.
     */
    public boolean update(Revision revision) {
        String sql = """
                UPDATE revisions
                SET note_id = ?, review_date = ?, status = ?, interval_days = ?
                WHERE id = ?;
                """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, revision.getNoteId());
            stmt.setString(2, DateUtil.formatDate(revision.getReviewDate()));
            stmt.setString(3, revision.getStatus());
            stmt.setInt(4, revision.getIntervalDays());
            stmt.setInt(5, revision.getId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating revision ID: " + revision.getId(), e);
            throw new DatabaseException("Failed to update revision with ID " + revision.getId(), e);
        }
    }

    /**
     * Deletes a revision by its ID.
     */
    public boolean delete(int id) {
        String sql = "DELETE FROM revisions WHERE id = ?;";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting revision ID: " + id, e);
            throw new DatabaseException("Failed to delete revision with ID " + id, e);
        }
    }

    private Revision mapResultSetToRevision(ResultSet rs) throws SQLException {
        return new Revision(
                rs.getInt("id"),
                rs.getInt("note_id"),
                DateUtil.parseDate(rs.getString("review_date")),
                rs.getString("status"),
                rs.getInt("interval_days"),
                DateUtil.parseDateTime(rs.getString("created_at"))
        );
    }
}
