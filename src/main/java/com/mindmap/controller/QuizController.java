package com.mindmap.controller;

import com.mindmap.model.BankQuestion;
import com.mindmap.model.Note;
import com.mindmap.model.QuizAttempt;
import com.mindmap.repository.NoteRepository;
import com.mindmap.repository.QuestionBankRepository;
import com.mindmap.repository.QuizAttemptRepository;
import com.mindmap.service.QuizGenerationService;
import com.mindmap.util.DateUtil;
import com.mindmap.util.UiUtils;
import com.mindmap.util.ViewManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Region;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class QuizController {
    private static final Logger LOGGER = Logger.getLogger(QuizController.class.getName());

    // Navigation Segments
    @FXML private Button btnTabBank;
    @FXML private Button btnTabExam;
    @FXML private VBox boxQuestionBank;
    @FXML private VBox boxExamSetup;
    
    // Stats
    @FXML private Label lblStatQuestions;
    @FXML private Label lblStatTopics;
    @FXML private Label lblStatSubjects;
    @FXML private Label lblStatExams;

    // Question Bank Filters
    @FXML private TextField txtSearchBank;
    @FXML private ComboBox<String> comboFilterSubject;
    @FXML private ComboBox<String> comboFilterTopic;
    @FXML private ComboBox<String> comboFilterDifficulty;
    @FXML private ComboBox<String> comboFilterType;
    @FXML private Button btnGenerateBank;
    @FXML private ListView<BankQuestion> listQuestionBank;

    // Exam Setup
    @FXML private ComboBox<String> comboExamSubject;
    @FXML private ComboBox<String> comboExamTopic;
    @FXML private ComboBox<String> comboExamDifficulty;
    @FXML private ComboBox<Integer> comboExamCount;
    @FXML private ComboBox<String> comboExamMode;
    @FXML private ComboBox<Integer> comboExamTime;
    @FXML private Label lblAvailableBankCount;
    @FXML private Button btnStartExam;
    
    // Recent Attempts
    @FXML private VBox boxRecentAttempts;
    @FXML private ListView<QuizAttempt> listRecentAttempts;

    private final QuestionBankRepository bankRepo = new QuestionBankRepository();
    private final NoteRepository noteRepo = new NoteRepository();
    private final QuizGenerationService genService = new QuizGenerationService();
    private final QuizAttemptRepository attemptRepo = new QuizAttemptRepository();

    private ObservableList<BankQuestion> currentBank = FXCollections.observableArrayList();
    private ObservableList<QuizAttempt> recentAttempts = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Initial setup for tabs
        handleTabBank();

        // Bank Filters
        initComboBox(comboFilterSubject, "All Subjects");
        initComboBox(comboFilterTopic, "All Topics");
        initComboBox(comboFilterDifficulty, "All Difficulties", "EASY", "MEDIUM", "HARD", "Mixed");
        initComboBox(comboFilterType, "All Types", "SINGLE_CHOICE", "MULTIPLE_CHOICE", "TRUE_FALSE");
        
        txtSearchBank.textProperty().addListener((o, oldV, newV) -> loadQuestionBank());
        comboFilterSubject.valueProperty().addListener((o, oldV, newV) -> loadQuestionBank());
        comboFilterTopic.valueProperty().addListener((o, oldV, newV) -> loadQuestionBank());
        comboFilterDifficulty.valueProperty().addListener((o, oldV, newV) -> loadQuestionBank());
        comboFilterType.valueProperty().addListener((o, oldV, newV) -> loadQuestionBank());

        listQuestionBank.setItems(currentBank);
        listQuestionBank.setCellFactory(lv -> new BankQuestionCell());

        // Exam Filters
        initComboBox(comboExamSubject, "All Subjects");
        initComboBox(comboExamTopic, "All Topics");
        initComboBox(comboExamDifficulty, "All Difficulties", "EASY", "MEDIUM", "HARD");
        
        comboExamCount.getItems().addAll(5, 10, 15, 20, 50);
        comboExamCount.getSelectionModel().selectFirst();
        
        initComboBox(comboExamMode, "Practice", "Timed Exam");
        comboExamTime.getItems().addAll(5, 10, 15, 30, 60);
        comboExamTime.getSelectionModel().selectFirst();
        
        comboExamMode.valueProperty().addListener((o, old, newVal) -> {
            comboExamTime.setDisable(!"Timed Exam".equals(newVal));
        });
        comboExamTime.setDisable(true);

        comboExamSubject.valueProperty().addListener((o, old, newVal) -> updateExamAvailableCount());
        comboExamTopic.valueProperty().addListener((o, old, newVal) -> updateExamAvailableCount());
        comboExamDifficulty.valueProperty().addListener((o, old, newVal) -> updateExamAvailableCount());

        // Load subjects
        List<Note> allNotes = noteRepo.findAll();
        List<String> subjects = allNotes.stream().map(Note::getSubject).filter(s -> s != null && !s.isEmpty()).distinct().collect(Collectors.toList());
        comboFilterSubject.getItems().addAll(subjects);
        comboExamSubject.getItems().addAll(subjects);
        
        // Attempts
        listRecentAttempts.setItems(recentAttempts);
        listRecentAttempts.setCellFactory(lv -> new AttemptCell());

        loadQuestionBank();
        loadAttempts();
        updateStats();
        updateExamAvailableCount();
    }
    
    @FXML
    private void handleTabBank() {
        btnTabBank.getStyleClass().add("segment-active");
        btnTabExam.getStyleClass().remove("segment-active");
        boxQuestionBank.setVisible(true);
        boxQuestionBank.setManaged(true);
        boxExamSetup.setVisible(false);
        boxExamSetup.setManaged(false);
    }
    
    @FXML
    private void handleTabExam() {
        btnTabExam.getStyleClass().add("segment-active");
        btnTabBank.getStyleClass().remove("segment-active");
        boxExamSetup.setVisible(true);
        boxExamSetup.setManaged(true);
        boxQuestionBank.setVisible(false);
        boxQuestionBank.setManaged(false);
        updateExamAvailableCount();
    }

    private void initComboBox(ComboBox<String> cb, String... options) {
        if (cb != null) {
            cb.getItems().addAll(options);
            cb.getSelectionModel().selectFirst();
        }
    }

    private void loadQuestionBank() {
        if (comboFilterSubject == null) return;
        List<BankQuestion> questions = bankRepo.findByFilter(
                txtSearchBank.getText(),
                comboFilterSubject.getValue(),
                comboFilterTopic.getValue(),
                comboFilterDifficulty.getValue(),
                comboFilterType.getValue()
        );
        currentBank.setAll(questions);
    }
    
    private void loadAttempts() {
        List<QuizAttempt> list = attemptRepo.findAll();
        recentAttempts.setAll(list);
    }
    
    private void updateStats() {
        List<BankQuestion> all = bankRepo.findAll();
        lblStatQuestions.setText(String.valueOf(all.size()));
        long topics = all.stream().map(BankQuestion::getTopic).filter(t -> t != null).distinct().count();
        lblStatTopics.setText(String.valueOf(topics));
        long subjects = all.stream().map(BankQuestion::getSubject).filter(s -> s != null).distinct().count();
        lblStatSubjects.setText(String.valueOf(subjects));
        lblStatExams.setText(String.valueOf(recentAttempts.size()));
    }

    private void updateExamAvailableCount() {
        if (comboExamSubject == null) return;
        List<BankQuestion> questions = bankRepo.findByFilter(
                null,
                comboExamSubject.getValue(),
                comboExamTopic.getValue(),
                comboExamDifficulty.getValue(),
                "All Types"
        );
        lblAvailableBankCount.setText(questions.size() + " questions available");
        if (questions.isEmpty()) {
            lblAvailableBankCount.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
            btnStartExam.setDisable(true);
            btnStartExam.setText("Not Enough Questions");
        } else {
            lblAvailableBankCount.setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold;");
            btnStartExam.setDisable(false);
            btnStartExam.setText("Start Exam");
        }
    }

    @FXML
    private void handleGenerateBank() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Generate Question Bank");
        
        VBox content = new VBox(12);
        content.setStyle("-fx-padding: 20; -fx-min-width: 300px; -fx-background-color: white;");
        
        Label lblHeader = new Label("Generate Questions from Notes");
        lblHeader.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");
        
        ComboBox<String> sub = new ComboBox<>();
        sub.getItems().addAll(comboFilterSubject.getItems());
        sub.getSelectionModel().selectFirst();
        sub.setMaxWidth(Double.MAX_VALUE);
        
        ComboBox<Integer> count = new ComboBox<>();
        count.getItems().addAll(10, 20, 50, 100);
        count.getSelectionModel().select(1);
        count.setMaxWidth(Double.MAX_VALUE);
        
        Label l1 = new Label("Source Subject");
        l1.setStyle("-fx-text-fill: #475569; -fx-font-size: 12px;");
        Label l2 = new Label("Number of Questions");
        l2.setStyle("-fx-text-fill: #475569; -fx-font-size: 12px;");
        
        content.getChildren().addAll(lblHeader, l1, sub, l2, count);
        dialog.getDialogPane().setContent(content);
        
        ButtonType btnTypeGen = new ButtonType("Generate", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnTypeGen, ButtonType.CANCEL);
        
        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == btnTypeGen) {
            generateQuestions(sub.getValue(), count.getValue());
        }
    }

    private void generateQuestions(String subject, int count) {
        btnGenerateBank.setDisable(true);
        btnGenerateBank.setText("Generating...");
        
        new Thread(() -> {
            try {
                List<Note> allNotes = noteRepo.findAll();
                List<Note> filtered = subject.equals("All Subjects") ? allNotes : 
                    allNotes.stream().filter(n -> subject.equals(n.getSubject())).collect(Collectors.toList());
                
                List<BankQuestion> generated = genService.generateQuestions(filtered, allNotes, count, "Mixed");
                
                int added = 0;
                for (BankQuestion q : generated) {
                    BankQuestion saved = bankRepo.create(q);
                    if (saved != null) added++;
                }
                
                final int finalAdded = added;
                Platform.runLater(() -> {
                    btnGenerateBank.setDisable(false);
                    btnGenerateBank.setText("+ Generate Questions");
                    loadQuestionBank();
                    updateStats();
                    updateExamAvailableCount();
                    UiUtils.showInfo("Generation Complete", finalAdded + " new questions were added to your Question Bank.");
                });
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error generating questions", e);
                Platform.runLater(() -> {
                    btnGenerateBank.setDisable(false);
                    btnGenerateBank.setText("+ Generate Questions");
                    UiUtils.showError("Error", "Failed to generate questions: " + e.getMessage());
                });
            }
        }).start();
    }

    @FXML
    private void handleStartExam() {
        List<BankQuestion> available = bankRepo.findByFilter(
                null,
                comboExamSubject.getValue(),
                comboExamTopic.getValue(),
                comboExamDifficulty.getValue(),
                "All Types"
        );
        
        int requested = comboExamCount.getValue();
        if (available.isEmpty()) {
            UiUtils.showError("No Questions", "There are no questions available for the selected filters.");
            return;
        }
        
        if (available.size() < requested) {
            boolean confirm = UiUtils.showConfirmation("Not enough questions", "Only " + available.size() + " questions are available. Start with " + available.size() + "?");
            if (!confirm) return;
            requested = available.size();
        }
        
        Collections.shuffle(available);
        List<BankQuestion> selectedQuestions = available.subList(0, requested);
        
        QuizAttempt attempt = new QuizAttempt();
        attempt.setMode(comboExamMode.getValue());
        attempt.setQuestionCount(requested);
        attempt.setTimeLimitSeconds("Timed Exam".equals(attempt.getMode()) ? comboExamTime.getValue() * 60 : 0);
        
        QuizAttempt savedAttempt = attemptRepo.create(attempt);
        launchSession(savedAttempt, selectedQuestions, false);
    }
    
    private void launchSession(QuizAttempt attempt, List<BankQuestion> questions, boolean studyMode) {
        try {
            ViewManager.ViewResult result = ViewManager.loadViewWithController("/fxml/quiz_session.fxml");
            QuizSessionController controller = (QuizSessionController) result.getController();
            controller.initSession(attempt, questions, studyMode);
            
            Parent current = btnTabBank != null ? btnTabBank.getParent() : listQuestionBank.getParent();
            while (current != null && !"contentArea".equals(current.getId())) {
                current = current.getParent();
            }
            if (current != null && current instanceof StackPane contentArea) {
                contentArea.getChildren().setAll(result.getRoot());
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to launch quiz session", e);
        }
    }

    private class BankQuestionCell extends ListCell<BankQuestion> {
        @Override
        protected void updateItem(BankQuestion item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setGraphic(null);
                setText(null);
                setStyle("-fx-background-color: transparent;");
            } else {
                VBox card = new VBox(8);
                card.getStyleClass().add("card");
                card.setStyle("-fx-padding: 16; -fx-background-color: #ffffff; -fx-border-color: #e2e8f0; -fx-border-radius: 12; -fx-background-radius: 12; -fx-cursor: hand;");
                
                HBox top = new HBox(12);
                top.setAlignment(Pos.CENTER_LEFT);
                Label type = new Label(item.getQuestionType().replace("_", " "));
                type.setStyle("-fx-font-weight: bold; -fx-text-fill: #4f46e5; -fx-background-color: #e0e7ff; -fx-padding: 4 8; -fx-background-radius: 6; -fx-font-size: 11px;");
                Label diff = new Label(item.getDifficulty());
                diff.setStyle("-fx-text-fill: #64748b; -fx-font-size: 11px; -fx-font-weight: bold;");
                top.getChildren().addAll(type, diff);
                
                Label text = new Label(item.getQuestionText());
                text.setWrapText(true);
                text.setStyle("-fx-font-size: 15px; -fx-text-fill: #0f172a; -fx-font-weight: 500;");
                
                VBox meta = new VBox(2);
                Label lTopic = new Label("Topic: " + item.getTopic());
                lTopic.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");
                Label lSource = new Label("Source: " + (item.getSourceNote() != null ? item.getSourceNote().getTitle() : "Unknown"));
                lSource.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");
                meta.getChildren().addAll(lTopic, lSource);
                
                HBox actions = new HBox(10);
                actions.setAlignment(Pos.CENTER_LEFT);
                actions.setPadding(new javafx.geometry.Insets(6, 0, 0, 0));
                
                Button btnStudy = new Button("Study");
                btnStudy.setStyle("-fx-background-color: #4f46e5; -fx-text-fill: white; -fx-padding: 6 16; -fx-background-radius: 6; -fx-font-size: 12px; -fx-font-weight: bold;");
                btnStudy.setOnAction(e -> {
                    launchSession(null, List.of(item), true);
                });
                
                Button btnView = new Button("View Note");
                btnView.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #334155; -fx-border-color: #cbd5e1; -fx-border-radius: 6; -fx-background-radius: 6; -fx-padding: 5 15; -fx-font-size: 12px;");
                btnView.setOnAction(e -> handleViewNote(item.getSourceNote()));
                
                actions.getChildren().addAll(btnStudy, btnView);
                
                card.getChildren().addAll(top, text, meta, actions);
                
                card.setOnMouseEntered(e -> card.setStyle("-fx-padding: 16; -fx-background-color: #f8fafc; -fx-border-color: #cbd5e1; -fx-border-radius: 12; -fx-background-radius: 12; -fx-cursor: hand;"));
                card.setOnMouseExited(e -> card.setStyle("-fx-padding: 16; -fx-background-color: #ffffff; -fx-border-color: #e2e8f0; -fx-border-radius: 12; -fx-background-radius: 12; -fx-cursor: hand;"));
                
                setGraphic(card);
                setStyle("-fx-background-color: transparent; -fx-padding: 4 0;");
            }
        }
    }
    
    private class AttemptCell extends ListCell<QuizAttempt> {
        private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
        @Override
        protected void updateItem(QuizAttempt item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setGraphic(null);
                setText(null);
                setStyle("-fx-background-color: transparent;");
            } else {
                VBox card = new VBox(4);
                card.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e2e8f0; -fx-border-radius: 8; -fx-padding: 10;");
                
                HBox header = new HBox(10);
                header.setAlignment(Pos.CENTER_LEFT);
                Label mode = new Label(item.getMode());
                mode.setStyle("-fx-font-weight: bold; -fx-text-fill: #0f172a;");
                Region r = new Region();
                HBox.setHgrow(r, javafx.scene.layout.Priority.ALWAYS);
                Label score = new Label(item.getScore() + "/" + item.getQuestionCount());
                score.setStyle("-fx-font-weight: bold; -fx-text-fill: #4f46e5;");
                header.getChildren().addAll(mode, r, score);
                
                int pct = item.getQuestionCount() > 0 ? (int)Math.round(((double)item.getScore() / item.getQuestionCount()) * 100) : 0;
                Label date = new Label(item.getCompletedAt() != null ? item.getCompletedAt().format(fmt) : "");
                date.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b;");
                Label pctLbl = new Label(pct + "%");
                pctLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b;");
                
                HBox footer = new HBox(10, date, pctLbl);
                
                card.getChildren().addAll(header, footer);
                setGraphic(card);
                setStyle("-fx-background-color: transparent; -fx-padding: 2 0;");
            }
        }
    }
    
    private void handleViewNote(Note note) {
        if (note == null) return;
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/note_view.fxml"));
            Parent root = loader.load();
            NoteViewController controller = loader.getController();
            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("View Note - " + note.getTitle());
            stage.initModality(Modality.APPLICATION_MODAL);
            if (btnTabBank.getScene() != null && btnTabBank.getScene().getWindow() != null) {
                stage.initOwner(btnTabBank.getScene().getWindow());
            }
            stage.setScene(new Scene(root));
            controller.setNote(note);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
