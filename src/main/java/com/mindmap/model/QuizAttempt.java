package com.mindmap.model;
import java.time.LocalDateTime;

public class QuizAttempt {
    private int id;
    private String mode;
    private int questionCount;
    private int timeLimitSeconds;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private int score;
    private int correctCount;
    private int incorrectCount;
    private int unansweredCount;

    public QuizAttempt() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }
    public int getQuestionCount() { return questionCount; }
    public void setQuestionCount(int questionCount) { this.questionCount = questionCount; }
    public int getTimeLimitSeconds() { return timeLimitSeconds; }
    public void setTimeLimitSeconds(int timeLimitSeconds) { this.timeLimitSeconds = timeLimitSeconds; }
    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
    public int getCorrectCount() { return correctCount; }
    public void setCorrectCount(int correctCount) { this.correctCount = correctCount; }
    public int getIncorrectCount() { return incorrectCount; }
    public void setIncorrectCount(int incorrectCount) { this.incorrectCount = incorrectCount; }
    public int getUnansweredCount() { return unansweredCount; }
    public void setUnansweredCount(int unansweredCount) { this.unansweredCount = unansweredCount; }
}
