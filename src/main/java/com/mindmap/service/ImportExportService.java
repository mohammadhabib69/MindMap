package com.mindmap.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mindmap.database.DatabaseException;
import com.mindmap.database.DatabaseManager;
import com.mindmap.export.JsonUtil;
import com.mindmap.export.dto.ExportConnection;
import com.mindmap.export.dto.ExportLearningEvent;
import com.mindmap.export.dto.ExportNote;
import com.mindmap.export.dto.ExportRevision;
import com.mindmap.export.dto.ExportTag;
import com.mindmap.export.dto.MindMapExport;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ImportExportService {
    private static final Logger LOGGER = Logger.getLogger(ImportExportService.class.getName());
    private final ObjectMapper mapper = JsonUtil.getMapper();

    /**
     * Exports the entire database to a MindMapExport DTO.
     */
    public MindMapExport exportData() {
        MindMapExport export = new MindMapExport();
        export.setExportedAt(LocalDateTime.now());

        Map<Integer, String> noteIdToExportId = new HashMap<>();

        try (Connection conn = DatabaseManager.getConnection()) {
            // Export Tags
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT name FROM tags")) {
                while (rs.next()) {
                    export.getTags().add(new ExportTag(rs.getString("name")));
                }
            }

            // Export Notes
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT * FROM notes")) {
                while (rs.next()) {
                    ExportNote en = new ExportNote();
                    int sqliteId = rs.getInt("id");
                    String exportId = "note-" + sqliteId;
                    noteIdToExportId.put(sqliteId, exportId);

                    en.setExportId(exportId);
                    en.setTitle(rs.getString("title"));
                    en.setContent(rs.getString("content"));
                    en.setSubject(rs.getString("subject"));
                    en.setDifficulty(rs.getString("difficulty"));
                    en.setCreatedAt(com.mindmap.util.DateUtil.parseDateTime(rs.getString("created_at")));
                    en.setUpdatedAt(com.mindmap.util.DateUtil.parseDateTime(rs.getString("updated_at")));

                    // Fetch tags for this note
                    try (PreparedStatement tagStmt = conn.prepareStatement(
                            "SELECT t.name FROM tags t INNER JOIN note_tags nt ON t.id = nt.tag_id WHERE nt.note_id = ?")) {
                        tagStmt.setInt(1, sqliteId);
                        ResultSet tagRs = tagStmt.executeQuery();
                        List<String> tags = new ArrayList<>();
                        while (tagRs.next()) {
                            tags.add(tagRs.getString("name"));
                        }
                        en.setTags(tags);
                    }
                    export.getNotes().add(en);
                }
            }

            // Export Connections
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT from_note_id, to_note_id, relation FROM connections")) {
                while (rs.next()) {
                    ExportConnection ec = new ExportConnection();
                    ec.setFromNoteExportId(noteIdToExportId.get(rs.getInt("from_note_id")));
                    ec.setToNoteExportId(noteIdToExportId.get(rs.getInt("to_note_id")));
                    ec.setRelation(rs.getString("relation"));
                    if (ec.getFromNoteExportId() != null && ec.getToNoteExportId() != null) {
                        export.getConnections().add(ec);
                    }
                }
            }

            // Export Revisions
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT note_id, review_date, status, interval_days, created_at FROM revisions")) {
                while (rs.next()) {
                    ExportRevision er = new ExportRevision();
                    er.setNoteExportId(noteIdToExportId.get(rs.getInt("note_id")));
                    if (rs.getString("review_date") != null) {
                        er.setReviewDate(java.time.LocalDate.parse(rs.getString("review_date").substring(0, 10)));
                    }
                    er.setStatus(rs.getString("status"));
                    er.setIntervalDays(rs.getInt("interval_days"));
                    er.setCreatedAt(com.mindmap.util.DateUtil.parseDateTime(rs.getString("created_at")));
                    if (er.getNoteExportId() != null) {
                        export.getRevisions().add(er);
                    }
                }
            }

            // Export Learning Events
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT note_id, event_type, event_date, description FROM learning_events")) {
                while (rs.next()) {
                    ExportLearningEvent ele = new ExportLearningEvent();
                    int noteId = rs.getInt("note_id");
                    if (!rs.wasNull()) {
                        ele.setNoteExportId(noteIdToExportId.get(noteId));
                    }
                    ele.setEventType(rs.getString("event_type"));
                    ele.setEventDate(com.mindmap.util.DateUtil.parseDateTime(rs.getString("event_date")));
                    ele.setDescription(rs.getString("description"));
                    export.getLearningEvents().add(ele);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to export data from database", e);
            throw new RuntimeException("Export failed", e);
        }

        return export;
    }

    public void exportToFile(File file) throws IOException {
        MindMapExport data = exportData();
        mapper.writeValue(file, data);
    }

    public MindMapExport validateAndPreview(File file) throws IOException {
        MindMapExport data = mapper.readValue(file, MindMapExport.class);
        if (data.getFormatVersion() != 1) {
            throw new IllegalArgumentException("Unsupported format version: " + data.getFormatVersion());
        }
        return data;
    }

    /**
     * Safely merges imported data into the existing database using a transaction.
     */
    public void importData(MindMapExport data) throws Exception {
        if (data.getFormatVersion() != 1) {
            throw new IllegalArgumentException("Unsupported format version: " + data.getFormatVersion());
        }

        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 1. Ensure all Tags exist and cache their IDs
                Map<String, Integer> tagNameToId = new HashMap<>();
                for (ExportTag et : data.getTags()) {
                    int tagId = getOrCreateTag(conn, et.getName());
                    tagNameToId.put(et.getName(), tagId);
                }

                // 2. Process Notes (Merge by Title)
                Map<String, Integer> exportIdToSqliteId = new HashMap<>();
                for (ExportNote en : data.getNotes()) {
                    if (en.getTitle() == null || en.getTitle().trim().isEmpty()) {
                        throw new IllegalArgumentException("Note title cannot be empty");
                    }
                    
                    int noteId = getNoteIdByTitle(conn, en.getTitle());
                    if (noteId == -1) {
                        // Insert new note
                        noteId = insertNote(conn, en);
                    } else {
                        // Merge/update existing note
                        updateNote(conn, noteId, en);
                    }
                    exportIdToSqliteId.put(en.getExportId(), noteId);

                    // Re-link tags
                    if (en.getTags() != null) {
                        for (String tagName : en.getTags()) {
                            int tagId = tagNameToId.computeIfAbsent(tagName, k -> {
                                try {
                                    return getOrCreateTag(conn, k);
                                } catch (SQLException e) {
                                    throw new RuntimeException(e);
                                }
                            });
                            linkNoteTag(conn, noteId, tagId);
                        }
                    }
                }

                // 3. Process Connections
                for (ExportConnection ec : data.getConnections()) {
                    Integer fromId = exportIdToSqliteId.get(ec.getFromNoteExportId());
                    Integer toId = exportIdToSqliteId.get(ec.getToNoteExportId());
                    if (fromId != null && toId != null && !fromId.equals(toId)) {
                        insertConnectionIgnoreDuplicate(conn, fromId, toId, ec.getRelation());
                    }
                }

                // 4. Process Revisions (only if a revision for this note doesn't already exist to avoid duplication, or overwrite?)
                // Strategy: if revision exists for note, we skip. Otherwise insert.
                for (ExportRevision er : data.getRevisions()) {
                    Integer noteId = exportIdToSqliteId.get(er.getNoteExportId());
                    if (noteId != null) {
                        if (!revisionExistsForNote(conn, noteId)) {
                            insertRevision(conn, noteId, er);
                        }
                    }
                }

                // 5. Process Learning Events
                // Strategy: just insert them, maybe check for exact duplicate to avoid exponential growth
                for (ExportLearningEvent ele : data.getLearningEvents()) {
                    Integer noteId = ele.getNoteExportId() != null ? exportIdToSqliteId.get(ele.getNoteExportId()) : null;
                    if (!eventExists(conn, noteId, ele)) {
                        insertLearningEvent(conn, noteId, ele);
                    }
                }

                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                LOGGER.log(Level.SEVERE, "Transaction failed during import, rolling back.", e);
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    private int getOrCreateTag(Connection conn, String name) throws SQLException {
        try (PreparedStatement sel = conn.prepareStatement("SELECT id FROM tags WHERE name = ?")) {
            sel.setString(1, name);
            ResultSet rs = sel.executeQuery();
            if (rs.next()) return rs.getInt(1);
        }
        try (PreparedStatement ins = conn.prepareStatement("INSERT INTO tags (name) VALUES (?)", Statement.RETURN_GENERATED_KEYS)) {
            ins.setString(1, name);
            ins.executeUpdate();
            ResultSet rs = ins.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);
        }
        throw new SQLException("Failed to create tag: " + name);
    }

    private int getNoteIdByTitle(Connection conn, String title) throws SQLException {
        try (PreparedStatement sel = conn.prepareStatement("SELECT id FROM notes WHERE title = ? LIMIT 1")) {
            sel.setString(1, title);
            ResultSet rs = sel.executeQuery();
            if (rs.next()) return rs.getInt(1);
        }
        return -1;
    }

    private int insertNote(Connection conn, ExportNote en) throws SQLException {
        String sql = "INSERT INTO notes (title, content, subject, difficulty, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, en.getTitle());
            stmt.setString(2, en.getContent());
            stmt.setString(3, en.getSubject());
            stmt.setString(4, en.getDifficulty());
            stmt.setString(5, com.mindmap.util.DateUtil.formatDateTime(en.getCreatedAt()));
            stmt.setString(6, com.mindmap.util.DateUtil.formatDateTime(en.getUpdatedAt()));
            stmt.executeUpdate();
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);
        }
        throw new SQLException("Failed to insert note: " + en.getTitle());
    }

    private void updateNote(Connection conn, int id, ExportNote en) throws SQLException {
        String sql = "UPDATE notes SET content = ?, subject = ?, difficulty = ?, updated_at = ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, en.getContent());
            stmt.setString(2, en.getSubject());
            stmt.setString(3, en.getDifficulty());
            stmt.setString(4, com.mindmap.util.DateUtil.formatDateTime(en.getUpdatedAt()));
            stmt.setInt(5, id);
            stmt.executeUpdate();
        }
    }

    private void linkNoteTag(Connection conn, int noteId, int tagId) throws SQLException {
        try (PreparedStatement sel = conn.prepareStatement("SELECT 1 FROM note_tags WHERE note_id = ? AND tag_id = ?")) {
            sel.setInt(1, noteId);
            sel.setInt(2, tagId);
            if (sel.executeQuery().next()) return; // Already linked
        }
        try (PreparedStatement ins = conn.prepareStatement("INSERT INTO note_tags (note_id, tag_id) VALUES (?, ?)")) {
            ins.setInt(1, noteId);
            ins.setInt(2, tagId);
            ins.executeUpdate();
        }
    }

    private void insertConnectionIgnoreDuplicate(Connection conn, int fromId, int toId, String relation) throws SQLException {
        try (PreparedStatement sel = conn.prepareStatement("SELECT 1 FROM connections WHERE from_note_id = ? AND to_note_id = ?")) {
            sel.setInt(1, fromId);
            sel.setInt(2, toId);
            if (sel.executeQuery().next()) return; // Already exists
        }
        try (PreparedStatement ins = conn.prepareStatement("INSERT INTO connections (from_note_id, to_note_id, relation) VALUES (?, ?, ?)")) {
            ins.setInt(1, fromId);
            ins.setInt(2, toId);
            ins.setString(3, relation);
            ins.executeUpdate();
        }
    }

    private boolean revisionExistsForNote(Connection conn, int noteId) throws SQLException {
        try (PreparedStatement sel = conn.prepareStatement("SELECT 1 FROM revisions WHERE note_id = ? LIMIT 1")) {
            sel.setInt(1, noteId);
            return sel.executeQuery().next();
        }
    }

    private void insertRevision(Connection conn, int noteId, ExportRevision er) throws SQLException {
        String sql = "INSERT INTO revisions (note_id, review_date, status, interval_days, created_at) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, noteId);
            stmt.setString(2, er.getReviewDate() != null ? er.getReviewDate().toString() : null);
            stmt.setString(3, er.getStatus());
            stmt.setInt(4, er.getIntervalDays());
            stmt.setString(5, com.mindmap.util.DateUtil.formatDateTime(er.getCreatedAt()));
            stmt.executeUpdate();
        }
    }

    private boolean eventExists(Connection conn, Integer noteId, ExportLearningEvent ele) throws SQLException {
        String sql;
        if (noteId == null) {
            sql = "SELECT 1 FROM learning_events WHERE note_id IS NULL AND event_type = ? AND event_date = ? LIMIT 1";
        } else {
            sql = "SELECT 1 FROM learning_events WHERE note_id = ? AND event_type = ? AND event_date = ? LIMIT 1";
        }
        try (PreparedStatement sel = conn.prepareStatement(sql)) {
            if (noteId == null) {
                sel.setString(1, ele.getEventType());
                sel.setString(2, com.mindmap.util.DateUtil.formatDateTime(ele.getEventDate()));
            } else {
                sel.setInt(1, noteId);
                sel.setString(2, ele.getEventType());
                sel.setString(3, com.mindmap.util.DateUtil.formatDateTime(ele.getEventDate()));
            }
            return sel.executeQuery().next();
        }
    }

    private void insertLearningEvent(Connection conn, Integer noteId, ExportLearningEvent ele) throws SQLException {
        String sql = "INSERT INTO learning_events (note_id, event_type, event_date, description) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (noteId != null) {
                stmt.setInt(1, noteId);
            } else {
                stmt.setNull(1, java.sql.Types.INTEGER);
            }
            stmt.setString(2, ele.getEventType());
            stmt.setString(3, com.mindmap.util.DateUtil.formatDateTime(ele.getEventDate()));
            stmt.setString(4, ele.getDescription());
            stmt.executeUpdate();
        }
    }


}
