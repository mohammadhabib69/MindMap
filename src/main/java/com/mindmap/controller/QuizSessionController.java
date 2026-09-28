package com.mindmap.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mindmap.model.QuizQuestion;
import com.mindmap.model.QuizSession;
import com.mindmap.repository.QuizQuestionRepository;
import com.mindmap.repository.QuizSessionRepository;
import com.mindmap.util.UiUtils;
import com.mindmap.util.ViewManager;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class QuizSessionController {
    private static final Logger LOGGER = Logger.getLogger(QuizSessionController.class.getName());

    @FXML private Label lblTitle;
    @FXML private Label lblProgress;
    @FXML private Label lblTimer;
    @FXML private ProgressBar progressQuiz;
    @FXML private Label lblQuestionText;
    @FXML private VBox boxOptions;
    @FXML private FlowPane boxNavigator;
    @FXML private Button btnPrev;
    @FXML private Button btnNext;
    @FXML private Button btnSubmit;

    private QuizSession session;
    private List<QuizQuestion> questions;
    private int currentIndex = 0;
    
    private final QuizSessionRepository sessionRepo = new QuizSessionRepository();
    private final QuizQuestionRepository questionRepo = new QuizQuestionRepository();
    private final ObjectMapper mapper = new ObjectMapper();
    
    private Timeline timeline;
    private int secondsRemaining;

    // Stores currently selected answers in memory before submission
    private final List<List<String>> userAnswers = new ArrayList<>();

    public void initSession(QuizSession session, List<QuizQuestion> questions) {
        this.session = session;
        this.questions = questions;
        
        for (int i = 0; i < questions.size(); i++) {
            userAnswers.add(new ArrayList<>());
        }
        
        lblTitle.setText(session.getTitle() + " - " + session.getMode());
        
        if ("EXAM".equalsIgnoreCase(session.getMode())) {
            secondsRemaining = session.getTimeLimitSeconds();
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
        } else {
            lblTimer.setText("No time limit");
        }
        
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
        
        QuizQuestion q = questions.get(index);
        lblQuestionText.setText(q.getQuestionText());
        lblProgress.setText("Question " + (index + 1) + " of " + questions.size());
        progressQuiz.setProgress((double)(index + 1) / questions.size());
        
        btnPrev.setDisable(index == 0);
        btnNext.setDisable(index == questions.size() - 1);
        
        boxOptions.getChildren().clear();
        
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
    private void handlePrev() {
        loadQuestion(currentIndex - 1);
    }

    @FXML
    private void handleNext() {
        loadQuestion(currentIndex + 1);
    }

    @FXML
    private void handleSubmit() {
        long answeredCount = userAnswers.stream().filter(l -> !l.isEmpty()).count();
        if (answeredCount < questions.size()) {
            boolean confirm = UiUtils.showConfirmation("Submit Quiz", "You have answered " + answeredCount + " of " + questions.size() + " questions. Submit anyway?");
            if (!confirm) return;
        } else {
            boolean confirm = UiUtils.showConfirmation("Submit Quiz", "Ready to submit your quiz?");
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
        
        for (int i = 0; i < questions.size(); i++) {
            QuizQuestion q = questions.get(i);
            List<String> userAns = userAnswers.get(i);
            
            try {
                List<String> correctAns = mapper.readValue(q.getCorrectAnswerJson(), new TypeReference<List<String>>() {});
                q.setUserAnswerJson(mapper.writeValueAsString(userAns));
                
                if (userAns.isEmpty()) {
                    unanswered++;
                    q.setCorrect(false);
                } else {
                    // simple exact match
                    boolean isCorrect = correctAns.containsAll(userAns) && userAns.containsAll(correctAns);
                    if (isCorrect) {
                        correct++;
                        q.setCorrect(true);
                    } else {
                        incorrect++;
                        q.setCorrect(false);
                    }
                }
                
                questionRepo.update(q);
                
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error scoring question", e);
            }
        }
        
        session.setCompletedAt(LocalDateTime.now());
        session.setScore(correct);
        session.setCorrectCount(correct);
        session.setIncorrectCount(incorrect);
        session.setUnansweredCount(unanswered);
        
        sessionRepo.update(session);
        
        // Go to results
        try {
            ViewManager.ViewResult result = ViewManager.loadViewWithController("/fxml/quiz_result.fxml");
            QuizResultController controller = (QuizResultController) result.getController();
            controller.initResult(session, questions);
            
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
