package com.mindmap.repository;

import com.mindmap.database.DatabaseException;
import com.mindmap.database.DatabaseManager;
import com.mindmap.model.Tag;

import java.sql.Connection;
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
 * Repository handling SQLite database operations for Tag entities.
 */
public class TagRepository {

    private static final Logger LOGGER = Logger.getLogger(TagRepository.class.getName());

    /**
     * Inserts a new Tag and sets its generated ID.
     * If the tag already exists by name, returns the existing tag without creating a duplicate.
     *
     * @param tag The tag to create.
     * @return The created or existing Tag.
     */
    public Tag create(Tag tag) {
        // First check if tag already exists to respect UNIQUE constraint
        Optional<Tag> existing = findByName(tag.getName());
        if (existing.isPresent()) {
            tag.setId(existing.get().getId());
            return tag;
        }

        String sql = "INSERT INTO tags (name) VALUES (?);";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, tag.getName().trim());
            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    tag.setId(generatedKeys.getInt(1));
                } else {
                    throw new DatabaseException("Creating tag failed, no ID obtained.");
                }
            }

            return tag;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating tag: " + e.getMessage(), e);
            throw new DatabaseException("Failed to insert tag into database", e);
        }
    }

    /**
     * Finds a tag by its ID.
     */
    public Optional<Tag> findById(int id) {
        String sql = "SELECT id, name FROM tags WHERE id = ?;";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new Tag(rs.getInt("id"), rs.getString("name")));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding tag by ID: " + id, e);
            throw new DatabaseException("Failed to query tag with ID " + id, e);
        }
    }

    /**
     * Finds a tag by its name.
     */
    public Optional<Tag> findByName(String name) {
        String sql = "SELECT id, name FROM tags WHERE name = ?;";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name.trim());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new Tag(rs.getInt("id"), rs.getString("name")));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding tag by name: " + name, e);
            throw new DatabaseException("Failed to query tag with name " + name, e);
        }
    }

    /**
     * Retrieves all tags ordered by name.
     */
    public List<Tag> findAll() {
        String sql = "SELECT id, name FROM tags ORDER BY name ASC;";
        List<Tag> tags = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                tags.add(new Tag(rs.getInt("id"), rs.getString("name")));
            }
            return tags;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding all tags: " + e.getMessage(), e);
            throw new DatabaseException("Failed to query all tags", e);
        }
    }

    /**
     * Deletes a tag by its ID.
     */
    public boolean delete(int id) {
        String sql = "DELETE FROM tags WHERE id = ?;";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting tag ID: " + id, e);
            throw new DatabaseException("Failed to delete tag with ID " + id, e);
        }
    }

    /**
     * Links a tag to a note in note_tags table.
     */
    public void addTagToNote(int noteId, int tagId) {
        String sql = "INSERT OR IGNORE INTO note_tags (note_id, tag_id) VALUES (?, ?);";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, noteId);
            stmt.setInt(2, tagId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error linking tag ID " + tagId + " to note ID " + noteId, e);
            throw new DatabaseException("Failed to link tag to note", e);
        }
    }

    /**
     * Removes a tag link from a note in note_tags table.
     */
    public void removeTagFromNote(int noteId, int tagId) {
        String sql = "DELETE FROM note_tags WHERE note_id = ? AND tag_id = ?;";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, noteId);
            stmt.setInt(2, tagId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error unlinking tag ID " + tagId + " from note ID " + noteId, e);
            throw new DatabaseException("Failed to unlink tag from note", e);
        }
    }

    /**
     * Retrieves all tags associated with a specific note.
     */
    public List<Tag> findTagsByNoteId(int noteId) {
        String sql = """
                SELECT t.id, t.name
                FROM tags t
                JOIN note_tags nt ON t.id = nt.tag_id
                WHERE nt.note_id = ?
                ORDER BY t.name ASC;
                """;
        List<Tag> tags = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, noteId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    tags.add(new Tag(rs.getInt("id"), rs.getString("name")));
                }
            }
            return tags;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error querying tags for note ID " + noteId, e);
            throw new DatabaseException("Failed to query tags for note", e);
        }
    }
}
