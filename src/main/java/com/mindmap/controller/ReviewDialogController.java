package com.mindmap.controller;

import com.mindmap.concurrency.TaskExecutor;
import com.mindmap.model.Difficulty;
import com.mindmap.model.Note;
import com.mindmap.model.ReviewOutcome;
import com.mindmap.model.ScheduledReview;
import com.mindmap.service.NoteService;
import com.mindmap.service.RevisionService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.mindmap.util.UiUtils;

/**
 * Controller for the Spaced Repetition study review dialog.
 * Guides the user through testing their recall, revealing the answer,
 * and grading retention with deterministic spaced intervals.
 */
public class ReviewDialogController {

    private static final Logger LOGGER = Logger.getLogger(ReviewDialogController.class.getName());

    @FXML private Label lblProgress;
    @FXML private VBox boxReviewContainer;
    @FXML private Label lblSubject;
    @FXML private Label lblDifficulty;
    @FXML private Label lblCurrentInterval;
    @FXML private Label lblTitle;
    @FXML private FlowPane flowTags;
    @FXML private VBox boxRevealPrompt;
    @FXML private Button btnReveal;
    @FXML private VBox boxRevealedContent;
    @FXML private Label lblContent;
    @FXML private Button btnAgain;
    @FXML private Button btnHard;
    @FXML private Button btnGood;
    @FXML private Button btnEasy;
    @FXML private VBox boxCompleted;
    @FXML private Label lblCompletedSummary;

    private RevisionService revisionService = new RevisionService();
    private NoteService noteService = new NoteService();
    private Stage dialogStage;
    private Runnable onFinished;

    private final List<ScheduledReview> reviewQueue = new ArrayList<>();
    private int currentIndex = 0;
    private int completedCount = 0;

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setRevisionService(RevisionService revisionService) {
        if (revisionService != null) {
            this.revisionService = revisionService;
        }
    }

    public void setOnFinished(Runnable onFinished) {
        this.onFinished = onFinished;
    }

    public void setReviews(List<ScheduledReview> reviews) {
        this.reviewQueue.clear();
        if (reviews != null) {
            this.reviewQueue.addAll(reviews);
        }
        this.currentIndex = 0;
        this.completedCount = 0;
        loadCurrentReview();
    }

    public void setSingleReview(ScheduledReview review) {
        setReviews(review != null ? List.of(review) : List.of());
    }

    private void loadCurrentReview() {
        if (currentIndex < 0 || currentIndex >= reviewQueue.size()) {
            showCompletedState();
            return;
        }

        ScheduledReview current = reviewQueue.get(currentIndex);
        Note note = current.getNote();

        // 1. Progress Header
        lblProgress.setText(String.format("Review %d of %d", currentIndex + 1, reviewQueue.size()));

        // 2. Note Title
        lblTitle.setText(note.getTitle() != null ? note.getTitle() : "Untitled Note");

        // 3. Subject Badge
        if (note.getSubject() != null && !note.getSubject().trim().isEmpty()) {
            lblSubject.setText(note.getSubject().trim());
            lblSubject.setVisible(true);
            lblSubject.setManaged(true);
        } else {
            lblSubject.setVisible(false);
            lblSubject.setManaged(false);
        }

        // 4. Difficulty Badge
        String diff = note.getDifficulty() != null ? note.getDifficulty().trim().toUpperCase() : Difficulty.MEDIUM.name();
        lblDifficulty.setText(diff);
        lblDifficulty.getStyleClass().removeAll("badge-easy", "badge-medium", "badge-hard");
        switch (diff) {
            case "EASY" -> lblDifficulty.getStyleClass().add("badge-easy");
            case "HARD" -> lblDifficulty.getStyleClass().add("badge-hard");
            default -> lblDifficulty.getStyleClass().add("badge-medium");
        }

        // 5. Current Interval Badge
        int currentInterval = current.getIntervalDays();
        lblCurrentInterval.setText("Interval: " + currentInterval + (currentInterval == 1 ? " day" : " days"));

        // 6. Tags
        flowTags.getChildren().clear();
        if (note.getTags() != null && !note.getTags().isEmpty()) {
            for (com.mindmap.model.Tag tag : note.getTags()) {
                Label tagLabel = new Label("#" + tag.getName());
                tagLabel.getStyleClass().add("tag-badge");
                flowTags.getChildren().add(tagLabel);
            }
            flowTags.setVisible(true);
            flowTags.setManaged(true);
        } else {
            flowTags.setVisible(false);
            flowTags.setManaged(false);
        }

        // 7. Content (hidden until user reveals)
        lblContent.setText(note.getContent() != null && !note.getContent().trim().isEmpty()
                ? note.getContent()
                : "(This note has no written content)");

        // 8. Outcome buttons next interval preview
        int nextAgain = revisionService.calculateNextInterval(currentInterval, ReviewOutcome.AGAIN);
        int nextHard = revisionService.calculateNextInterval(currentInterval, ReviewOutcome.HARD);
        int nextGood = revisionService.calculateNextInterval(currentInterval, ReviewOutcome.GOOD);
        int nextEasy = revisionService.calculateNextInterval(currentInterval, ReviewOutcome.EASY);

        btnAgain.setText("🔄 Again (" + nextAgain + "d)");
        btnHard.setText("⚡ Hard (" + nextHard + "d)");
        btnGood.setText("👍 Good (" + nextGood + "d)");
        btnEasy.setText("🌟 Easy (" + nextEasy + "d)");

        // Reset display state to unrevealed
        boxReviewContainer.setVisible(true);
        boxReviewContainer.setManaged(true);
        boxCompleted.setVisible(false);
        boxCompleted.setManaged(false);

        boxRevealPrompt.setVisible(true);
        boxRevealPrompt.setManaged(true);
        boxRevealedContent.setVisible(false);
        boxRevealedContent.setManaged(false);
    }

