package com.mindmap.model;

public class QuizAttemptQuestion {
    private int id;
    private int attemptId;
    private int questionId;
    private int questionOrder;
    private String userAnswerJson;
    private boolean isCorrect;
    
    private BankQuestion bankQuestion; // transient

    public QuizAttemptQuestion() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getAttemptId() { return attemptId; }
    public void setAttemptId(int attemptId) { this.attemptId = attemptId; }
    public int getQuestionId() { return questionId; }
    public void setQuestionId(int questionId) { this.questionId = questionId; }
    public int getQuestionOrder() { return questionOrder; }
    public void setQuestionOrder(int questionOrder) { this.questionOrder = questionOrder; }
    public String getUserAnswerJson() { return userAnswerJson; }
    public void setUserAnswerJson(String userAnswerJson) { this.userAnswerJson = userAnswerJson; }
    public boolean isCorrect() { return isCorrect; }
    public void setCorrect(boolean correct) { isCorrect = correct; }

    public BankQuestion getBankQuestion() { return bankQuestion; }
    public void setBankQuestion(BankQuestion bankQuestion) { this.bankQuestion = bankQuestion; }
}
