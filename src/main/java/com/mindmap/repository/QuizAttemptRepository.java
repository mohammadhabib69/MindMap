package com.mindmap.repository;

import com.mindmap.database.DatabaseException;
import com.mindmap.database.DatabaseManager;
import com.mindmap.model.BankQuestion;
import com.mindmap.model.Note;
import com.mindmap.model.QuizAttempt;
import com.mindmap.model.QuizAttemptQuestion;
import com.mindmap.util.DateUtil;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class QuizAttemptRepository {
    private static final Logger LOGGER = Logger.getLogger(QuizAttemptRepository.class.getName());

    public QuizAttempt create(QuizAttempt attempt) {
        String sql = """
                INSERT INTO quiz_attempts (mode, question_count, time_limit_seconds, started_at, completed_at, score, correct_count, incorrect_count, unanswered_count)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);
                """;
        if (attempt.getStartedAt() == null) attempt.setStartedAt(LocalDateTime.now());

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, attempt.getMode());
            stmt.setInt(2, attempt.getQuestionCount());
            stmt.setInt(3, attempt.getTimeLimitSeconds());
            stmt.setString(4, DateUtil.formatDateTime(attempt.getStartedAt()));
            stmt.setString(5, attempt.getCompletedAt() != null ? DateUtil.formatDateTime(attempt.getCompletedAt()) : null);
            stmt.setInt(6, attempt.getScore());
            stmt.setInt(7, attempt.getCorrectCount());
            stmt.setInt(8, attempt.getIncorrectCount());
            stmt.setInt(9, attempt.getUnansweredCount());

            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) attempt.setId(rs.getInt(1));
            }
            return attempt;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating attempt", e);
            throw new DatabaseException("Failed to insert attempt", e);
        }
    }

    public boolean update(QuizAttempt attempt) {
        String sql = """
                UPDATE quiz_attempts
                SET completed_at = ?, score = ?, correct_count = ?, incorrect_count = ?, unanswered_count = ?
                WHERE id = ?;
                """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, attempt.getCompletedAt() != null ? DateUtil.formatDateTime(attempt.getCompletedAt()) : null);
            stmt.setInt(2, attempt.getScore());
            stmt.setInt(3, attempt.getCorrectCount());
            stmt.setInt(4, attempt.getIncorrectCount());
            stmt.setInt(5, attempt.getUnansweredCount());
            stmt.setInt(6, attempt.getId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating attempt", e);
            throw new DatabaseException("Failed to update attempt", e);
        }
    }

    
    public List<QuizAttempt> findAll() {
        List<QuizAttempt> list = new ArrayList<>();
        String sql = "SELECT * FROM quiz_attempts ORDER BY completed_at DESC, started_at DESC LIMIT 20";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                QuizAttempt a = new QuizAttempt();
                a.setId(rs.getInt("id"));
                a.setMode(rs.getString("mode"));
                a.setQuestionCount(rs.getInt("question_count"));
                a.setTimeLimitSeconds(rs.getInt("time_limit_seconds"));
                String started = rs.getString("started_at");
                if (started != null && !started.isEmpty()) a.setStartedAt(DateUtil.parseDateTime(started));
                String completed = rs.getString("completed_at");
                if (completed != null && !completed.isEmpty()) a.setCompletedAt(DateUtil.parseDateTime(completed));
                a.setScore(rs.getInt("score"));
                a.setCorrectCount(rs.getInt("correct_count"));
                a.setIncorrectCount(rs.getInt("incorrect_count"));
                a.setUnansweredCount(rs.getInt("unanswered_count"));
                list.add(a);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching attempts", e);
        }
        return list;
    }

    public void addAttemptQuestion(QuizAttemptQuestion qaq) {
        String sql = """
                INSERT INTO quiz_attempt_questions (attempt_id, question_id, question_order, user_answer_json, is_correct)
                VALUES (?, ?, ?, ?, ?);
                """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, qaq.getAttemptId());
            stmt.setInt(2, qaq.getQuestionId());
            stmt.setInt(3, qaq.getQuestionOrder());
            stmt.setString(4, qaq.getUserAnswerJson());
            stmt.setInt(5, qaq.isCorrect() ? 1 : 0);

            stmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error inserting attempt question", e);
        }
    }
}
