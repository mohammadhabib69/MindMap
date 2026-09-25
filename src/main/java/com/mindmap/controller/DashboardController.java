package com.mindmap.controller;

import com.mindmap.concurrency.TaskExecutor;
import com.mindmap.model.DashboardStats;
import com.mindmap.model.Difficulty;
import com.mindmap.model.Note;
import com.mindmap.model.TimelineEvent;
import com.mindmap.service.DashboardService;

import com.mindmap.service.StudyRecommendationService;
import com.mindmap.service.StudyRecommendationService.Recommendation;

import com.mindmap.service.NoteService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.mindmap.util.UiUtils;

/**
 * Controller for the data-driven Learning Dashboard screen.
 * Displays key metrics, distribution charts, revision progress, recent activity,
 * and recent notes using real SQLite data via DashboardService.
 * Features responsive FlowPane-based wrapping, background multithreaded loading,
 * and lightweight layout adaptation.
 */
public class DashboardController {

    private static final Logger LOGGER = Logger.getLogger(DashboardController.class.getName());
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy");

    // Header Controls
    @FXML private Button btnRefresh;
    @FXML private Button btnNewNote;
    @FXML private Button btnStartReview;
    @FXML private Button btnOpenMindMap;

    // Layout Containers & Responsive Cards
    @FXML private ScrollPane scrollDashboard;
    @FXML private VBox contentContainer;

    // Primary Metric Cards
    @FXML private FlowPane paneMetrics;
    @FXML private VBox cardTotalNotes;
    @FXML private VBox cardTotalConnections;
    @FXML private VBox cardReviewsDue;
    @FXML private VBox cardUpcomingReviews;
    @FXML private VBox cardTotalTags;


    // Phase 18 Features
    @FXML private VBox boxStudyNext;
    @FXML private Label lblEmptyStudy;
    @FXML private VBox boxFavorites;
    @FXML private Label lblEmptyFavorites;
    @FXML private VBox boxRecentlyViewed;
    @FXML private Label lblEmptyRecent;

    // Primary Metric Labels
    @FXML private Label lblTotalNotes;
    @FXML private Label lblTotalConnections;
@FXML
    private javafx.scene.layout.VBox cardPrivateNotes;
    @FXML private Label lblDueToday;
    @FXML private Label lblUpcomingReviews;
    @FXML private Label lblTotalTags;

    // Middle Section: Knowledge Distribution & Revision
    @FXML private FlowPane paneMiddleSection;
    @FXML private VBox cardKnowledgeDistribution;
    @FXML private VBox cardRevisionOverview;

    // Charts
    @FXML private HBox boxChartsContainer;
    @FXML private VBox boxEmptyCharts;
    @FXML private BarChart<String, Number> chartSubjects;
    @FXML private PieChart chartDifficulty;

    // Revision Summary
    @FXML private Label lblCompletedToday;
    @FXML private Label lblRevisionDue;
    @FXML private Label lblTotalScheduled;
    @FXML private Label lblReviewProgressPercent;
    @FXML private ProgressBar progressReview;
    @FXML private Label lblReviewProgressText;

    // Knowledge Graph Summary
    @FXML private Label lblConnectedNotes;
    @FXML private Label lblIsolatedNotes;
    @FXML private Label lblGraphRatio;

    // Lower Section: Recent Activity & Recent Notes
    @FXML private FlowPane paneLowerSection;
    @FXML private VBox cardRecentActivity;
    @FXML private VBox cardRecentNotes;
    @FXML private VBox boxRecentActivity;
    @FXML private VBox boxEmptyActivity;
    @FXML private VBox boxRecentNotes;
    @FXML private VBox boxEmptyNotes;

    private DashboardService dashboardService;
    private NoteService noteService;

    public DashboardController() {
        this(new DashboardService(), new NoteService());
    }

    public DashboardController(DashboardService dashboardService, NoteService noteService) {
        this.dashboardService = dashboardService != null ? dashboardService : new DashboardService();
        this.noteService = noteService != null ? noteService : new NoteService();
    }

