package com.mindmap.repository;

import com.mindmap.database.DatabaseException;
import com.mindmap.database.DatabaseManager;
import com.mindmap.model.QuizSession;
import com.mindmap.util.DateUtil;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class QuizSessionRepository {
    private static final Logger LOGGER = Logger.getLogger(QuizSessionRepository.class.getName());

    public QuizSession create(QuizSession session) {
        String sql = """
                INSERT INTO quiz_sessions (title, mode, question_count, time_limit_seconds, started_at, completed_at, score, correct_count, incorrect_count, unanswered_count)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);
                """;

        if (session.getStartedAt() == null) {
            session.setStartedAt(LocalDateTime.now());
        }

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, session.getTitle());
            stmt.setString(2, session.getMode());
            stmt.setInt(3, session.getQuestionCount());
            stmt.setInt(4, session.getTimeLimitSeconds());
            stmt.setString(5, DateUtil.formatDateTime(session.getStartedAt()));
            stmt.setString(6, session.getCompletedAt() != null ? DateUtil.formatDateTime(session.getCompletedAt()) : null);
            stmt.setInt(7, session.getScore());
            stmt.setInt(8, session.getCorrectCount());
            stmt.setInt(9, session.getIncorrectCount());
            stmt.setInt(10, session.getUnansweredCount());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new DatabaseException("Creating quiz session failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    session.setId(generatedKeys.getInt(1));
                } else {
                    throw new DatabaseException("Creating quiz session failed, no ID obtained.");
                }
            }
            return session;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating quiz session: " + e.getMessage(), e);
            throw new DatabaseException("Failed to insert quiz session into database", e);
        }
    }

    public boolean update(QuizSession session) {
        String sql = """
                UPDATE quiz_sessions
                SET title = ?, mode = ?, question_count = ?, time_limit_seconds = ?, started_at = ?, completed_at = ?, score = ?, correct_count = ?, incorrect_count = ?, unanswered_count = ?
                WHERE id = ?;
                """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, session.getTitle());
            stmt.setString(2, session.getMode());
            stmt.setInt(3, session.getQuestionCount());
            stmt.setInt(4, session.getTimeLimitSeconds());
            stmt.setString(5, DateUtil.formatDateTime(session.getStartedAt()));
            stmt.setString(6, session.getCompletedAt() != null ? DateUtil.formatDateTime(session.getCompletedAt()) : null);
            stmt.setInt(7, session.getScore());
            stmt.setInt(8, session.getCorrectCount());
            stmt.setInt(9, session.getIncorrectCount());
            stmt.setInt(10, session.getUnansweredCount());
            stmt.setInt(11, session.getId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating quiz session ID: " + session.getId(), e);
            throw new DatabaseException("Failed to update quiz session", e);
        }
    }

    public List<QuizSession> findAllRecent() {
        String sql = "SELECT * FROM quiz_sessions ORDER BY started_at DESC LIMIT 50;";
        List<QuizSession> list = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToSession(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching quiz sessions: " + e.getMessage(), e);
        }
        return list;
    }

    private QuizSession mapResultSetToSession(ResultSet rs) throws SQLException {
        QuizSession s = new QuizSession();
        s.setId(rs.getInt("id"));
        s.setTitle(rs.getString("title"));
        s.setMode(rs.getString("mode"));
        s.setQuestionCount(rs.getInt("question_count"));
        s.setTimeLimitSeconds(rs.getInt("time_limit_seconds"));
        s.setStartedAt(DateUtil.parseDateTime(rs.getString("started_at")));
        if (rs.getString("completed_at") != null) {
            s.setCompletedAt(DateUtil.parseDateTime(rs.getString("completed_at")));
        }
        s.setScore(rs.getInt("score"));
        s.setCorrectCount(rs.getInt("correct_count"));
        s.setIncorrectCount(rs.getInt("incorrect_count"));
        s.setUnansweredCount(rs.getInt("unanswered_count"));
        return s;
    }
}
