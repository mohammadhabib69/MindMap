package com.mindmap.model;

public class QuizConfiguration {
    private String subject;
    private String topic;
    private String difficulty;
    private int questionCount;
    private String type; // "Mixed", "Single Choice", "Multiple Choice", "True / False"

    public QuizConfiguration(String subject, String topic, String difficulty, int questionCount, String type) {
        this.subject = subject;
        this.topic = topic;
        this.difficulty = difficulty;
        this.questionCount = questionCount;
        this.type = type;
    }

    public String getSubject() { return subject; }
    public String getTopic() { return topic; }
    public String getDifficulty() { return difficulty; }
    public int getQuestionCount() { return questionCount; }
    public String getType() { return type; }
}
