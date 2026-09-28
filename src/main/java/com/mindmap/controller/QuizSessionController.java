package com.mindmap.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mindmap.model.BankQuestion;
import com.mindmap.model.QuizAttempt;
import com.mindmap.model.QuizAttemptQuestion;
import com.mindmap.repository.QuizAttemptRepository;
import com.mindmap.util.UiUtils;
import com.mindmap.util.ViewManager;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.util.Duration;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class QuizSessionController {
    private static final Logger LOGGER = Logger.getLogger(QuizSessionController.class.getName());

    @FXML private Label lblSessionTitle;
    @FXML private Label lblProgress;
    @FXML private Label lblTimer;
    @FXML private ProgressBar progressSession;
    @FXML private Label lblQuestionText;
    @FXML private VBox boxOptions;
    @FXML private VBox boxExplanation;
    @FXML private Label lblCorrectAnswer;
    @FXML private Label lblExplanation;
    @FXML private Button btnViewSourceNote;
    
    @FXML private Button btnShowAnswer;
    @FXML private Button btnPrev;
    @FXML private Button btnNext;
    @FXML private Button btnSubmit;
    @FXML private FlowPane boxNavigator;

    private QuizAttempt attempt;
    private List<BankQuestion> questions;
    private boolean studyMode;
    private int currentIndex = 0;
    
    private final QuizAttemptRepository attemptRepo = new QuizAttemptRepository();
    private final ObjectMapper mapper = new ObjectMapper();
    
    private Timeline timeline;
    private int secondsRemaining;

    private final List<List<String>> userAnswers = new ArrayList<>();

    public void initSession(QuizAttempt attempt, List<BankQuestion> questions, boolean studyMode) {
        this.attempt = attempt;
        this.questions = questions;
        this.studyMode = studyMode;
        
        for (int i = 0; i < questions.size(); i++) {
            userAnswers.add(new ArrayList<>());
        }
        
        lblSessionTitle.setText(studyMode ? "Study Mode" : attempt.getMode());
        
        if (!studyMode && "Timed Exam".equalsIgnoreCase(attempt.getMode())) {
            secondsRemaining = attempt.getTimeLimitSeconds();
            updateTimerLabel();
            timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
                secondsRemaining--;
                updateTimerLabel();
                if (secondsRemaining <= 0) {
                    timeline.stop();
                    UiUtils.showInfo("Time's up!", "Your exam will now be submitted automatically.");
                    autoSubmit();
                }
            }));
            timeline.setCycleCount(Timeline.INDEFINITE);
            timeline.play();
            lblTimer.setVisible(true);
        } else {
            lblTimer.setVisible(false);
        }
        
        btnShowAnswer.setVisible(studyMode);
        btnShowAnswer.setManaged(studyMode);
        
        btnSubmit.setVisible(!studyMode);
        btnSubmit.setManaged(!studyMode);
        
        boxExplanation.setVisible(false);
        boxExplanation.setManaged(false);
        
        buildNavigator();
        loadQuestion(0);
    }
    
    private void updateTimerLabel() {
        int m = secondsRemaining / 60;
        int s = secondsRemaining % 60;
        lblTimer.setText(String.format("%02d:%02d", m, s));
        if (secondsRemaining < 60) {
            lblTimer.setStyle("-fx-text-fill: #dc2626; -fx-font-weight: bold;");
        }
    }

    private void buildNavigator() {
        boxNavigator.getChildren().clear();
        for (int i = 0; i < questions.size(); i++) {
            Button navBtn = new Button(String.valueOf(i + 1));
            navBtn.setPrefWidth(35);
            navBtn.setPrefHeight(35);
            int idx = i;
            navBtn.setOnAction(e -> loadQuestion(idx));
            boxNavigator.getChildren().add(navBtn);
        }
    }

    private void updateNavigatorVisuals() {
        for (int i = 0; i < questions.size(); i++) {
            Button btn = (Button) boxNavigator.getChildren().get(i);
            btn.getStyleClass().removeAll("nav-btn-current", "nav-btn-answered");
            
            if (i == currentIndex) {
                btn.setStyle("-fx-background-color: #4f46e5; -fx-text-fill: white; -fx-border-color: #4f46e5;");
            } else if (!userAnswers.get(i).isEmpty()) {
                btn.setStyle("-fx-background-color: #e0e7ff; -fx-text-fill: #3730a3; -fx-border-color: #a5b4fc;");
            } else {
                btn.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #475569; -fx-border-color: #cbd5e1;");
            }
        }
    }

    private void loadQuestion(int index) {
        if (index < 0 || index >= questions.size()) return;
        currentIndex = index;
        
        BankQuestion q = questions.get(index);
        lblQuestionText.setText(q.getQuestionText());
        lblProgress.setText("Question " + (index + 1) + " of " + questions.size());
        progressSession.setProgress((double)(index + 1) / questions.size());
        
        btnPrev.setDisable(index == 0);
        btnNext.setDisable(index == questions.size() - 1);
        
        boxOptions.getChildren().clear();
        boxExplanation.setVisible(false);
        boxExplanation.setManaged(false);
        
        try {
            List<String> options = mapper.readValue(q.getOptionsJson(), new TypeReference<List<String>>() {});
            List<String> selected = userAnswers.get(index);
            
            boolean isMulti = "MULTIPLE_CHOICE".equals(q.getQuestionType());
            ToggleGroup group = new ToggleGroup();
            
            for (String opt : options) {
                if (isMulti) {
                    CheckBox cb = new CheckBox(opt);
                    cb.setWrapText(true);
                    cb.setSelected(selected.contains(opt));
                    cb.setOnAction(e -> {
                        if (cb.isSelected()) {
                            if (!selected.contains(opt)) selected.add(opt);
                        } else {
                            selected.remove(opt);
                        }
                        updateNavigatorVisuals();
                    });
                    boxOptions.getChildren().add(cb);
                } else {
                    RadioButton rb = new RadioButton(opt);
                    rb.setWrapText(true);
                    rb.setToggleGroup(group);
                    rb.setSelected(selected.contains(opt));
                    rb.setOnAction(e -> {
                        selected.clear();
                        selected.add(opt);
                        updateNavigatorVisuals();
                    });
                    boxOptions.getChildren().add(rb);
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to parse options", e);
            lblQuestionText.setText("Error loading options.");
        }
        
        updateNavigatorVisuals();
    }
    
    @FXML
    private void handleShowAnswer() {
        BankQuestion q = questions.get(currentIndex);
        try {
            List<String> correct = mapper.readValue(q.getCorrectAnswerJson(), new TypeReference<List<String>>() {});
            lblCorrectAnswer.setText("Correct Answer: " + String.join(", ", correct));
            lblExplanation.setText("Explanation: " + q.getExplanation());
            
            btnViewSourceNote.setOnAction(e -> {
                if (q.getSourceNote() != null) {
                    try {
                        javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/note_view.fxml"));
                        Parent root = loader.load();
                        NoteViewController controller = loader.getController();
                        javafx.stage.Stage stage = new javafx.stage.Stage();
                        stage.setTitle("View Note - " + q.getSourceNote().getTitle());
                        stage.initModality(Modality.APPLICATION_MODAL);
                        if (btnViewSourceNote.getScene() != null && btnViewSourceNote.getScene().getWindow() != null) {
                            stage.initOwner(btnViewSourceNote.getScene().getWindow());
                        }
                        stage.setScene(new Scene(root));
                        controller.setNote(q.getSourceNote());
                        stage.show();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });
            
            boxExplanation.setVisible(true);
            boxExplanation.setManaged(true);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error showing answer", e);
        }
    }

    @FXML
    private void handlePrev() {
        loadQuestion(currentIndex - 1);
    }

    @FXML
    private void handleNext() {
        loadQuestion(currentIndex + 1);
    }
    
    @FXML
    private void handleExitStudy() {
        // Just go back to quiz screen
        if (!studyMode) return;
        try {
            Parent view = ViewManager.loadView("/fxml/quiz.fxml");
            Parent current = btnPrev.getParent();
            while (current != null && !"contentArea".equals(current.getId())) {
                current = current.getParent();
            }
            if (current != null && current instanceof StackPane contentArea) {
                contentArea.getChildren().setAll(view);
            }
        } catch (Exception e) {}
    }

    @FXML
    private void handleSubmit() {
        if (studyMode) {
            handleExitStudy();
            return;
        }
        long answeredCount = userAnswers.stream().filter(l -> !l.isEmpty()).count();
        if (answeredCount < questions.size()) {
            boolean confirm = UiUtils.showConfirmation("Submit Exam", "You have answered " + answeredCount + " of " + questions.size() + " questions. Submit anyway?");
            if (!confirm) return;
        } else {
            boolean confirm = UiUtils.showConfirmation("Submit Exam", "Ready to submit your exam?");
            if (!confirm) return;
        }
        finishQuiz();
    }
    
    private void autoSubmit() {
        finishQuiz();
    }
    
    private void finishQuiz() {
        if (timeline != null) timeline.stop();
        
        int correct = 0;
        int incorrect = 0;
        int unanswered = 0;
        
        List<QuizAttemptQuestion> attemptQuestions = new ArrayList<>();
        
        for (int i = 0; i < questions.size(); i++) {
            BankQuestion bq = questions.get(i);
            List<String> userAns = userAnswers.get(i);
            QuizAttemptQuestion qaq = new QuizAttemptQuestion();
            qaq.setAttemptId(attempt.getId());
            qaq.setQuestionId(bq.getId());
            qaq.setQuestionOrder(i + 1);
            qaq.setBankQuestion(bq);
            
            try {
                qaq.setUserAnswerJson(mapper.writeValueAsString(userAns));
                List<String> correctAns = mapper.readValue(bq.getCorrectAnswerJson(), new TypeReference<List<String>>() {});
                
                if (userAns.isEmpty()) {
                    unanswered++;
                    qaq.setCorrect(false);
                } else {
                    boolean isCorrect = correctAns.containsAll(userAns) && userAns.containsAll(correctAns);
                    if (isCorrect) {
                        correct++;
                        qaq.setCorrect(true);
                    } else {
                        incorrect++;
                        qaq.setCorrect(false);
                    }
                }
                
                attemptRepo.addAttemptQuestion(qaq);
                attemptQuestions.add(qaq);
                
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error scoring question", e);
            }
        }
        
        attempt.setCompletedAt(LocalDateTime.now());
        attempt.setScore(correct);
        attempt.setCorrectCount(correct);
        attempt.setIncorrectCount(incorrect);
        attempt.setUnansweredCount(unanswered);
        
        attemptRepo.update(attempt);
        
        try {
            ViewManager.ViewResult result = ViewManager.loadViewWithController("/fxml/quiz_result.fxml");
            QuizResultController controller = (QuizResultController) result.getController();
            controller.initResult(attempt, attemptQuestions);
            
            Parent current = btnSubmit.getParent();
            while (current != null && !"contentArea".equals(current.getId())) {
                current = current.getParent();
            }
            if (current != null && current instanceof StackPane contentArea) {
                contentArea.getChildren().setAll(result.getRoot());
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to load results", e);
        }
    }
}
