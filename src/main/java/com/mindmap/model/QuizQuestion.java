package com.mindmap.model;

public class QuizQuestion {
    private int id;
    private int quizSessionId;
    private int sourceNoteId;
    private String questionText;
    private String questionType; // "SINGLE_CHOICE", "MULTIPLE_CHOICE", "TRUE_FALSE"
    private String optionsJson;
    private String correctAnswerJson;
    private String userAnswerJson;
    private int questionOrder;
    private boolean isCorrect;

    private Note sourceNote; // Transient reference for UI review

    public QuizQuestion() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getQuizSessionId() { return quizSessionId; }
    public void setQuizSessionId(int quizSessionId) { this.quizSessionId = quizSessionId; }
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
    public String getUserAnswerJson() { return userAnswerJson; }
    public void setUserAnswerJson(String userAnswerJson) { this.userAnswerJson = userAnswerJson; }
    public int getQuestionOrder() { return questionOrder; }
    public void setQuestionOrder(int questionOrder) { this.questionOrder = questionOrder; }
    public boolean isCorrect() { return isCorrect; }
    public void setCorrect(boolean isCorrect) { this.isCorrect = isCorrect; }

    public Note getSourceNote() { return sourceNote; }
    public void setSourceNote(Note sourceNote) { this.sourceNote = sourceNote; }
}
