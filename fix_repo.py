with open('src/main/java/com/mindmap/repository/QuizAttemptRepository.java', 'r') as f:
    text = f.read()

insert = """
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
"""

text = text.replace("public void addAttemptQuestion", insert + "\n    public void addAttemptQuestion")

with open('src/main/java/com/mindmap/repository/QuizAttemptRepository.java', 'w') as f:
    f.write(text)
