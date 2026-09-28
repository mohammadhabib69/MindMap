package com.mindmap.model;

public class BankQuestion {
    private int id;
    private int sourceNoteId;
    private String questionText;
    private String questionType;
    private String optionsJson;
    private String correctAnswerJson;
    private String explanation;
    private String subject;
    private String topic;
    private String difficulty;
    private String createdAt;
    private int timesStudied;
    private int timesAsked;
    
    private Note sourceNote; // transient

    public BankQuestion() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getSourceNoteId() { return sourceNoteId; }
    public void setSourceNoteId(int sourceNoteId) { this.sourceNoteId = sourceNoteId; }
    public String getQuestionText() { return questionText; }
    public void setQuestionText(String questionText) { this.questionText = questionText; }
    public String getQuestionType() { return questionType; }
    public void setQuestionType(String questionType) { this.questionType = questionType; }
    public String getOptionsJson() { return optionsJson; }
    public void setOptionsJson(String optionsJson) { this.optionsJson = optionsJson; }
    public String getCorrectAnswerJson() { return correctAnswerJson; }
    public void setCorrectAnswerJson(String correctAnswerJson) { this.correctAnswerJson = correctAnswerJson; }
    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }
    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public int getTimesStudied() { return timesStudied; }
    public void setTimesStudied(int timesStudied) { this.timesStudied = timesStudied; }
    public int getTimesAsked() { return timesAsked; }
    public void setTimesAsked(int timesAsked) { this.timesAsked = timesAsked; }

    public Note getSourceNote() { return sourceNote; }
    public void setSourceNote(Note sourceNote) { this.sourceNote = sourceNote; }
}
