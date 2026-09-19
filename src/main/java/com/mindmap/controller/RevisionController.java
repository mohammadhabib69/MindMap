package com.mindmap.controller;

import com.mindmap.model.Difficulty;
import com.mindmap.model.Note;
import com.mindmap.model.ScheduledReview;
import com.mindmap.service.NoteService;
import com.mindmap.service.RevisionService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for the Revision (Spaced Repetition) screen.
 * Displays due reviews today, upcoming future reviews, memory engine stats,
 * and controls for starting active recall sessions or scheduling notes.
 */
public class RevisionController {

    private static final Logger LOGGER = Logger.getLogger(RevisionController.class.getName());
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy");

    @FXML private Label lblStatDueToday;
    @FXML private Label lblStatUpcoming;
    @FXML private Label lblStatTotal;
    @FXML private Label lblDueBadge;
    @FXML private Label lblUpcomingBadge;

    @FXML private Button btnStartSession;
    @FXML private Button btnAddNote;
    @FXML private Button btnScheduleAll;
    @FXML private Button btnRefresh;

    @FXML private VBox boxDueContainer;
    @FXML private VBox boxEmptyDue;
    @FXML private VBox boxDueList;

    @FXML private VBox boxUpcomingContainer;
    @FXML private VBox boxEmptyUpcoming;
    @FXML private VBox boxUpcomingList;

    private RevisionService revisionService = new RevisionService();
    private NoteService noteService = new NoteService();

    public void setRevisionService(RevisionService revisionService) {
        if (revisionService != null) {
            this.revisionService = revisionService;
        }
    }

    public void setNoteService(NoteService noteService) {
        if (noteService != null) {
            this.noteService = noteService;
        }
    }

    @FXML
    public void initialize() {
        loadRevisionData();
    }

    /**
     * Loads due and upcoming reviews from SQLite and renders the UI.
     */
    public void loadRevisionData() {
        List<ScheduledReview> dueReviews = revisionService.getDueReviews();
        List<ScheduledReview> upcomingReviews = revisionService.getUpcomingReviews();

        int dueCount = dueReviews.size();
        int upcomingCount = upcomingReviews.size();
        int totalScheduled = revisionService.getTotalScheduledCount();

        // 1. Update summary metrics
        if (lblStatDueToday != null) lblStatDueToday.setText(String.valueOf(dueCount));
        if (lblStatUpcoming != null) lblStatUpcoming.setText(String.valueOf(upcomingCount));
        if (lblStatTotal != null) lblStatTotal.setText(String.valueOf(totalScheduled));
        if (lblDueBadge != null) lblDueBadge.setText(String.valueOf(dueCount));
        if (lblUpcomingBadge != null) lblUpcomingBadge.setText(String.valueOf(upcomingCount));

        if (btnStartSession != null) {
            btnStartSession.setDisable(dueCount == 0);
        }

        // 2. Render Due Today column
        renderDueList(dueReviews);

        // 3. Render Upcoming column
        renderUpcomingList(upcomingReviews);
    }

    private void renderDueList(List<ScheduledReview> dueReviews) {
        if (boxDueList == null) return;
        boxDueList.getChildren().clear();

        if (dueReviews.isEmpty()) {
            if (boxEmptyDue != null) {
                boxEmptyDue.setVisible(true);
                boxEmptyDue.setManaged(true);
            }
        } else {
            if (boxEmptyDue != null) {
                boxEmptyDue.setVisible(false);
                boxEmptyDue.setManaged(false);
            }
            for (ScheduledReview sr : dueReviews) {
                boxDueList.getChildren().add(createDueCard(sr));
            }
        }
    }

    private VBox createDueCard(ScheduledReview sr) {
        VBox card = new VBox(6);
        card.getStyleClass().add("review-item-card");

        Note note = sr.getNote();

        // Title row
        HBox titleRow = new HBox(8);
        titleRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label lblTitle = new Label(note.getTitle() != null ? note.getTitle() : "Untitled Note");
        lblTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #0f172a;");
        lblTitle.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(lblTitle, Priority.ALWAYS);

        titleRow.getChildren().add(lblTitle);

        if (sr.isOverdue()) {
            Label lblOverdue = new Label("OVERDUE");
            lblOverdue.getStyleClass().add("badge-hard");
            lblOverdue.setStyle("-fx-font-size: 10px; -fx-padding: 2 6;");
            titleRow.getChildren().add(lblOverdue);
        }

        Label lblInterval = new Label(sr.getIntervalDays() + "d");
        lblInterval.getStyleClass().add("badge-interval");
        lblInterval.setStyle("-fx-font-size: 10px; -fx-padding: 2 6;");
        titleRow.getChildren().add(lblInterval);

        // Meta row
        HBox metaRow = new HBox(6);
        metaRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        if (note.getSubject() != null && !note.getSubject().isBlank()) {
            Label lblSubj = new Label(note.getSubject().trim());
            lblSubj.getStyleClass().add("badge-subject");
            lblSubj.setStyle("-fx-font-size: 10px; -fx-padding: 1 6;");
            metaRow.getChildren().add(lblSubj);
        }

        String diff = note.getDifficulty() != null ? note.getDifficulty().toUpperCase() : Difficulty.MEDIUM.name();
        Label lblDiff = new Label(diff);
        lblDiff.setStyle("-fx-font-size: 10px; -fx-padding: 1 6;");
        switch (diff) {
            case "EASY" -> lblDiff.getStyleClass().add("badge-easy");
            case "HARD" -> lblDiff.getStyleClass().add("badge-hard");
            default -> lblDiff.getStyleClass().add("badge-medium");
        }
        metaRow.getChildren().add(lblDiff);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        metaRow.getChildren().add(spacer);

        Label lblDueStatus = new Label(sr.isOverdue()
                ? Math.abs(sr.getDaysUntilDue()) + " days overdue"
                : "Due today");
        lblDueStatus.setStyle("-fx-font-size: 11px; -fx-text-fill: #dc2626; -fx-font-weight: 600;");
        metaRow.getChildren().add(lblDueStatus);

        // Action row
        HBox actionRow = new HBox(6);
        actionRow.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);

