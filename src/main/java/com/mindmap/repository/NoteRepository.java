package com.mindmap.repository;

import com.mindmap.database.DatabaseException;
import com.mindmap.database.DatabaseManager;
import com.mindmap.model.ConnectionFilterPreset;
import com.mindmap.model.DateFilterPreset;
import com.mindmap.model.Note;
import com.mindmap.model.SearchCriteria;
import com.mindmap.model.Tag;
import com.mindmap.util.DateUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Repository handling SQLite database operations for Note entities.
 */
public class NoteRepository {

    private static final Logger LOGGER = Logger.getLogger(NoteRepository.class.getName());
    private final TagRepository tagRepository;

    public NoteRepository() {
        this(new TagRepository());
    }

    public NoteRepository(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

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

    /**
     * Returns the count of distinct non-empty subjects across all notes.
     *
     * @return Distinct subjects count.
     */
    public int countDistinctSubjects() {
        String sql = "SELECT COUNT(DISTINCT TRIM(subject)) FROM notes WHERE subject IS NOT NULL AND TRIM(subject) <> '';";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error counting distinct subjects: " + e.getMessage(), e);
            throw new DatabaseException("Failed to count distinct subjects", e);
        }
    }

    /**
     * Retrieves a sorted list of all distinct, non-empty subjects across notes.
     *
     * @return Alphabetically ordered list of distinct subject names.
     */
    public List<String> findAllDistinctSubjects() {
        String sql = "SELECT DISTINCT TRIM(subject) AS subject_name FROM notes WHERE subject IS NOT NULL AND TRIM(subject) <> '' ORDER BY TRIM(subject) COLLATE NOCASE ASC;";
        List<String> subjects = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                subjects.add(rs.getString("subject_name"));
            }
            return subjects;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error querying distinct subjects: " + e.getMessage(), e);
            throw new DatabaseException("Failed to query distinct subjects", e);
        }
    }

    /**
     * Atomically persists a Note along with its associated tags in a single database transaction.
     *
     * @param note The note to persist.
     * @param tagNames List of tag names to link to this note.
     * @return The persisted Note with its generated ID and populated tags.
     */
    public Note createWithTags(Note note, List<String> tagNames) {
        String insertNoteSql = """
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

        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 1. Insert note
                try (PreparedStatement stmt = conn.prepareStatement(insertNoteSql, Statement.RETURN_GENERATED_KEYS)) {
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
                }

                // 2. Insert tags & links
                List<Tag> persistedTags = new ArrayList<>();
                if (tagNames != null && !tagNames.isEmpty()) {
                    Set<String> processedNames = new HashSet<>();
                    for (String rawName : tagNames) {
                        if (rawName == null || rawName.trim().isEmpty()) {
                            continue;
                        }
                        String cleanName = rawName.trim();
                        if (!processedNames.add(cleanName.toLowerCase())) {
                            continue; // avoid duplicate tags within the same note
                        }
                        Tag tag = tagRepository.create(new Tag(cleanName), conn);
                        tagRepository.addTagToNote(note.getId(), tag.getId(), conn);
                        persistedTags.add(tag);
                    }
                }
                note.setTags(persistedTags);

                conn.commit();
                return note;
            } catch (Exception e) {
                conn.rollback();
                LOGGER.log(Level.SEVERE, "Transaction rolled back while creating note: " + e.getMessage(), e);
                if (e instanceof DatabaseException) {
                    throw (DatabaseException) e;
                }
                throw new DatabaseException("Failed to create note with tags in transaction", e);
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error in createWithTags: " + e.getMessage(), e);
            throw new DatabaseException("Database connection error in createWithTags", e);
        }
    }

    /**
     * Atomically updates a Note and synchronizes its associated tags in a single database transaction.
     *
     * @param note The note to update.
     * @param tagNames The updated list of tag names.
     * @return True if updated successfully, false if note was not found.
     */
    public boolean updateWithTags(Note note, List<String> tagNames) {
        String updateNoteSql = """
                UPDATE notes
                SET title = ?, content = ?, subject = ?, difficulty = ?, updated_at = ?
                WHERE id = ?;
                """;

        note.setUpdatedAt(LocalDateTime.now());

        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 1. Update note
                boolean updated;
                try (PreparedStatement stmt = conn.prepareStatement(updateNoteSql)) {
                    stmt.setString(1, note.getTitle());
                    stmt.setString(2, note.getContent());
                    stmt.setString(3, note.getSubject());
                    stmt.setString(4, note.getDifficulty());
                    stmt.setString(5, DateUtil.formatDateTime(note.getUpdatedAt()));
                    stmt.setInt(6, note.getId());

                    updated = stmt.executeUpdate() > 0;
                }

                if (!updated) {
                    conn.rollback();
                    return false;
                }

                // 2. Remove old tags association for this note
                tagRepository.removeTagsForNote(note.getId(), conn);

                // 3. Link updated tags
                List<Tag> persistedTags = new ArrayList<>();
                if (tagNames != null && !tagNames.isEmpty()) {
                    Set<String> processedNames = new HashSet<>();
                    for (String rawName : tagNames) {
                        if (rawName == null || rawName.trim().isEmpty()) {
                            continue;
                        }
                        String cleanName = rawName.trim();
                        if (!processedNames.add(cleanName.toLowerCase())) {
                            continue;
                        }
                        Tag tag = tagRepository.create(new Tag(cleanName), conn);
                        tagRepository.addTagToNote(note.getId(), tag.getId(), conn);
                        persistedTags.add(tag);
                    }
                }
                note.setTags(persistedTags);

                conn.commit();
                return true;
            } catch (Exception e) {
                conn.rollback();
                LOGGER.log(Level.SEVERE, "Transaction rolled back while updating note: " + e.getMessage(), e);
                if (e instanceof DatabaseException) {
                    throw (DatabaseException) e;
                }
                throw new DatabaseException("Failed to update note with tags in transaction", e);
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error in updateWithTags: " + e.getMessage(), e);
            throw new DatabaseException("Database connection error in updateWithTags", e);
        }
    }

    /**
     * Retrieves a note by ID with its tags populated.
     */
    public Optional<Note> findByIdWithTags(int id) {
        String sql = """
                SELECT n.id, n.title, n.content, n.subject, n.difficulty, n.created_at, n.updated_at,
                       t.id AS tag_id, t.name AS tag_name
                FROM notes n
                LEFT JOIN note_tags nt ON n.id = nt.note_id
                LEFT JOIN tags t ON nt.tag_id = t.id
                WHERE n.id = ?
                ORDER BY t.name COLLATE NOCASE ASC;
                """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                List<Note> notes = mapResultSetToNotesList(rs);
                if (!notes.isEmpty()) {
                    return Optional.of(notes.get(0));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding note by ID with tags: " + id, e);
            throw new DatabaseException("Failed to query note with ID " + id, e);
        }
    }

    /**
     * Retrieves all notes ordered by updated_at descending, with their associated tags populated.
     */
    public List<Note> findAllWithTags() {
        return searchAndFilterWithTags(null, null);
    }

    /**
     * Searches notes by matching text in title, content, or subject (case-insensitive) with tags populated.
     */
    public List<Note> searchWithTags(String query) {
        return searchAndFilterWithTags(query, null);
    }

    /**
     * Filters notes associated with a specific tag name using relational tables.
     */
    public List<Note> findByTagWithTags(String tagName) {
        return searchAndFilterWithTags(null, tagName);
    }

    /**
     * Combined search and tag filter using relational joins and parameterized placeholders.
     */
    public List<Note> searchAndFilterWithTags(String query, String tagName) {
        boolean hasQuery = (query != null && !query.trim().isEmpty());
        boolean hasTag = (tagName != null && !tagName.trim().isEmpty() && !"All Tags".equalsIgnoreCase(tagName.trim()));

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT n.id, n.title, n.content, n.subject, n.difficulty, n.created_at, n.updated_at, ");
        sql.append("(SELECT COUNT(*) FROM connections c WHERE c.from_note_id = n.id OR c.to_note_id = n.id) AS connection_count, ");
        sql.append("t.id AS tag_id, t.name AS tag_name ");
        sql.append("FROM notes n ");

        if (hasTag) {
            sql.append("JOIN note_tags filter_nt ON n.id = filter_nt.note_id ");
            sql.append("JOIN tags filter_t ON filter_nt.tag_id = filter_t.id AND filter_t.name = ? COLLATE NOCASE ");
        }

        sql.append("LEFT JOIN note_tags nt ON n.id = nt.note_id ");
        sql.append("LEFT JOIN tags t ON nt.tag_id = t.id ");

        if (hasQuery) {
            sql.append("WHERE (n.title LIKE ? OR n.content LIKE ? OR n.subject LIKE ?) ");
        }

        sql.append("ORDER BY n.updated_at DESC, t.name COLLATE NOCASE ASC;");

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            int paramIndex = 1;
            if (hasTag) {
                stmt.setString(paramIndex++, tagName.trim());
            }
            if (hasQuery) {
                String wildcard = "%" + query.trim() + "%";
                stmt.setString(paramIndex++, wildcard);
                stmt.setString(paramIndex++, wildcard);
                stmt.setString(paramIndex++, wildcard);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                return mapResultSetToNotesList(rs);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error searching notes with tags: " + e.getMessage(), e);
            throw new DatabaseException("Failed to search notes with tags", e);
        }
    }

    /**
     * Advanced multi-criteria search combining keyword matching, subject, difficulty, tag,
     * date range, and connection status filters using logical AND semantics.
     *
     * @param criteria Multi-criteria search filter object.
     * @return List of matching Note entities with tags and connection counts.
     */
    public List<Note> searchByCriteria(SearchCriteria criteria) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT n.id, n.title, n.content, n.subject, n.difficulty, n.created_at, n.updated_at, ");
        sql.append("(SELECT COUNT(*) FROM connections c WHERE c.from_note_id = n.id OR c.to_note_id = n.id) AS connection_count, ");
        sql.append("t.id AS tag_id, t.name AS tag_name ");
        sql.append("FROM notes n ");
        sql.append("LEFT JOIN note_tags nt ON n.id = nt.note_id ");
        sql.append("LEFT JOIN tags t ON nt.tag_id = t.id ");

        List<String> whereClauses = new ArrayList<>();
        List<Object> params = new ArrayList<>();

        if (criteria != null) {
            // 1. Text Search across Title, Content, Subject, or Tags
            if (criteria.hasQuery()) {
                String queryWildcard = "%" + criteria.getQuery().trim().toLowerCase() + "%";
                whereClauses.add("""
                        (LOWER(n.title) LIKE ? OR LOWER(n.content) LIKE ? OR LOWER(n.subject) LIKE ? OR EXISTS (
                            SELECT 1 FROM note_tags nt_q
                            JOIN tags t_q ON nt_q.tag_id = t_q.id
                            WHERE nt_q.note_id = n.id AND LOWER(t_q.name) LIKE ?
                        ))
                        """);
                params.add(queryWildcard);
                params.add(queryWildcard);
                params.add(queryWildcard);
                params.add(queryWildcard);
            }

            // 2. Subject Filter
            if (criteria.hasSubject()) {
                whereClauses.add("LOWER(TRIM(n.subject)) = LOWER(?)");
                params.add(criteria.getSubject().trim());
            }

            // 3. Difficulty Filter
            if (criteria.hasDifficulty()) {
                whereClauses.add("UPPER(n.difficulty) = UPPER(?)");
                params.add(criteria.getDifficulty().trim());
            }

            // 4. Tag Filter
            if (criteria.hasTag()) {
                whereClauses.add("""
                        EXISTS (
                            SELECT 1 FROM note_tags nt_f
                            JOIN tags t_f ON nt_f.tag_id = t_f.id
                            WHERE nt_f.note_id = n.id AND LOWER(t_f.name) = LOWER(?)
                        )
                        """);
                params.add(criteria.getTag().trim());
            }

            // 5. Date Filter
            if (criteria.hasDateFilter()) {
                LocalDate today = LocalDate.now();
                LocalDateTime startDateTime = null;
                LocalDateTime endDateTime = null;

                DateFilterPreset preset = criteria.getDateFilter();
                switch (preset) {
                    case TODAY -> {
                        startDateTime = today.atStartOfDay();
                        endDateTime = today.atTime(LocalTime.MAX);
                    }
                    case LAST_7_DAYS -> {
                        startDateTime = today.minusDays(7).atStartOfDay();
                        endDateTime = today.atTime(LocalTime.MAX);
                    }
                    case LAST_30_DAYS -> {
                        startDateTime = today.minusDays(30).atStartOfDay();
                        endDateTime = today.atTime(LocalTime.MAX);
                    }
                    case CUSTOM -> {
                        if (criteria.getCustomStartDate() != null) {
                            startDateTime = criteria.getCustomStartDate().atStartOfDay();
                        }
                        if (criteria.getCustomEndDate() != null) {
                            endDateTime = criteria.getCustomEndDate().atTime(LocalTime.MAX);
                        }
                    }
                    default -> {}
                }

                if (startDateTime != null) {
                    whereClauses.add("n.updated_at >= ?");
                    params.add(DateUtil.formatDateTime(startDateTime));
                }
                if (endDateTime != null) {
                    whereClauses.add("n.updated_at <= ?");
                    params.add(DateUtil.formatDateTime(endDateTime));
                }
            }

            // 6. Connection Filter
            if (criteria.hasConnectionFilter()) {
                ConnectionFilterPreset connPreset = criteria.getConnectionFilter();
                switch (connPreset) {
                    case HAS_CONNECTIONS -> whereClauses.add("EXISTS (SELECT 1 FROM connections c WHERE c.from_note_id = n.id OR c.to_note_id = n.id)");
                    case NO_CONNECTIONS -> whereClauses.add("NOT EXISTS (SELECT 1 FROM connections c WHERE c.from_note_id = n.id OR c.to_note_id = n.id)");
                    default -> {}
                }
            }
        }

        if (!whereClauses.isEmpty()) {
            sql.append("WHERE ").append(String.join(" AND ", whereClauses)).append(" ");
        }

        sql.append("ORDER BY n.updated_at DESC, t.name COLLATE NOCASE ASC;");

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                return mapResultSetToNotesList(rs);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error searching notes by criteria: " + e.getMessage(), e);
            throw new DatabaseException("Failed to search notes by criteria", e);
        }
    }

    private List<Note> mapResultSetToNotesList(ResultSet rs) throws SQLException {
        Map<Integer, Note> noteMap = new LinkedHashMap<>();

        while (rs.next()) {
            int noteId = rs.getInt("id");
            Note note = noteMap.get(noteId);
            if (note == null) {
                note = new Note();
                note.setId(noteId);
                note.setTitle(rs.getString("title"));
                note.setContent(rs.getString("content"));
                note.setSubject(rs.getString("subject"));
                note.setDifficulty(rs.getString("difficulty"));
                note.setCreatedAt(DateUtil.parseDateTime(rs.getString("created_at")));
                note.setUpdatedAt(DateUtil.parseDateTime(rs.getString("updated_at")));
                try {
                    note.setConnectionCount(rs.getInt("connection_count"));
                } catch (SQLException ignored) {
                    // Column might not exist in older queries
                }
                noteMap.put(noteId, note);
            }

            int tagId = rs.getInt("tag_id");
            if (!rs.wasNull() && tagId > 0) {
                String tagName = rs.getString("tag_name");
                Tag tag = new Tag(tagId, tagName);
                note.addTag(tag);
            }
        }

        return new ArrayList<>(noteMap.values());
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
