package com.mindmap.repository;

import com.mindmap.database.DatabaseException;
import com.mindmap.database.DatabaseManager;
import com.mindmap.model.QuizQuestion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class QuizQuestionRepository {
    private static final Logger LOGGER = Logger.getLogger(QuizQuestionRepository.class.getName());

    public QuizQuestion create(QuizQuestion q) {
        String sql = """
                INSERT INTO quiz_questions (quiz_session_id, source_note_id, question_text, question_type, options_json, correct_answer_json, user_answer_json, question_order, is_correct)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);
                """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, q.getQuizSessionId());
            stmt.setInt(2, q.getSourceNoteId());
            stmt.setString(3, q.getQuestionText());
            stmt.setString(4, q.getQuestionType());
            stmt.setString(5, q.getOptionsJson());
            stmt.setString(6, q.getCorrectAnswerJson());
            stmt.setString(7, q.getUserAnswerJson());
            stmt.setInt(8, q.getQuestionOrder());
            stmt.setInt(9, q.isCorrect() ? 1 : 0);

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new DatabaseException("Creating quiz question failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    q.setId(generatedKeys.getInt(1));
                }
            }
            return q;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating quiz question: " + e.getMessage(), e);
            throw new DatabaseException("Failed to insert quiz question", e);
        }
    }

    public boolean update(QuizQuestion q) {
        String sql = """
                UPDATE quiz_questions
                SET user_answer_json = ?, is_correct = ?
                WHERE id = ?;
                """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, q.getUserAnswerJson());
            stmt.setInt(2, q.isCorrect() ? 1 : 0);
            stmt.setInt(3, q.getId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating quiz question ID: " + q.getId(), e);
            throw new DatabaseException("Failed to update quiz question", e);
        }
    }

    public List<QuizQuestion> findBySessionId(int sessionId) {
        String sql = "SELECT * FROM quiz_questions WHERE quiz_session_id = ? ORDER BY question_order ASC;";
        List<QuizQuestion> list = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, sessionId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    QuizQuestion q = new QuizQuestion();
                    q.setId(rs.getInt("id"));
                    q.setQuizSessionId(rs.getInt("quiz_session_id"));
                    q.setSourceNoteId(rs.getInt("source_note_id"));
                    q.setQuestionText(rs.getString("question_text"));
                    q.setQuestionType(rs.getString("question_type"));
                    q.setOptionsJson(rs.getString("options_json"));
                    q.setCorrectAnswerJson(rs.getString("correct_answer_json"));
                    q.setUserAnswerJson(rs.getString("user_answer_json"));
                    q.setQuestionOrder(rs.getInt("question_order"));
                    q.setCorrect(rs.getInt("is_correct") == 1);
                    list.add(q);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching quiz questions: " + e.getMessage(), e);
        }
        return list;
    }
}
