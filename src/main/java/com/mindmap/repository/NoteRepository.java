package com.mindmap.repository;

import com.mindmap.database.DatabaseException;
import com.mindmap.database.DatabaseManager;
import com.mindmap.model.Note;
import com.mindmap.util.DateUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Repository handling SQLite database operations for Note entities.
 */
public class NoteRepository {

    private static final Logger LOGGER = Logger.getLogger(NoteRepository.class.getName());

    /**
     * Inserts a new Note and assigns its auto-generated database ID.
     *
     * @param note The note to persist.
     * @return The persisted Note instance with its generated ID.
     */
    public Note create(Note note) {
        String sql = """
                INSERT INTO notes (title, content, subject, difficulty, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?);
                """;

        LocalDateTime now = LocalDateTime.now();
        if (note.getCreatedAt() == null) {
            note.setCreatedAt(now);
        }
        if (note.getUpdatedAt() == null) {
            note.setUpdatedAt(now);
        }

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, note.getTitle());
            stmt.setString(2, note.getContent());
            stmt.setString(3, note.getSubject());
            stmt.setString(4, note.getDifficulty());
            stmt.setString(5, DateUtil.formatDateTime(note.getCreatedAt()));
            stmt.setString(6, DateUtil.formatDateTime(note.getUpdatedAt()));

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new DatabaseException("Creating note failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    note.setId(generatedKeys.getInt(1));
                } else {
                    throw new DatabaseException("Creating note failed, no ID obtained.");
                }
            }

            return note;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating note: " + e.getMessage(), e);
            throw new DatabaseException("Failed to insert note into database", e);
        }
    }

    /**
     * Finds a note by its ID.
     *
     * @param id The note ID.
     * @return Optional containing the Note if found.
     */
    public Optional<Note> findById(int id) {
        String sql = "SELECT id, title, content, subject, difficulty, created_at, updated_at FROM notes WHERE id = ?;";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToNote(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding note by ID: " + id, e);
            throw new DatabaseException("Failed to query note with ID " + id, e);
        }
    }

    /**
     * Retrieves all notes ordered by updated_at descending.
     *
     * @return List of all notes.
     */
    public List<Note> findAll() {
        String sql = "SELECT id, title, content, subject, difficulty, created_at, updated_at FROM notes ORDER BY updated_at DESC;";
        List<Note> notes = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                notes.add(mapResultSetToNote(rs));
            }
            return notes;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding all notes: " + e.getMessage(), e);
            throw new DatabaseException("Failed to query all notes", e);
        }
    }

    /**
     * Updates an existing note.
     *
     * @param note The note containing updated fields.
     * @return True if updated successfully, false if not found.
     */
    public boolean update(Note note) {
        String sql = """
                UPDATE notes
                SET title = ?, content = ?, subject = ?, difficulty = ?, updated_at = ?
                WHERE id = ?;
                """;

        note.setUpdatedAt(LocalDateTime.now());

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, note.getTitle());
            stmt.setString(2, note.getContent());
            stmt.setString(3, note.getSubject());
            stmt.setString(4, note.getDifficulty());
            stmt.setString(5, DateUtil.formatDateTime(note.getUpdatedAt()));
            stmt.setInt(6, note.getId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating note ID " + note.getId(), e);
            throw new DatabaseException("Failed to update note with ID " + note.getId(), e);
        }
    }

    /**
     * Deletes a note by its ID.
     *
     * @param id The ID of the note to delete.
     * @return True if deleted, false if not found.
     */
    public boolean delete(int id) {
        String sql = "DELETE FROM notes WHERE id = ?;";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting note ID " + id, e);
            throw new DatabaseException("Failed to delete note with ID " + id, e);
        }
    }

    /**
     * Returns the total count of notes in the database.
     *
     * @return Total count.
     */
    public int count() {
        String sql = "SELECT COUNT(*) FROM notes;";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error counting notes: " + e.getMessage(), e);
            throw new DatabaseException("Failed to count notes", e);
        }
    }

    private Note mapResultSetToNote(ResultSet rs) throws SQLException {
        Note note = new Note();
        note.setId(rs.getInt("id"));
        note.setTitle(rs.getString("title"));
        note.setContent(rs.getString("content"));
        note.setSubject(rs.getString("subject"));
        note.setDifficulty(rs.getString("difficulty"));
        note.setCreatedAt(DateUtil.parseDateTime(rs.getString("created_at")));
        note.setUpdatedAt(DateUtil.parseDateTime(rs.getString("updated_at")));
        return note;
    }
}
