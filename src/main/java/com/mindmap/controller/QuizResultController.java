package com.mindmap.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mindmap.model.QuizQuestion;
import com.mindmap.model.QuizSession;
import com.mindmap.util.UiUtils;
import com.mindmap.util.ViewManager;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.stage.Modality;
import com.mindmap.model.Note;

import java.time.Duration;
import java.util.List;

public class QuizResultController {

    @FXML private Label lblScore;
    @FXML private Label lblPercentage;
    @FXML private Label lblCorrect;
    @FXML private Label lblIncorrect;
    @FXML private Label lblUnanswered;
    @FXML private Label lblTimeUsed;
    @FXML private Label lblMessage;
    @FXML private ListView<QuizQuestion> listReview;
    @FXML private Button btnBack;

    private final ObjectMapper mapper = new ObjectMapper();

    public void initResult(QuizSession session, List<QuizQuestion> questions) {
        lblScore.setText(session.getScore() + " / " + session.getQuestionCount());
        
        int pct = (int) Math.round(((double) session.getScore() / session.getQuestionCount()) * 100);
        lblPercentage.setText(pct + "%");
        
        lblCorrect.setText(String.valueOf(session.getCorrectCount()));
        lblIncorrect.setText(String.valueOf(session.getIncorrectCount()));
        lblUnanswered.setText(String.valueOf(session.getUnansweredCount()));
        
        if (session.getCompletedAt() != null && session.getStartedAt() != null) {
            long secs = Duration.between(session.getStartedAt(), session.getCompletedAt()).getSeconds();
            lblTimeUsed.setText(String.format("%02d:%02d", secs / 60, secs % 60));
        } else {
            lblTimeUsed.setText("--:--");
        }
        
        if (pct >= 90) lblMessage.setText("Excellent work! Outstanding performance.");
        else if (pct >= 75) lblMessage.setText("Great job! Solid understanding.");
        else if (pct >= 50) lblMessage.setText("Good effort. Keep reviewing to improve.");
        else lblMessage.setText("Don't give up! More practice will help.");
        
        listReview.getItems().setAll(questions);
        listReview.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(QuizQuestion item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    VBox card = new VBox(8);
                    card.setStyle("-fx-background-color: white; -fx-padding: 16; -fx-background-radius: 8; -fx-border-color: #e2e8f0; -fx-border-radius: 8;");
                    
                    HBox header = new HBox(8);
                    Label qLabel = new Label("Q" + item.getQuestionOrder() + " " + (item.isCorrect() ? "✓ Correct" : "✗ Incorrect"));
                    qLabel.setStyle(item.isCorrect() ? "-fx-text-fill: #16a34a; -fx-font-weight: bold;" : "-fx-text-fill: #dc2626; -fx-font-weight: bold;");
                    header.getChildren().add(qLabel);
                    
                    Label text = new Label(item.getQuestionText());
                    text.setWrapText(true);
                    text.setStyle("-fx-font-size: 14px;");
                    
                    try {
                        List<String> uAns = mapper.readValue(item.getUserAnswerJson(), new TypeReference<>() {});
                        List<String> cAns = mapper.readValue(item.getCorrectAnswerJson(), new TypeReference<>() {});
                        
                        Label userL = new Label("Your answer: " + (uAns.isEmpty() ? "None" : String.join(", ", uAns)));
                        userL.setStyle("-fx-text-fill: #64748b;");
                        
                        Label correctL = new Label("Correct answer: " + String.join(", ", cAns));
                        correctL.setStyle("-fx-text-fill: #0f172a; -fx-font-weight: bold;");
                        
                        card.getChildren().addAll(header, text, userL, correctL);
                    } catch (Exception e) {}
                    
                    if (item.getSourceNote() != null) {
                        Button sourceBtn = new Button("View Source Note: " + item.getSourceNote().getTitle());
                        sourceBtn.setStyle("-fx-font-size: 11px; -fx-background-color: #f1f5f9; -fx-text-fill: #475569;");
                        sourceBtn.setOnAction(e -> {
                            try {
                                Note note = item.getSourceNote();
                                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/note_view.fxml"));
                                Parent root = loader.load();
                                NoteViewController controller = loader.getController();
                                Stage stage = new Stage();
                                stage.setTitle("View Note - " + note.getTitle());
                                stage.initModality(Modality.APPLICATION_MODAL);
                                if (sourceBtn.getScene() != null && sourceBtn.getScene().getWindow() != null) {
                                    stage.initOwner(sourceBtn.getScene().getWindow());
                                }
                                stage.setScene(new Scene(root));
                                controller.setNote(note);
                                stage.show();
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        });
                        card.getChildren().add(sourceBtn);
                    }
                    
                    setGraphic(card);
                }
            }
        });
    }

    @FXML
    private void handleBack() {
        try {
            Parent view = ViewManager.loadView("/fxml/quiz.fxml");
            Parent current = btnBack.getParent();
            while (current != null && !"contentArea".equals(current.getId())) {
                current = current.getParent();
            }
            if (current != null && current instanceof StackPane contentArea) {
                contentArea.getChildren().setAll(view);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
