package com.mindmap.controller;

import com.mindmap.model.BankQuestion;
import com.mindmap.model.Note;
import com.mindmap.model.QuizAttempt;
import com.mindmap.repository.NoteRepository;
import com.mindmap.repository.QuestionBankRepository;
import com.mindmap.repository.QuizAttemptRepository;
import com.mindmap.service.QuizGenerationService;
import com.mindmap.util.UiUtils;
import com.mindmap.util.ViewManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Optional;
import javafx.stage.Modality;

public class QuizController {
    private static final Logger LOGGER = Logger.getLogger(QuizController.class.getName());

    @FXML private ComboBox<String> comboFilterSubject;
    @FXML private ComboBox<String> comboFilterTopic;
    @FXML private ComboBox<String> comboFilterDifficulty;
    @FXML private ComboBox<String> comboFilterType;
    @FXML private Button btnGenerateBank;
    @FXML private ListView<BankQuestion> listQuestionBank;

    @FXML private ComboBox<String> comboExamSubject;
    @FXML private ComboBox<String> comboExamTopic;
    @FXML private ComboBox<String> comboExamDifficulty;
    @FXML private ComboBox<Integer> comboExamCount;
    @FXML private ComboBox<String> comboExamMode;
    @FXML private ComboBox<Integer> comboExamTime;
    @FXML private Label lblAvailableBankCount;
    @FXML private Button btnStartExam;

    private final QuestionBankRepository bankRepo = new QuestionBankRepository();
    private final NoteRepository noteRepo = new NoteRepository();
    private final QuizGenerationService genService = new QuizGenerationService();
    private final QuizAttemptRepository attemptRepo = new QuizAttemptRepository();

    private ObservableList<BankQuestion> currentBank = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Initialize filters for Question Bank
        initComboBox(comboFilterSubject, "All Subjects");
        initComboBox(comboFilterTopic, "All Topics");
        initComboBox(comboFilterDifficulty, "All Difficulties", "EASY", "MEDIUM", "HARD");
        initComboBox(comboFilterType, "All Types", "SINGLE_CHOICE", "MULTIPLE_CHOICE", "TRUE_FALSE");

        // Filter listeners
        comboFilterSubject.valueProperty().addListener((o, oldV, newV) -> loadQuestionBank());
        comboFilterTopic.valueProperty().addListener((o, oldV, newV) -> loadQuestionBank());
        comboFilterDifficulty.valueProperty().addListener((o, oldV, newV) -> loadQuestionBank());
        comboFilterType.valueProperty().addListener((o, oldV, newV) -> loadQuestionBank());

        listQuestionBank.setItems(currentBank);
        listQuestionBank.setCellFactory(lv -> new BankQuestionCell());

        // Initialize filters for Exam Setup
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

        loadQuestionBank();
        updateExamAvailableCount();
        
        // Populate subject lists from notes
        List<Note> allNotes = noteRepo.findAll();
        List<String> subjects = allNotes.stream().map(Note::getSubject).filter(s -> s != null && !s.isEmpty()).distinct().collect(Collectors.toList());
        comboFilterSubject.getItems().addAll(subjects);
        comboExamSubject.getItems().addAll(subjects);
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
                null,
                comboFilterSubject.getValue(),
                comboFilterTopic.getValue(),
                comboFilterDifficulty.getValue(),
                comboFilterType.getValue()
        );
        currentBank.setAll(questions);
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
    }

    @FXML
    private void handleGenerateBank() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Generate Question Bank");
        
        VBox content = new VBox(10);
        ComboBox<String> sub = new ComboBox<>();
        sub.getItems().addAll(comboFilterSubject.getItems());
        sub.getSelectionModel().selectFirst();
        
        ComboBox<Integer> count = new ComboBox<>();
        count.getItems().addAll(10, 20, 50, 100);
        count.getSelectionModel().select(1);
        
        content.getChildren().addAll(new Label("Select Subject:"), sub, new Label("Number of Questions:"), count);
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
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
                    updateExamAvailableCount();
                    UiUtils.showInfo("Generation Complete", finalAdded + " unique questions were added to the Question Bank.");
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
            
            Parent current = btnStartExam != null ? btnStartExam.getParent() : listQuestionBank.getParent();
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
            } else {
                VBox card = new VBox(6);
                card.getStyleClass().add("card");
                card.setStyle("-fx-padding: 12; -fx-background-color: white; -fx-border-color: #e2e8f0; -fx-border-radius: 8; -fx-background-radius: 8;");
                
                HBox top = new HBox(10);
                Label type = new Label("[" + item.getQuestionType() + "]");
                type.setStyle("-fx-font-weight: bold; -fx-text-fill: #4f46e5;");
                Label diff = new Label(item.getDifficulty());
                diff.setStyle("-fx-text-fill: #64748b; -fx-font-size: 11px;");
                top.getChildren().addAll(type, diff);
                
                Label text = new Label(item.getQuestionText());
                text.setWrapText(true);
                text.setStyle("-fx-font-size: 14px; -fx-text-fill: #0f172a;");
                
                Label topic = new Label("Topic: " + item.getTopic() + " | Source: " + (item.getSourceNote() != null ? item.getSourceNote().getTitle() : "Unknown"));
                topic.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b;");
                
                HBox actions = new HBox(10);
                actions.setAlignment(Pos.CENTER_LEFT);
                Button btnStudy = new Button("Study");
                btnStudy.getStyleClass().addAll("btn-secondary");
                btnStudy.setOnAction(e -> {
                    launchSession(null, List.of(item), true);
                });
                
                Button btnView = new Button("View Note");
                btnView.getStyleClass().addAll("btn-secondary");
                btnView.setOnAction(e -> handleViewNote(item.getSourceNote()));
                
                actions.getChildren().addAll(btnStudy, btnView);
                
                card.getChildren().addAll(top, text, topic, actions);
                setGraphic(card);
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
            if (listQuestionBank.getScene() != null && listQuestionBank.getScene().getWindow() != null) {
                stage.initOwner(listQuestionBank.getScene().getWindow());
            }
            stage.setScene(new Scene(root));
            controller.setNote(note);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
