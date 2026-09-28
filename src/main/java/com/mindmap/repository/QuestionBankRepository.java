package com.mindmap.repository;

import com.mindmap.database.DatabaseException;
import com.mindmap.database.DatabaseManager;
import com.mindmap.model.BankQuestion;
import com.mindmap.model.Note;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class QuestionBankRepository {
    private static final Logger LOGGER = Logger.getLogger(QuestionBankRepository.class.getName());
    private final NoteRepository noteRepo = new NoteRepository();

    public BankQuestion create(BankQuestion q) {
        String sql = """
                INSERT INTO question_bank (source_note_id, question_text, question_type, options_json, correct_answer_json, explanation, subject, topic, difficulty, created_at, times_studied, times_asked)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, q.getSourceNoteId());
            stmt.setString(2, q.getQuestionText());
            stmt.setString(3, q.getQuestionType());
            stmt.setString(4, q.getOptionsJson());
            stmt.setString(5, q.getCorrectAnswerJson());
            stmt.setString(6, q.getExplanation());
            stmt.setString(7, q.getSubject());
            stmt.setString(8, q.getTopic());
            stmt.setString(9, q.getDifficulty());
            stmt.setString(10, q.getCreatedAt());
            stmt.setInt(11, q.getTimesStudied());
            stmt.setInt(12, q.getTimesAsked());

            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) q.setId(rs.getInt(1));
            }
            return q;
        } catch (SQLException e) {
            // Ignore unique constraint violation if question already exists
            if (e.getMessage().contains("UNIQUE constraint failed")) {
                LOGGER.info("Duplicate question skipped.");
                return null;
            }
            LOGGER.log(Level.SEVERE, "Failed to create bank question", e);
            throw new DatabaseException("Failed to insert bank question", e);
        }
    }

    public List<BankQuestion> findAll() {
        return findByFilter(null, null, null, null, null);
    }

    public List<BankQuestion> findByFilter(String search, String subject, String topic, String difficulty, String type) {
        StringBuilder sql = new StringBuilder("SELECT q.*, n.title as note_title, n.content as note_content FROM question_bank q JOIN notes n ON q.source_note_id = n.id WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (search != null && !search.trim().isEmpty()) {
            sql.append(" AND q.question_text LIKE ?");
            params.add("%" + search.trim() + "%");
        }
        if (subject != null && !subject.equals("All Subjects")) {
            sql.append(" AND q.subject = ?");
            params.add(subject);
        }
        if (topic != null && !topic.equals("All Topics")) {
            sql.append(" AND q.topic = ?");
            params.add(topic);
        }
        if (difficulty != null && !difficulty.equals("All Difficulties") && !difficulty.equals("Mixed")) {
            sql.append(" AND q.difficulty = ?");
            params.add(difficulty);
        }
        if (type != null && !type.equals("All Types") && !type.equals("Mixed")) {
            sql.append(" AND q.question_type = ?");
            if (type.equals("Single Choice")) params.add("SINGLE_CHOICE");
            else if (type.equals("Multiple Choice")) params.add("MULTIPLE_CHOICE");
            else if (type.equals("True / False")) params.add("TRUE_FALSE");
            else params.add(type);
        }
        sql.append(" ORDER BY q.created_at DESC");

        List<BankQuestion> list = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching question bank", e);
        }
        return list;
    }

    private BankQuestion mapResultSet(ResultSet rs) throws SQLException {
        BankQuestion q = new BankQuestion();
        q.setId(rs.getInt("id"));
        q.setSourceNoteId(rs.getInt("source_note_id"));
        q.setQuestionText(rs.getString("question_text"));
        q.setQuestionType(rs.getString("question_type"));
        q.setOptionsJson(rs.getString("options_json"));
        q.setCorrectAnswerJson(rs.getString("correct_answer_json"));
        q.setExplanation(rs.getString("explanation"));
        q.setSubject(rs.getString("subject"));
        q.setTopic(rs.getString("topic"));
        q.setDifficulty(rs.getString("difficulty"));
        q.setCreatedAt(rs.getString("created_at"));
        q.setTimesStudied(rs.getInt("times_studied"));
        q.setTimesAsked(rs.getInt("times_asked"));
        
        Note n = new Note();
        n.setId(q.getSourceNoteId());
        n.setTitle(rs.getString("note_title"));
        n.setContent(rs.getString("note_content"));
        q.setSourceNote(n);
        
        return q;
    }
}
