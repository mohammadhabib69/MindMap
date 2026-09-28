package com.mindmap.controller;

import com.mindmap.model.Note;
import com.mindmap.model.QuizConfiguration;
import com.mindmap.model.QuizQuestion;
import com.mindmap.model.QuizSession;
import com.mindmap.repository.NoteRepository;
import com.mindmap.repository.QuizQuestionRepository;
import com.mindmap.repository.QuizSessionRepository;
import com.mindmap.service.QuizGenerationService;
import com.mindmap.util.UiUtils;
import com.mindmap.util.ViewManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class QuizController {
    private static final Logger LOGGER = Logger.getLogger(QuizController.class.getName());

    @FXML private ComboBox<String> comboSubject;
    @FXML private ComboBox<String> comboTopic;
    @FXML private ComboBox<String> comboDifficulty;
    @FXML private ComboBox<Integer> comboQuestionCount;
    @FXML private ComboBox<String> comboMode;
    @FXML private ComboBox<Integer> comboTimeLimit;
    @FXML private Label lblAvailableNotes;
    @FXML private Button btnGenerate;
    @FXML private ListView<QuizSession> listRecentQuizzes;
    @FXML private VBox mainContainer; // Used to replace content if needed, though MainController handles it.

    private final NoteRepository noteRepository = new NoteRepository();
    private final QuizSessionRepository sessionRepo = new QuizSessionRepository();
    private final QuizQuestionRepository questionRepo = new QuizQuestionRepository();
    private final QuizGenerationService generationService = new QuizGenerationService();

    private List<Note> allNotes;

    @FXML
    public void initialize() {
        allNotes = noteRepository.findAll();
        lblAvailableNotes.setText(allNotes.size() + " notes available");

        comboSubject.getItems().addAll("All Subjects");
        allNotes.stream().map(Note::getSubject).filter(s -> s != null && !s.isEmpty()).distinct().forEach(comboSubject.getItems()::add);
        comboSubject.getSelectionModel().selectFirst();

        comboTopic.getItems().addAll("All Topics");
        comboTopic.getSelectionModel().selectFirst();

        comboDifficulty.getItems().addAll("All Difficulties", "EASY", "MEDIUM", "HARD");
        comboDifficulty.getSelectionModel().selectFirst();

        comboQuestionCount.getItems().addAll(5, 10, 15, 20);
        comboQuestionCount.getSelectionModel().selectFirst();

        comboMode.getItems().addAll("Practice", "Exam");
        comboMode.getSelectionModel().selectFirst();
        
        comboTimeLimit.getItems().addAll(5, 10, 15, 20, 30);
        comboTimeLimit.getSelectionModel().selectFirst();

        comboMode.valueProperty().addListener((obs, oldV, newV) -> {
            comboTimeLimit.setDisable(!"Exam".equals(newV));
        });
        comboTimeLimit.setDisable(true);

        loadRecentQuizzes();
    }

    private void loadRecentQuizzes() {
        List<QuizSession> recent = sessionRepo.findAllRecent();
        listRecentQuizzes.getItems().setAll(recent);
        
        listRecentQuizzes.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(QuizSession item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getTitle() + " - " + item.getMode() + " - Score: " + item.getScore() + "/" + item.getQuestionCount());
                }
            }
        });
    }

    @FXML
    private void handleGenerate() {
        String subject = comboSubject.getValue();
        String topic = comboTopic.getValue();
        String diff = comboDifficulty.getValue();
        int count = comboQuestionCount.getValue();
        String mode = comboMode.getValue();
        int timeLimit = comboTimeLimit.getValue();

        QuizConfiguration config = new QuizConfiguration(subject, topic, diff, count, "Mixed");

        btnGenerate.setDisable(true);
        btnGenerate.setText("Generating...");

        new Thread(() -> {
            try {
                List<QuizQuestion> questions = generationService.generateQuiz(allNotes, config);
                Platform.runLater(() -> {
                    btnGenerate.setDisable(false);
                    btnGenerate.setText("Generate & Start Quiz");
                    
                    if (questions.isEmpty()) {
                        UiUtils.showError("No Questions", "No meaningful questions could be generated from the selected notes.");
                        return;
                    }

                    if (questions.size() < count) {
                        boolean cont = UiUtils.showConfirmation("Not enough notes", "Only " + questions.size() + " unique questions could be generated. Start with " + questions.size() + "?");
                        if (!cont) return;
                    }

                    QuizSession session = new QuizSession();
                    session.setTitle(subject.equals("All Subjects") ? "Mixed Quiz" : subject + " Quiz");
                    session.setMode(mode.toUpperCase());
                    session.setQuestionCount(questions.size());
                    session.setTimeLimitSeconds("Exam".equalsIgnoreCase(mode) ? timeLimit * 60 : 0);
                    
                    QuizSession savedSession = sessionRepo.create(session);
                    
                    for (QuizQuestion q : questions) {
                        q.setQuizSessionId(savedSession.getId());
                        questionRepo.create(q);
                    }
                    
                    launchQuizSession(savedSession, questions);
                });
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error generating quiz", e);
                Platform.runLater(() -> {
                    btnGenerate.setDisable(false);
                    btnGenerate.setText("Generate & Start Quiz");
                    UiUtils.showError("Generation Error", "Failed to generate quiz: " + e.getMessage());
                });
            }
        }).start();
    }
    
    private void launchQuizSession(QuizSession session, List<QuizQuestion> questions) {
        try {
            ViewManager.ViewResult result = ViewManager.loadViewWithController("/fxml/quiz_session.fxml");
            QuizSessionController controller = (QuizSessionController) result.getController();
            controller.initSession(session, questions);
            
            // Swap view in the main window
            Scene scene = btnGenerate.getScene();
            if (scene.getRoot() instanceof VBox rootVBox) { // Assuming MainView structure or similar
                // We actually need to tell MainController. 
                // A simpler way: we find the parent of mainContainer which is contentArea in MainController.
            }
            
            // Wait, we can just grab the parent ContentArea.
            // But main.fxml's contentArea is a StackPane.
            // Let's just do:
            Parent current = btnGenerate.getParent();
            while (current != null && !"contentArea".equals(current.getId())) {
                current = current.getParent();
            }
            if (current != null && current instanceof javafx.scene.layout.StackPane contentArea) {
                contentArea.getChildren().setAll(result.getRoot());
            } else {
                LOGGER.severe("Could not find contentArea to swap view");
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to launch quiz session", e);
        }
    }
}