    @FXML
    private void handleReveal() {
        boxRevealPrompt.setVisible(false);
        boxRevealPrompt.setManaged(false);
        boxRevealedContent.setVisible(true);
        boxRevealedContent.setManaged(true);
    }

    @FXML
    private void handleAgain() {
        processOutcome(ReviewOutcome.AGAIN);
    }

    @FXML
    private void handleHard() {
        processOutcome(ReviewOutcome.HARD);
    }

    @FXML
    private void handleGood() {
        processOutcome(ReviewOutcome.GOOD);
    }

    @FXML
    private void handleEasy() {
        processOutcome(ReviewOutcome.EASY);
    }

    private void processOutcome(ReviewOutcome outcome) {
        if (currentIndex < 0 || currentIndex >= reviewQueue.size()) {
            return;
        }

        ScheduledReview current = reviewQueue.get(currentIndex);
        setOutcomeButtonsDisable(true);
        final RevisionService service = this.revisionService;

        TaskExecutor.runAsync(
                () -> {
                    service.completeReview(current.getRevision(), outcome);
                    return true;
                },
                success -> {
                    setOutcomeButtonsDisable(false);
                    completedCount++;
                    currentIndex++;
                    loadCurrentReview();
                },
                throwable -> {
                    setOutcomeButtonsDisable(false);
                    LOGGER.log(Level.SEVERE, "Failed to complete review: " + throwable.getMessage(), throwable);
                    UiUtils.showError("Review Error", "Failed to save review outcome: " + throwable.getMessage());
                    currentIndex++;
                    loadCurrentReview();
                }
        );
    }

    private void setOutcomeButtonsDisable(boolean disable) {
        if (btnAgain != null) btnAgain.setDisable(disable);
        if (btnHard != null) btnHard.setDisable(disable);
        if (btnGood != null) btnGood.setDisable(disable);
        if (btnEasy != null) btnEasy.setDisable(disable);
    }

    private void showCompletedState() {
        boxReviewContainer.setVisible(false);
        boxReviewContainer.setManaged(false);
        boxCompleted.setVisible(true);
        boxCompleted.setManaged(true);
        lblCompletedSummary.setText(String.format("You've successfully completed %d %s in this session!",
                completedCount, completedCount == 1 ? "review" : "reviews"));

        if (onFinished != null) {
            onFinished.run();
        }
    }

    @FXML
    private void handleOpenNote() {
        if (currentIndex < 0 || currentIndex >= reviewQueue.size()) return;
        Note note = reviewQueue.get(currentIndex).getNote();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/note_view.fxml"));
            Parent root = loader.load();

            NoteViewController controller = loader.getController();
            Stage stage = new Stage();
            stage.setTitle("View Note - " + note.getTitle());
            stage.initModality(Modality.APPLICATION_MODAL);
            if (dialogStage != null) {
                stage.initOwner(dialogStage);
            }
            stage.setScene(new Scene(root));
            controller.setDialogStage(stage);
            controller.setNote(note);
            controller.setEditHandler(this::handleEditNote);
            stage.showAndWait();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Could not open note viewer: " + e.getMessage(), e);
            UiUtils.showError("Error", "Could not open note viewer: " + e.getMessage());
        }
    }

    @FXML
    private void handleOpenInMindMap() {
        if (currentIndex < 0 || currentIndex >= reviewQueue.size()) return;
        Note note = reviewQueue.get(currentIndex).getNote();

        MainController main = MainController.getInstance();
        if (main != null) {
            main.openInMindMap(note);
            handleClose();
        }
    }

    @FXML
    private void handleClose() {
        if (dialogStage != null) {
            dialogStage.close();
        }
        if (onFinished != null) {
            onFinished.run();
        }
    }

    private void handleEditNote(com.mindmap.model.Note note) {
        if (note == null) return;
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/note_editor.fxml"));
            javafx.scene.Parent root = loader.load();
            com.mindmap.controller.NoteEditorController controller = loader.getController();
            controller.setNoteService(new com.mindmap.service.NoteService());

            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("Edit Note - " + note.getTitle());
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            if (dialogStage != null) {
                stage.initOwner(dialogStage);
            }
            stage.setScene(new javafx.scene.Scene(root));
            controller.setDialogStage(stage);

            com.mindmap.model.Note targetNote = new com.mindmap.service.NoteService().getNoteWithTags(note.getId()).orElse(note);
            controller.setNote(targetNote, com.mindmap.controller.NoteEditorMode.EDIT);

            stage.showAndWait();

            if (controller.isSaved()) {
                // Refresh the current note in the queue if needed, or just let it be.
                com.mindmap.model.Note updatedNote = new com.mindmap.service.NoteService().getNoteWithTags(note.getId()).orElse(note);
                if (currentIndex >= 0 && currentIndex < reviewQueue.size()) {
                    
                    loadCurrentReview();
                }
            }
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }
}