    public void setDashboardService(DashboardService dashboardService) {
        if (dashboardService != null) {
            this.dashboardService = dashboardService;
        }
    }

    public DashboardService getDashboardService() {
        return dashboardService;
    }

    public ScrollPane getScrollDashboard() {
        return scrollDashboard;
    }

    public VBox getContentContainer() {
        return contentContainer;
    }

    public FlowPane getPaneMetrics() {
        return paneMetrics;
    }

    public FlowPane getPaneMiddleSection() {
        return paneMiddleSection;
    }

    public FlowPane getPaneLowerSection() {
        return paneLowerSection;
    }

    public BarChart<String, Number> getChartSubjects() {
        return chartSubjects;
    }

    public PieChart getChartDifficulty() {
        return chartDifficulty;
    }

    public VBox getCardKnowledgeDistribution() {
        return cardKnowledgeDistribution;
    }

    public VBox getCardRevisionOverview() {
        return cardRevisionOverview;
    }

    @FXML
    public void initialize() {
        setupResponsiveLayout();
        loadDashboardData();
    }

    /**
     * Sets up a lightweight listener on container width to adapt card layout smoothly.
     * Operates purely on layout properties; never triggers DB queries or chart reloads.
     */
    private void setupResponsiveLayout() {
        if (contentContainer != null) {
            contentContainer.widthProperty().addListener((obs, oldW, newW) -> {
                if (newW != null && newW.doubleValue() > 0) {
                    applyResponsiveWidths(newW.doubleValue());
                }
            });
        }
    }

    /**
     * Lightweight responsive layout calculation.
     * Adjusts prefWidth of dashboard cards so they wrap naturally into rows
     * and expand to fill available space without horizontal scrollbars or clipping.
     * NO database queries, NO chart recreation, NO node reconstruction.
     *
     * @param containerWidth Current width of content container
     */
    public void applyResponsiveWidths(double containerWidth) {
        if (containerWidth <= 100) return;

        // Content padding is 24px left + 24px right = 48px
        double usableWidth = Math.max(280, containerWidth - 48);

        // 1. Primary Metrics Cards (5 cards, 12px gap)
        if (paneMetrics != null) {
            int cols;
            if (usableWidth >= 860) {
                cols = 5;
            } else if (usableWidth >= 520) {
                cols = 3;
            } else {
                cols = 2;
            }
            double metricCardWidth = Math.floor((usableWidth - (cols - 1) * 12.0) / cols);
            setCardWidth(cardTotalNotes, metricCardWidth);
            setCardWidth(cardTotalConnections, metricCardWidth);
            setCardWidth(cardReviewsDue, metricCardWidth);
            setCardWidth(cardUpcomingReviews, metricCardWidth);
            setCardWidth(cardTotalTags, metricCardWidth);
        }

        // 2. Middle Section (Knowledge Distribution & Revision, 14px gap)
        if (paneMiddleSection != null) {
            if (usableWidth >= 800) {
                // Side-by-side: 54% / 46% split
                double wLeft = Math.floor((usableWidth - 14.0) * 0.54);
                double wRight = Math.floor(usableWidth - 14.0 - wLeft);
                setCardWidth(cardKnowledgeDistribution, wLeft);
                setCardWidth(cardRevisionOverview, wRight);
            } else {
                // Stacked: Both span full usable width
                setCardWidth(cardKnowledgeDistribution, usableWidth);
                setCardWidth(cardRevisionOverview, usableWidth);
            }
        }

        // 3. Lower Section (Recent Activity & Recent Notes, 14px gap)
        if (paneLowerSection != null) {
            if (usableWidth >= 800) {
                // Side-by-side: 52% / 48% split
                double wLeft = Math.floor((usableWidth - 14.0) * 0.52);
                double wRight = Math.floor(usableWidth - 14.0 - wLeft);
                setCardWidth(cardRecentActivity, wLeft);
                setCardWidth(cardRecentNotes, wRight);
            } else {
                // Stacked: Both span full usable width
                setCardWidth(cardRecentActivity, usableWidth);
                setCardWidth(cardRecentNotes, usableWidth);
            }
        }
    }