        Button btnReview = new Button("▶ Review");
        btnReview.getStyleClass().add("btn-primary");
        btnReview.setStyle("-fx-font-size: 11px; -fx-padding: 4 10;");
        btnReview.setOnAction(e -> handleReviewSingle(sr));

        Button btnView = new Button("👁 View");
        btnView.getStyleClass().add("btn-secondary");
        btnView.setStyle("-fx-font-size: 11px; -fx-padding: 4 10;");
        btnView.setOnAction(e -> handleViewNote(sr.getNote()));

        Button btnMindMap = new Button("🌐 Mind Map");
        btnMindMap.getStyleClass().add("btn-secondary");
        btnMindMap.setStyle("-fx-font-size: 11px; -fx-padding: 4 10;");
        btnMindMap.setOnAction(e -> handleOpenInMindMap(sr.getNote()));

        actionRow.getChildren().addAll(btnReview, btnView, btnMindMap);

        card.getChildren().addAll(titleRow, metaRow, actionRow);
        return card;
    }

    private void renderUpcomingList(List<ScheduledReview> upcomingReviews) {
        if (boxUpcomingList == null) return;
        boxUpcomingList.getChildren().clear();

        if (upcomingReviews.isEmpty()) {
            if (boxEmptyUpcoming != null) {
                boxEmptyUpcoming.setVisible(true);
                boxEmptyUpcoming.setManaged(true);
            }
        } else {
            if (boxEmptyUpcoming != null) {
                boxEmptyUpcoming.setVisible(false);
                boxEmptyUpcoming.setManaged(false);
            }
            for (ScheduledReview sr : upcomingReviews) {
                boxUpcomingList.getChildren().add(createUpcomingCard(sr));
            }
        }
    }

    private VBox createUpcomingCard(ScheduledReview sr) {
        VBox card = new VBox(6);
        card.getStyleClass().add("review-item-card");

        Note note = sr.getNote();

        // Title row
        HBox titleRow = new HBox(8);
        titleRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label lblTitle = new Label(note.getTitle() != null ? note.getTitle() : "Untitled Note");
        lblTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #0f172a;");
        lblTitle.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(lblTitle, Priority.ALWAYS);

        titleRow.getChildren().add(lblTitle);

        Label lblInterval = new Label(sr.getIntervalDays() + "d");
        lblInterval.getStyleClass().add("badge-interval");
        lblInterval.setStyle("-fx-font-size: 10px; -fx-padding: 2 6;");
        titleRow.getChildren().add(lblInterval);

        // Schedule & Meta row
        HBox metaRow = new HBox(6);
        metaRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        if (note.getSubject() != null && !note.getSubject().isBlank()) {
            Label lblSubj = new Label(note.getSubject().trim());
            lblSubj.getStyleClass().add("badge-subject");
            lblSubj.setStyle("-fx-font-size: 10px; -fx-padding: 1 6;");
            metaRow.getChildren().add(lblSubj);
        }

        String diff = note.getDifficulty() != null ? note.getDifficulty().toUpperCase() : Difficulty.MEDIUM.name();
        Label lblDiff = new Label(diff);
        lblDiff.setStyle("-fx-font-size: 10px; -fx-padding: 1 6;");
        switch (diff) {
            case "EASY" -> lblDiff.getStyleClass().add("badge-easy");
            case "HARD" -> lblDiff.getStyleClass().add("badge-hard");
            default -> lblDiff.getStyleClass().add("badge-medium");
        }
        metaRow.getChildren().add(lblDiff);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        metaRow.getChildren().add(spacer);

        long days = sr.getDaysUntilDue();
        String dateText = (days == 1) ? "Tomorrow" : "In " + days + " days";
        if (sr.getReviewDate() != null) {
            dateText += " (" + sr.getReviewDate().format(DATE_FORMATTER) + ")";
        }
        Label lblDate = new Label("📅 " + dateText);
        lblDate.setStyle("-fx-font-size: 11px; -fx-text-fill: #2563eb; -fx-font-weight: 500;");
        metaRow.getChildren().add(lblDate);

        // Action row
        HBox actionRow = new HBox(6);
        actionRow.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);

        Button btnView = new Button("👁 View");
        btnView.getStyleClass().add("btn-secondary");
        btnView.setStyle("-fx-font-size: 11px; -fx-padding: 4 10;");
        btnView.setOnAction(e -> handleViewNote(sr.getNote()));

        Button btnMindMap = new Button("🌐 Mind Map");
        btnMindMap.getStyleClass().add("btn-secondary");
        btnMindMap.setStyle("-fx-font-size: 11px; -fx-padding: 4 10;");
        btnMindMap.setOnAction(e -> handleOpenInMindMap(sr.getNote()));

        actionRow.getChildren().addAll(btnView, btnMindMap);

        card.getChildren().addAll(titleRow, metaRow, actionRow);
        return card;
    }

    @FXML
    private void handleStartSession() {
        List<ScheduledReview> due = revisionService.getDueReviews();
        if (due.isEmpty()) return;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/review_dialog.fxml"));
            Parent root = loader.load();

            ReviewDialogController controller = loader.getController();
            Stage stage = new Stage();
            stage.setTitle("Spaced Repetition Review Session");
            stage.initModality(Modality.APPLICATION_MODAL);
            if (btnStartSession != null && btnStartSession.getScene() != null && btnStartSession.getScene().getWindow() != null) {
                stage.initOwner(btnStartSession.getScene().getWindow());
            }
            stage.setScene(new Scene(root));
            controller.setDialogStage(stage);
            controller.setRevisionService(revisionService);
            controller.setOnFinished(this::loadRevisionData);
            controller.setReviews(due);
            stage.showAndWait();
            loadRevisionData();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to open Review Dialog: " + e.getMessage(), e);
        }
    }

    private void handleReviewSingle(ScheduledReview sr) {
        if (sr == null) return;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/review_dialog.fxml"));
            Parent root = loader.load();

            ReviewDialogController controller = loader.getController();
            Stage stage = new Stage();
            stage.setTitle("Review Note - " + (sr.getNote() != null ? sr.getNote().getTitle() : ""));
            stage.initModality(Modality.APPLICATION_MODAL);
            if (btnStartSession != null && btnStartSession.getScene() != null && btnStartSession.getScene().getWindow() != null) {
                stage.initOwner(btnStartSession.getScene().getWindow());
            }
            stage.setScene(new Scene(root));
            controller.setDialogStage(stage);
            controller.setRevisionService(revisionService);
            controller.setOnFinished(this::loadRevisionData);
            controller.setSingleReview(sr);
            stage.showAndWait();
            loadRevisionData();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to open single Review Dialog: " + e.getMessage(), e);
        }
    }

    private void handleViewNote(Note note) {
        if (note == null) return;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/note_view.fxml"));
            Parent root = loader.load();

            NoteViewController controller = loader.getController();
            Stage stage = new Stage();
            stage.setTitle("View Note - " + note.getTitle());
            stage.initModality(Modality.APPLICATION_MODAL);
            if (btnStartSession != null && btnStartSession.getScene() != null && btnStartSession.getScene().getWindow() != null) {
                stage.initOwner(btnStartSession.getScene().getWindow());
            }
            stage.setScene(new Scene(root));
            controller.setDialogStage(stage);
            controller.setNote(note);
            stage.showAndWait();
            loadRevisionData();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Could not open note viewer: " + e.getMessage(), e);
        }
    }

    private void handleOpenInMindMap(Note note) {
        if (note == null) return;
        MainController main = MainController.getInstance();
        if (main != null) {
            main.openInMindMap(note);
        }
    }

    @FXML
    private void handleAddNote() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/schedule_note_dialog.fxml"));
            Parent root = loader.load();

            ScheduleNoteDialogController controller = loader.getController();
            Stage stage = new Stage();
            stage.setTitle("Schedule Note for Revision");
            stage.initModality(Modality.APPLICATION_MODAL);
            if (btnStartSession != null && btnStartSession.getScene() != null && btnStartSession.getScene().getWindow() != null) {
                stage.initOwner(btnStartSession.getScene().getWindow());
            }
            stage.setScene(new Scene(root));
            controller.setDialogStage(stage);
            controller.setServices(revisionService, noteService);
            stage.showAndWait();

            if (controller.isScheduled()) {
                loadRevisionData();
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to open Schedule Note dialog: " + e.getMessage(), e);
        }
    }

    @FXML
    private void handleScheduleAll() {
        int scheduledCount = revisionService.scheduleAllUnscheduledNotes();
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Schedule Notes");
        alert.setHeaderText(null);
        if (scheduledCount > 0) {
            alert.setContentText("Successfully added " + scheduledCount + " " +
                    (scheduledCount == 1 ? "note" : "notes") + " to the Spaced Repetition queue!");
        } else {
            alert.setContentText("All notes are already scheduled in the Spaced Repetition system.");
        }
        alert.showAndWait();
        loadRevisionData();
    }

    @FXML
    private void handleRefresh() {
        loadRevisionData();
    }
}

