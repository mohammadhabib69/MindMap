package com.mindmap.controller;

import com.mindmap.concurrency.TaskExecutor;
import com.mindmap.model.Difficulty;
import com.mindmap.model.Note;
import com.mindmap.model.ScheduledReview;
import com.mindmap.service.NoteService;
import com.mindmap.service.RevisionService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.mindmap.util.UiUtils;

/**
 * Controller for the Revision (Spaced Repetition) screen.
 * Uses virtualized ListView with cell reuse for smooth scrolling with 200+ reviews.
 * Data is cached in ObservableList — no DB queries during scroll or resize.
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

    @FXML private ListView<ScheduledReview> listDue;
    @FXML private VBox boxEmptyDue;

    @FXML private ListView<ScheduledReview> listUpcoming;
    @FXML private VBox boxEmptyUpcoming;

    // Cached data — no DB queries during scroll/resize
    private final ObservableList<ScheduledReview> dueData = FXCollections.observableArrayList();
    private final ObservableList<ScheduledReview> upcomingData = FXCollections.observableArrayList();

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
        setupListViews();
        loadRevisionData();
    }

    /**
     * Configures virtualized ListViews with cell factories that reuse cells.
     * Fixed cell height enables O(1) scroll offset calculation in JavaFX VirtualFlow.
     */
    private void setupListViews() {
        if (listDue != null) {
            listDue.setItems(dueData);
            listDue.setFocusTraversable(false);
            listDue.setFixedCellSize(108.0);
            listDue.setCellFactory(lv -> new DueReviewCell());
        }

        if (listUpcoming != null) {
            listUpcoming.setItems(upcomingData);
            listUpcoming.setFocusTraversable(false);
            listUpcoming.setFixedCellSize(108.0);
            listUpcoming.setCellFactory(lv -> new UpcomingReviewCell());
        }
    }

    public ObservableList<ScheduledReview> getDueData() {
        return dueData;
    }

    public ObservableList<ScheduledReview> getUpcomingData() {
        return upcomingData;
    }

    public ListView<ScheduledReview> getListDue() {
        return listDue;
    }

    public ListView<ScheduledReview> getListUpcoming() {
        return listUpcoming;
    }

    public record RevisionSnapshot(List<ScheduledReview> due, List<ScheduledReview> upcoming, int totalScheduled) {}

    /**
     * Loads due and upcoming reviews from SQLite in the background
     * and populates the cached ObservableLists safely on the JavaFX thread.
     *
     * @return CompletableFuture holding the loaded snapshot.
     */
    public CompletableFuture<RevisionSnapshot> loadRevisionData() {
        if (btnRefresh != null) {
            btnRefresh.setDisable(true);
            btnRefresh.setText("⏳");
        }

        final RevisionService service = this.revisionService;
        CompletableFuture<RevisionSnapshot> future = new CompletableFuture<>();

        TaskExecutor.runAsync(
                () -> {
                    List<ScheduledReview> due = service.getDueReviews();
                    List<ScheduledReview> upcoming = service.getUpcomingReviews();
                    int total = service.getTotalScheduledCount();
                    return new RevisionSnapshot(due, upcoming, total);
                },
                snapshot -> {
                    try {
                        applyRevisionSnapshot(snapshot);
                        future.complete(snapshot);
                    } catch (Exception e) {
                        LOGGER.log(Level.SEVERE, "Error updating revision UI: " + e.getMessage(), e);
                        future.completeExceptionally(e);
                    } finally {
                        if (btnRefresh != null) {
                            btnRefresh.setDisable(false);
                            btnRefresh.setText("🔄 Refresh");
                        }
                    }
                },
                throwable -> {
                    LOGGER.log(Level.SEVERE, "Failed to load revision data: " + throwable.getMessage(), throwable);
                    if (btnRefresh != null) {
                        btnRefresh.setDisable(false);
                        btnRefresh.setText("🔄 Refresh");
                    }
                    future.completeExceptionally(throwable);
                }
        );

        return future;
    }

    /**
     * Synchronous variant for tests or immediate retrieval.
     */
    public void loadRevisionDataSync() {
        List<ScheduledReview> due = revisionService.getDueReviews();
        List<ScheduledReview> upcoming = revisionService.getUpcomingReviews();
        int total = revisionService.getTotalScheduledCount();
        applyRevisionSnapshot(new RevisionSnapshot(due, upcoming, total));
    }

    private void applyRevisionSnapshot(RevisionSnapshot snapshot) {
        int dueCount = snapshot.due().size();
        int upcomingCount = snapshot.upcoming().size();
        int totalScheduled = snapshot.totalScheduled();

        // 1. Update summary metrics
        if (lblStatDueToday != null) lblStatDueToday.setText(String.valueOf(dueCount));
        if (lblStatUpcoming != null) lblStatUpcoming.setText(String.valueOf(upcomingCount));
        if (lblStatTotal != null) lblStatTotal.setText(String.valueOf(totalScheduled));
        if (lblDueBadge != null) lblDueBadge.setText(String.valueOf(dueCount));
        if (lblUpcomingBadge != null) lblUpcomingBadge.setText(String.valueOf(upcomingCount));

        if (btnStartSession != null) {
            btnStartSession.setDisable(dueCount == 0);
        }

        // 2. Update cached data (triggers ListView refresh via ObservableList)
        dueData.setAll(snapshot.due());
        upcomingData.setAll(snapshot.upcoming());

        // 3. Toggle empty states
        updateEmptyState(boxEmptyDue, listDue, dueCount == 0);
        updateEmptyState(boxEmptyUpcoming, listUpcoming, upcomingCount == 0);
    }

    private void updateEmptyState(VBox emptyBox, ListView<?> listView, boolean isEmpty) {
        if (emptyBox != null) {
            emptyBox.setVisible(isEmpty);
            emptyBox.setManaged(isEmpty);
        }
        if (listView != null) {
            listView.setVisible(!isEmpty);
            listView.setManaged(!isEmpty);
        }
    }

    // =========================================================================
    // Virtualized Cell: Due Review
    // =========================================================================

    /**
     * Custom ListCell for due reviews. Creates UI hierarchy once in constructor,
     * then only updates data in updateItem(). Guarantees cell reuse.
     */
    private class DueReviewCell extends ListCell<ScheduledReview> {
        private final VBox card;
        private final Label lblTitle;
        private final Label lblOverdue;
        private final Label lblInterval;
        private final Label lblSubject;
        private final Label lblDifficulty;
        private final Label lblDueStatus;
        private final Button btnReview;
        private final Button btnView;
        private final Button btnMindMap;

        DueReviewCell() {
            card = new VBox(6);
            card.getStyleClass().add("revision-cell-card");

            // Title row
            HBox titleRow = new HBox(8);
            titleRow.setAlignment(Pos.CENTER_LEFT);

            lblTitle = new Label();
            lblTitle.getStyleClass().add("revision-cell-title");
            lblTitle.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(lblTitle, Priority.ALWAYS);

            lblOverdue = new Label("OVERDUE");
            lblOverdue.getStyleClass().add("badge-hard");
            lblOverdue.setStyle("-fx-font-size: 10px; -fx-padding: 2 6;");

            lblInterval = new Label();
            lblInterval.getStyleClass().add("badge-interval");
            lblInterval.setStyle("-fx-font-size: 10px; -fx-padding: 2 6;");

            titleRow.getChildren().addAll(lblTitle, lblOverdue, lblInterval);

            // Meta row
            HBox metaRow = new HBox(6);
            metaRow.setAlignment(Pos.CENTER_LEFT);

            lblSubject = new Label();
            lblSubject.getStyleClass().add("badge-subject");
            lblSubject.setStyle("-fx-font-size: 10px; -fx-padding: 1 6;");

            lblDifficulty = new Label();
            lblDifficulty.setStyle("-fx-font-size: 10px; -fx-padding: 1 6;");

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            lblDueStatus = new Label();
            lblDueStatus.getStyleClass().add("revision-cell-due-status");

            metaRow.getChildren().addAll(lblSubject, lblDifficulty, spacer, lblDueStatus);

            // Action row
            HBox actionRow = new HBox(6);
            actionRow.setAlignment(Pos.CENTER_RIGHT);

            btnReview = new Button("▶ Review");
            btnReview.getStyleClass().addAll("revision-cell-btn", "revision-cell-btn-review");

            btnView = new Button("👁 View");
            btnView.getStyleClass().addAll("revision-cell-btn", "revision-cell-btn-secondary");

            btnMindMap = new Button("🌐 Map");
            btnMindMap.getStyleClass().addAll("revision-cell-btn", "revision-cell-btn-secondary");

            actionRow.getChildren().addAll(btnReview, btnView, btnMindMap);

            card.getChildren().addAll(titleRow, metaRow, actionRow);

            setGraphic(null);
            setText(null);
            setPrefWidth(0);
        }

        @Override
        protected void updateItem(ScheduledReview sr, boolean empty) {
            super.updateItem(sr, empty);

            if (empty || sr == null) {
                setGraphic(null);
                return;
            }

            Note note = sr.getNote();

            // Update title
            lblTitle.setText(note.getTitle() != null ? note.getTitle() : "Untitled Note");

            // Update overdue badge
            boolean overdue = sr.isOverdue();
            lblOverdue.setVisible(overdue);
            lblOverdue.setManaged(overdue);

            // Update interval
            lblInterval.setText(sr.getIntervalDays() + "d");

            // Update subject
            boolean hasSubject = note.getSubject() != null && !note.getSubject().isBlank();
            lblSubject.setVisible(hasSubject);
            lblSubject.setManaged(hasSubject);
            if (hasSubject) {
                lblSubject.setText(note.getSubject().trim());
            }

            // Update difficulty badge
            String diff = note.getDifficulty() != null ? note.getDifficulty().toUpperCase() : Difficulty.MEDIUM.name();
            lblDifficulty.setText(diff);
            lblDifficulty.getStyleClass().removeAll("badge-easy", "badge-medium", "badge-hard");
            switch (diff) {
                case "EASY" -> lblDifficulty.getStyleClass().add("badge-easy");
                case "HARD" -> lblDifficulty.getStyleClass().add("badge-hard");
                default -> lblDifficulty.getStyleClass().add("badge-medium");
            }

            // Update due status
            lblDueStatus.setText(overdue
                    ? Math.abs(sr.getDaysUntilDue()) + " days overdue"
                    : "Due today");

            // Wire actions (re-wire on each update since the item may have changed)
            btnReview.setOnAction(e -> handleReviewSingle(sr));
            btnView.setOnAction(e -> handleViewNote(note));
            btnMindMap.setOnAction(e -> handleOpenInMindMap(note));

            setGraphic(card);
        }
    }

    // =========================================================================
    // Virtualized Cell: Upcoming Review
    // =========================================================================

    /**
     * Custom ListCell for upcoming reviews. Creates UI once, updates data on reuse.
     */
    private class UpcomingReviewCell extends ListCell<ScheduledReview> {
        private final VBox card;
        private final Label lblTitle;
        private final Label lblInterval;
        private final Label lblSubject;
        private final Label lblDifficulty;
        private final Label lblDate;
        private final Button btnView;
        private final Button btnMindMap;

        UpcomingReviewCell() {
            card = new VBox(6);
            card.getStyleClass().add("revision-cell-card");

            // Title row
            HBox titleRow = new HBox(8);
            titleRow.setAlignment(Pos.CENTER_LEFT);

            lblTitle = new Label();
            lblTitle.getStyleClass().add("revision-cell-title");
            lblTitle.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(lblTitle, Priority.ALWAYS);

            lblInterval = new Label();
            lblInterval.getStyleClass().add("badge-interval");
            lblInterval.setStyle("-fx-font-size: 10px; -fx-padding: 2 6;");

            titleRow.getChildren().addAll(lblTitle, lblInterval);

            // Meta row
            HBox metaRow = new HBox(6);
            metaRow.setAlignment(Pos.CENTER_LEFT);

            lblSubject = new Label();
            lblSubject.getStyleClass().add("badge-subject");
            lblSubject.setStyle("-fx-font-size: 10px; -fx-padding: 1 6;");

            lblDifficulty = new Label();
            lblDifficulty.setStyle("-fx-font-size: 10px; -fx-padding: 1 6;");

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            lblDate = new Label();
            lblDate.getStyleClass().add("revision-cell-upcoming-date");

            metaRow.getChildren().addAll(lblSubject, lblDifficulty, spacer, lblDate);

            // Action row
            HBox actionRow = new HBox(6);
            actionRow.setAlignment(Pos.CENTER_RIGHT);

            btnView = new Button("👁 View");
            btnView.getStyleClass().addAll("revision-cell-btn", "revision-cell-btn-secondary");

            btnMindMap = new Button("🌐 Map");
            btnMindMap.getStyleClass().addAll("revision-cell-btn", "revision-cell-btn-secondary");

            actionRow.getChildren().addAll(btnView, btnMindMap);

            card.getChildren().addAll(titleRow, metaRow, actionRow);

            setGraphic(null);
            setText(null);
            setPrefWidth(0);
        }

        @Override
        protected void updateItem(ScheduledReview sr, boolean empty) {
            super.updateItem(sr, empty);

            if (empty || sr == null) {
                setGraphic(null);
                return;
            }

            Note note = sr.getNote();

            // Update title
            lblTitle.setText(note.getTitle() != null ? note.getTitle() : "Untitled Note");

            // Update interval
            lblInterval.setText(sr.getIntervalDays() + "d");

            // Update subject
            boolean hasSubject = note.getSubject() != null && !note.getSubject().isBlank();
            lblSubject.setVisible(hasSubject);
            lblSubject.setManaged(hasSubject);
            if (hasSubject) {
                lblSubject.setText(note.getSubject().trim());
            }

            // Update difficulty badge
            String diff = note.getDifficulty() != null ? note.getDifficulty().toUpperCase() : Difficulty.MEDIUM.name();
            lblDifficulty.setText(diff);
            lblDifficulty.getStyleClass().removeAll("badge-easy", "badge-medium", "badge-hard");
            switch (diff) {
                case "EASY" -> lblDifficulty.getStyleClass().add("badge-easy");
                case "HARD" -> lblDifficulty.getStyleClass().add("badge-hard");
                default -> lblDifficulty.getStyleClass().add("badge-medium");
            }

            // Update date
            long days = sr.getDaysUntilDue();
            String dateText = (days == 1) ? "Tomorrow" : "In " + days + " days";
            if (sr.getReviewDate() != null) {
                dateText += " (" + sr.getReviewDate().format(DATE_FORMATTER) + ")";
            }
            lblDate.setText("📅 " + dateText);

            // Wire actions
            btnView.setOnAction(e -> handleViewNote(note));
            btnMindMap.setOnAction(e -> handleOpenInMindMap(note));

            setGraphic(card);
        }
    }

    // =========================================================================
    // Action Handlers
    // =========================================================================

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
            UiUtils.showError("Error", "Failed to open Review Dialog: " + e.getMessage());
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
            UiUtils.showError("Error", "Failed to open single Review Dialog: " + e.getMessage());
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
            UiUtils.showError("Error", "Could not open note viewer: " + e.getMessage());
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
            UiUtils.showError("Error", "Failed to open Schedule Note dialog: " + e.getMessage());
        }
    }

    @FXML
    private void handleScheduleAll() {
        if (btnScheduleAll != null) {
            btnScheduleAll.setDisable(true);
            btnScheduleAll.setText("⚡ Scheduling...");
        }

        final RevisionService service = this.revisionService;
        TaskExecutor.runAsync(
                service::scheduleAllUnscheduledNotes,
                scheduledCount -> {
                    if (btnScheduleAll != null) {
                        btnScheduleAll.setDisable(false);
                        btnScheduleAll.setText("⚡ Schedule All");
                    }
                    if (scheduledCount > 0) {
                        UiUtils.showInfo("Schedule Notes", "Successfully added " + scheduledCount + " " +
                                (scheduledCount == 1 ? "note" : "notes") + " to the Spaced Repetition queue!");
                    } else {
                        UiUtils.showInfo("Schedule Notes", "All notes are already scheduled in the Spaced Repetition system.");
                    }
                    loadRevisionData();
                },
                throwable -> {
                    LOGGER.log(Level.SEVERE, "Failed to schedule all notes: " + throwable.getMessage(), throwable);
                    UiUtils.showError("Error", "Failed to schedule all notes: " + throwable.getMessage());
                    if (btnScheduleAll != null) {
                        btnScheduleAll.setDisable(false);
                        btnScheduleAll.setText("⚡ Schedule All");
                    }
                }
        );
    }

    @FXML
    private void handleRefresh() {
        loadRevisionData();
    }
}