    private void setCardWidth(Region card, double width) {
        if (card != null && width > 0) {
            card.setPrefWidth(width);
            card.setMaxWidth(width);
        }
    }


    /**
     * Loads aggregated dashboard data asynchronously in a background worker thread.
     * Updates all UI components on the JavaFX Application Thread once retrieved.
     *
     * @return CompletableFuture holding the loaded DashboardStats.
     */
    public CompletableFuture<DashboardStats> loadDashboardData() {
        if (btnRefresh != null) {
            btnRefresh.setDisable(true);
            btnRefresh.setText("⏳ Loading...");
        }

        final DashboardService service = this.dashboardService;
        CompletableFuture<DashboardStats> future = new CompletableFuture<>();
        TaskExecutor.runAsync(
                () -> service.getDashboardStats(),
                stats -> {
                    try {
                        updatePrimaryMetrics(stats);
                        updateCharts(stats);
                        updateRevisionOverview(stats);
                        updateGraphSummary(stats);
                        updateRecentActivity(stats);
            
            updateRecentNotes(stats);
            populateProductivitySection();

                        future.complete(stats);
                    } catch (Exception e) {
                        LOGGER.log(Level.SEVERE, "Error rendering dashboard data: " + e.getMessage(), e);
                        future.completeExceptionally(e);
                    } finally {
                        if (btnRefresh != null) {
                            btnRefresh.setDisable(false);
                            btnRefresh.setText("🔄 Refresh");
                        }
                    }
                },
                throwable -> {
                    LOGGER.log(Level.SEVERE, "Failed to load dashboard data asynchronously: " + throwable.getMessage(), throwable);
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
    public void loadDashboardDataSync() {
        try {
            DashboardStats stats = dashboardService.getDashboardStats();
            updatePrimaryMetrics(stats);
            updateCharts(stats);
            updateRevisionOverview(stats);
            updateGraphSummary(stats);
            updateRecentActivity(stats);

            updateRecentNotes(stats);
            populateProductivitySection();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to load dashboard data synchronously: " + e.getMessage(), e);
        }
    }

    /**
     * Updates top summary cards.
     */
    private void updatePrimaryMetrics(DashboardStats stats) {
        if (lblTotalNotes != null) lblTotalNotes.setText(String.valueOf(stats.getTotalNotes()));
        if (lblTotalConnections != null) lblTotalConnections.setText(String.valueOf(stats.getTotalConnections()));
        if (lblDueToday != null) lblDueToday.setText(String.valueOf(stats.getDueToday()));
        if (lblUpcomingReviews != null) lblUpcomingReviews.setText(String.valueOf(stats.getUpcomingReviews()));
        if (lblTotalTags != null) lblTotalTags.setText(String.valueOf(stats.getTotalTags()));
    }

    /**
     * Populates subject and difficulty distribution charts.
     */
    private void updateCharts(DashboardStats stats) {
        boolean hasNotes = stats.getTotalNotes() > 0;

        if (boxChartsContainer != null) {
            boxChartsContainer.setVisible(hasNotes);
            boxChartsContainer.setManaged(hasNotes);
        }
        if (boxEmptyCharts != null) {
            boxEmptyCharts.setVisible(!hasNotes);
            boxEmptyCharts.setManaged(!hasNotes);
        }

        if (!hasNotes) {
            return;
        }

        // 1. BarChart: Notes by Subject
        if (chartSubjects != null) {
            chartSubjects.getData().clear();
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Subjects");

            for (Map.Entry<String, Integer> entry : stats.getNotesBySubject().entrySet()) {
                String subj = entry.getKey();
                // Truncate overly long subject names for chart display
                String label = subj.length() > 12 ? subj.substring(0, 10) + "…" : subj;
                series.getData().add(new XYChart.Data<>(label, entry.getValue()));
            }
            chartSubjects.getData().add(series);
        }

        // 2. PieChart: Notes by Difficulty
        if (chartDifficulty != null) {
            chartDifficulty.getData().clear();
            ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();

            for (Map.Entry<String, Integer> entry : stats.getNotesByDifficulty().entrySet()) {
                if (entry.getValue() > 0) {
                    pieData.add(new PieChart.Data(entry.getKey() + " (" + entry.getValue() + ")", entry.getValue()));
                }
            }
            chartDifficulty.setData(pieData);
        }
    }

    /**
     * Updates revision statistics and progress bar.
     */
    private void updateRevisionOverview(DashboardStats stats) {
        if (lblCompletedToday != null) lblCompletedToday.setText(String.valueOf(stats.getCompletedToday()));
        if (lblRevisionDue != null) lblRevisionDue.setText(String.valueOf(stats.getDueToday()));
        if (lblTotalScheduled != null) lblTotalScheduled.setText(String.valueOf(stats.getTotalScheduled()));

        int completed = stats.getCompletedToday();
        int due = stats.getDueToday();
        int totalToday = completed + due;
        int percent = stats.getReviewProgressPercent();

        if (lblReviewProgressPercent != null) {
            lblReviewProgressPercent.setText(percent + "%");
        }
        if (progressReview != null) {
            progressReview.setProgress(stats.getReviewProgressRatio());
        }
        if (lblReviewProgressText != null) {
            if (totalToday == 0) {
                lblReviewProgressText.setText("All caught up! No reviews due today.");
            } else {
                lblReviewProgressText.setText(completed + " of " + totalToday + " reviews completed (" + percent + "%)");
            }
        }
    }

    /**
     * Updates knowledge graph connected vs isolated metrics.
     */
    private void updateGraphSummary(DashboardStats stats) {
        if (lblConnectedNotes != null) lblConnectedNotes.setText(String.valueOf(stats.getConnectedNotes()));
        if (lblIsolatedNotes != null) lblIsolatedNotes.setText(String.valueOf(stats.getIsolatedNotes()));

        if (lblGraphRatio != null) {
            int total = stats.getTotalNotes();
            if (total == 0) {
                lblGraphRatio.setText("No notes in knowledge base yet.");
            } else {
                int percent = stats.getConnectedPercent();
                lblGraphRatio.setText(stats.getConnectedNotes() + " connected (" + percent + "%) • " +
                        stats.getIsolatedNotes() + " isolated (" + (100 - percent) + "%)");
            }
        }
    }

    /**
     * Populates recent learning activity items.
     */
    private void updateRecentActivity(DashboardStats stats) {
        if (boxRecentActivity == null) return;
        boxRecentActivity.getChildren().clear();

        List<TimelineEvent> activities = stats.getRecentActivity();
        boolean hasActivity = !activities.isEmpty();

        if (boxEmptyActivity != null) {
            boxEmptyActivity.setVisible(!hasActivity);
            boxEmptyActivity.setManaged(!hasActivity);
        }

        if (!hasActivity) {
            return;
        }

        for (TimelineEvent ev : activities) {
            boxRecentActivity.getChildren().add(createActivityRow(ev));
        }
    }

    private HBox createActivityRow(TimelineEvent ev) {
        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("dashboard-list-row");

        // Event type badge
        Label badge = new Label();
        badge.getStyleClass().add("timeline-type-badge");
        badge.setMinWidth(Region.USE_PREF_SIZE);

        String type = ev.getEventType() != null ? ev.getEventType().toUpperCase() : "EVENT";
        switch (type) {
            case "NOTE_REVIEWED" -> {
                badge.setText("REVIEWED");
                badge.getStyleClass().add("timeline-badge-reviewed");
            }
            case "NOTE_CREATED" -> {
                badge.setText("CREATED");
                badge.getStyleClass().add("timeline-badge-created");
            }
            case "NOTE_UPDATED" -> {
                badge.setText("UPDATED");
                badge.getStyleClass().add("timeline-badge-updated");
            }
            default -> {
                badge.setText(type.replace("NOTE_", ""));
                badge.getStyleClass().add("timeline-badge-default");
            }
        }

        // Title / Description
        VBox textBox = new VBox(2);
        textBox.setMinWidth(60);
        HBox.setHgrow(textBox, Priority.ALWAYS);

        String titleText = ev.hasNote() ? ev.getNoteTitle() : ev.getDescription();
        Label lblTitle = new Label(titleText != null ? titleText : "Milestone");
        lblTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: #0f172a;");
        lblTitle.setMaxWidth(Double.MAX_VALUE);

        Label lblDesc = new Label(ev.getDescription());
        lblDesc.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b;");
        lblDesc.setMaxWidth(Double.MAX_VALUE);

        textBox.getChildren().addAll(lblTitle, lblDesc);

        // Timestamp
        Label lblTime = new Label(ev.getFormattedDate() + " " + ev.getFormattedTime());
        lblTime.setStyle("-fx-font-size: 10px; -fx-text-fill: #94a3b8;");
        lblTime.setMinWidth(Region.USE_PREF_SIZE);

        row.getChildren().addAll(badge, textBox, lblTime);

        // Click to view note if present
        if (ev.hasNote()) {
            row.setStyle("-fx-cursor: hand;");
            row.setOnMouseClicked(e -> handleViewNote(ev.getNote()));
        }

        return row;
    }

    /**
     * Populates recently updated notes items.
     */

    private void populateProductivitySection() {
        javafx.application.Platform.runLater(() -> {
            try {
                // 1. Study Next
                StudyRecommendationService recService = new StudyRecommendationService();
                List<Recommendation> recs = recService.getStudyNextRecommendations(3);
                
                if (boxStudyNext != null) boxStudyNext.getChildren().clear();
                if (recs.isEmpty()) {
                    if (lblEmptyStudy != null) {
                        lblEmptyStudy.setVisible(true);
                        lblEmptyStudy.setManaged(true);
                    }
                } else {
                    if (lblEmptyStudy != null) {
                        lblEmptyStudy.setVisible(false);
                        lblEmptyStudy.setManaged(false);
                    }
                    if (boxStudyNext != null) {
                        for (Recommendation r : recs) {
                            boxStudyNext.getChildren().add(createMiniNoteCard(r.getNote(), r.getReason()));
                        }
                    }
                }

                // 2. Favorites
                com.mindmap.service.NoteService noteService = new com.mindmap.service.NoteService();
                List<Note> favs = noteService.getFavorites();
                
                if (boxFavorites != null) boxFavorites.getChildren().clear();
                if (favs.isEmpty()) {
                    if (lblEmptyFavorites != null) {
                        lblEmptyFavorites.setVisible(true);
                        lblEmptyFavorites.setManaged(true);
                    }
                } else {
                    if (lblEmptyFavorites != null) {
                        lblEmptyFavorites.setVisible(false);
                        lblEmptyFavorites.setManaged(false);
                    }
                    if (boxFavorites != null) {
                        for (Note f : favs) {
                            boxFavorites.getChildren().add(createMiniNoteCard(f, f.getSubject()));
                        }
                    }
                }

                // 3. Recently Viewed
                List<Note> recentViews = noteService.getRecentlyViewed(3);
                
                if (boxRecentlyViewed != null) boxRecentlyViewed.getChildren().clear();
                if (recentViews.isEmpty()) {
                    if (lblEmptyRecent != null) {
                        lblEmptyRecent.setVisible(true);
                        lblEmptyRecent.setManaged(true);
                    }
                } else {
                    if (lblEmptyRecent != null) {
                        lblEmptyRecent.setVisible(false);
                        lblEmptyRecent.setManaged(false);
                    }
                    if (boxRecentlyViewed != null) {
                        for (Note rv : recentViews) {
                            boxRecentlyViewed.getChildren().add(createMiniNoteCard(rv, "Recently viewed"));
                        }
                    }
                }
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Failed to populate productivity section", e);
            }
        });
    }

    private javafx.scene.layout.HBox createMiniNoteCard(Note note, String subtext) {
        javafx.scene.layout.HBox box = new javafx.scene.layout.HBox();
        box.setSpacing(8);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setStyle("-fx-padding: 8px 12px; -fx-background-color: #f8fafc; -fx-background-radius: 6px; -fx-cursor: hand;");
        
        javafx.scene.layout.VBox v = new javafx.scene.layout.VBox();
        v.setSpacing(2);
        
        Label title = new Label(note.getTitle());
        title.setStyle("-fx-font-weight: bold; -fx-text-fill: #1e293b;");
        
        Label sub = new Label(subtext != null ? subtext : "Note");
        sub.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b;");
        
        v.getChildren().addAll(title, sub);
        box.getChildren().add(v);
        
        box.setOnMouseClicked(e -> handleViewNote(note));
        
        // Hover effect
        box.setOnMouseEntered(e -> box.setStyle("-fx-padding: 8px 12px; -fx-background-color: #f1f5f9; -fx-background-radius: 6px; -fx-cursor: hand;"));
        box.setOnMouseExited(e -> box.setStyle("-fx-padding: 8px 12px; -fx-background-color: #f8fafc; -fx-background-radius: 6px; -fx-cursor: hand;"));
        
        return box;
    }

    private void updateRecentNotes(DashboardStats stats) {
        if (boxRecentNotes == null) return;
        boxRecentNotes.getChildren().clear();

        List<Note> notes = stats.getRecentNotes();
        boolean hasNotes = !notes.isEmpty();

        if (boxEmptyNotes != null) {
            boxEmptyNotes.setVisible(!hasNotes);
            boxEmptyNotes.setManaged(!hasNotes);
        }

        if (!hasNotes) {
            return;
        }

        for (Note note : notes) {
            boxRecentNotes.getChildren().add(createRecentNoteRow(note));
        }
    }

    private HBox createRecentNoteRow(Note note) {
        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("dashboard-list-row");

        // Title and Subject
        VBox textBox = new VBox(2);
        textBox.setMinWidth(60);
        HBox.setHgrow(textBox, Priority.ALWAYS);

        Label lblTitle = new Label(note.getTitle() != null ? note.getTitle() : "Untitled Note");
        lblTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: #0f172a;");
        lblTitle.setMaxWidth(Double.MAX_VALUE);

        HBox metaBox = new HBox(6);
        metaBox.setAlignment(Pos.CENTER_LEFT);

        if (note.getSubject() != null && !note.getSubject().isBlank()) {
            Label lblSubj = new Label(note.getSubject().trim());
            lblSubj.getStyleClass().add("badge-subject");
            lblSubj.setStyle("-fx-font-size: 9px; -fx-padding: 1 5;");
            metaBox.getChildren().add(lblSubj);
        }

        String diff = note.getDifficulty() != null ? note.getDifficulty().toUpperCase() : Difficulty.MEDIUM.name();
        Label lblDiff = new Label(diff);
        lblDiff.setStyle("-fx-font-size: 9px; -fx-padding: 1 5;");
        switch (diff) {
            case "EASY" -> lblDiff.getStyleClass().add("badge-easy");
            case "HARD" -> lblDiff.getStyleClass().add("badge-hard");
            default -> lblDiff.getStyleClass().add("badge-medium");
        }
        metaBox.getChildren().add(lblDiff);

        if (note.getUpdatedAt() != null) {
            Label lblDate = new Label(note.getUpdatedAt().format(DATE_FORMATTER));
            lblDate.setStyle("-fx-font-size: 10px; -fx-text-fill: #94a3b8;");
            metaBox.getChildren().add(lblDate);
        }

        textBox.getChildren().addAll(lblTitle, metaBox);

        // Actions
        HBox actionBox = new HBox(4);
        actionBox.setAlignment(Pos.CENTER_RIGHT);
        actionBox.setMinWidth(Region.USE_PREF_SIZE);

        Button btnView = new Button("View");
        btnView.getStyleClass().addAll("timeline-action-btn", "btn-secondary");
        btnView.setStyle("-fx-font-size: 10px; -fx-padding: 2 8;");
        btnView.setOnAction(e -> handleViewNote(note));

        Button btnEdit = new Button("Edit");
        btnEdit.getStyleClass().addAll("timeline-action-btn", "btn-secondary");
        btnEdit.setStyle("-fx-font-size: 10px; -fx-padding: 2 8;");
        btnEdit.setOnAction(e -> handleEditNote(note));

        actionBox.getChildren().addAll(btnView, btnEdit);

        row.getChildren().addAll(textBox, actionBox);
        return row;
    }

    // =========================================================================
    // Action Handlers
    // =========================================================================

    @FXML
    public void handleNewNote() {

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/note_editor.fxml"));
            Parent root = loader.load();

            NoteEditorController controller = loader.getController();
            controller.setNoteService(noteService);

            Stage stage = new Stage();
            stage.setTitle("Create Note");
            stage.initModality(Modality.APPLICATION_MODAL);
            if (lblTotalNotes != null && lblTotalNotes.getScene() != null && lblTotalNotes.getScene().getWindow() != null) {
                stage.initOwner(lblTotalNotes.getScene().getWindow());
            }
            stage.setScene(new Scene(root));
            controller.setDialogStage(stage);
            controller.setNote(new Note(), NoteEditorMode.CREATE);

            stage.showAndWait();

            if (controller.isSaved()) {
                loadDashboardData();
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to open Note Editor dialog: " + e.getMessage(), e);
            UiUtils.showError("Error", "Failed to open Note Editor dialog: " + e.getMessage());
        }
    }

    @FXML
    public void handleStartReview() {
        MainController main = MainController.getInstance();
        if (main != null) {
            main.showRevision();
        }
    }

    @FXML
    public void handleOpenMindMap() {
        MainController main = MainController.getInstance();
        if (main != null) {
            main.showMindMap();
        }
    }

    @FXML
    public void handleOpenTimeline() {
        MainController main = MainController.getInstance();
        if (main != null) {
            main.showTimeline();
        }
    }

    @FXML
    public void handleOpenNotes() {
        MainController main = MainController.getInstance();
        if (main != null) {
            main.showNotes();
        }
    }

    @FXML
    public void handleRefresh() {
        loadDashboardData();
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
            if (lblTotalNotes != null && lblTotalNotes.getScene() != null && lblTotalNotes.getScene().getWindow() != null) {
                stage.initOwner(lblTotalNotes.getScene().getWindow());
            }
            stage.setScene(new Scene(root));
            controller.setDialogStage(stage);
            controller.setNote(note);
            stage.showAndWait();
            loadDashboardData();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Could not open note viewer: " + e.getMessage(), e);
            UiUtils.showError("Error", "Could not open note viewer: " + e.getMessage());
        }
    }

    private void handleEditNote(Note note) {
        if (note == null) return;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/note_editor.fxml"));
            Parent root = loader.load();

            NoteEditorController controller = loader.getController();
            controller.setNoteService(noteService);

            Stage stage = new Stage();
            stage.setTitle("Edit Note - " + note.getTitle());
            stage.initModality(Modality.APPLICATION_MODAL);
            if (lblTotalNotes != null && lblTotalNotes.getScene() != null && lblTotalNotes.getScene().getWindow() != null) {
                stage.initOwner(lblTotalNotes.getScene().getWindow());
            }
            stage.setScene(new Scene(root));
            controller.setDialogStage(stage);

            Note targetNote = noteService.getNoteWithTags(note.getId()).orElse(note);
            controller.setNote(targetNote, NoteEditorMode.EDIT);

            stage.showAndWait();

            if (controller.isSaved()) {
                loadDashboardData();
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Could not open note editor: " + e.getMessage(), e);
            UiUtils.showError("Error", "Could not open note editor: " + e.getMessage());
        }
    }
}
