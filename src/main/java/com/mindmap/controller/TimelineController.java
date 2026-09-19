package com.mindmap.controller;

import com.mindmap.model.Difficulty;
import com.mindmap.model.LearningEventType;
import com.mindmap.model.Note;
import com.mindmap.model.TimelineEvent;
import com.mindmap.model.TimelineRow;
import com.mindmap.model.TimelineRow.TimelineEventRow;
import com.mindmap.model.TimelineRow.TimelineHeaderRow;
import com.mindmap.service.TimelineService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for the Learning Timeline screen.
 * Displays a chronological vertical timeline of user activity with date grouping,
 * multi-criteria filtering, search, and note interactions.
 * Uses virtualized ListView with cell reuse for high performance with 1000+ events.
 */
public class TimelineController {

    private static final Logger LOGGER = Logger.getLogger(TimelineController.class.getName());

    @FXML private Label lblStatTotal;
    @FXML private Label lblStatReviews;
    @FXML private Label lblStatNoteActivity;
    @FXML private Label lblFilterStatus;

    @FXML private TextField txtSearch;
    @FXML private ComboBox<String> cmbEventType;
    @FXML private ComboBox<String> cmbDateRange;
    @FXML private HBox boxCustomDates;
    @FXML private DatePicker dpFrom;
    @FXML private DatePicker dpTo;
    @FXML private Button btnApply;
    @FXML private Button btnReset;
    @FXML private Button btnRefresh;

    @FXML private ListView<TimelineRow> listTimeline;
    @FXML private VBox boxEmptyState;

    private final ObservableList<TimelineRow> timelineData = FXCollections.observableArrayList();
    private TimelineService timelineService = new TimelineService();

    public void setTimelineService(TimelineService timelineService) {
        if (timelineService != null) {
            this.timelineService = timelineService;
        }
    }

    public ObservableList<TimelineRow> getTimelineData() {
        return timelineData;
    }

    public ListView<TimelineRow> getListTimeline() {
        return listTimeline;
    }

    @FXML
    public void initialize() {
        setupFilterControls();
        setupListView();
        loadTimelineData();
    }

    private void setupFilterControls() {
        if (cmbEventType != null) {
            cmbEventType.setItems(FXCollections.observableArrayList(
                    "All Events",
                    "Reviewed Notes",
                    "Created Notes",
                    "Updated Notes"
            ));
            cmbEventType.getSelectionModel().selectFirst();
            cmbEventType.valueProperty().addListener((obs, oldVal, newVal) -> handleApplyFilters());
        }

        if (cmbDateRange != null) {
            cmbDateRange.setItems(FXCollections.observableArrayList(
                    "All Dates",
                    "Today",
                    "Yesterday",
                    "Last 7 Days",
                    "Last 30 Days",
                    "Custom Range"
            ));
            cmbDateRange.getSelectionModel().selectFirst();
            cmbDateRange.valueProperty().addListener((obs, oldVal, newVal) -> {
                boolean isCustom = "Custom Range".equals(newVal);
                if (boxCustomDates != null) {
                    boxCustomDates.setVisible(isCustom);
                    boxCustomDates.setManaged(isCustom);
                }
                if (!isCustom) {
                    handleApplyFilters();
                }
            });
        }

        if (txtSearch != null) {
            txtSearch.setOnAction(e -> handleApplyFilters());
        }
    }

    private void setupListView() {
        if (listTimeline != null) {
            listTimeline.setItems(timelineData);
            listTimeline.setFocusTraversable(false);
            listTimeline.setCellFactory(lv -> new TimelineListCell());
        }
    }

    /**
     * Loads timeline events from database, updates statistics, groups by date,
     * and refreshes the virtualized ListView.
     */
    public void loadTimelineData() {
        handleApplyFilters();
    }

    @FXML
    public void handleApplyFilters() {
        String eventTypeFilter = resolveEventTypeFilter();
        LocalDate[] dates = resolveDateRange();
        LocalDate fromDate = dates[0];
        LocalDate toDate = dates[1];
        String query = (txtSearch != null && txtSearch.getText() != null) ? txtSearch.getText().trim() : null;

        List<TimelineEvent> events = timelineService.getFilteredTimelineEvents(eventTypeFilter, fromDate, toDate, query);
        List<TimelineRow> groupedRows = timelineService.buildGroupedTimelineRows(events);

        timelineData.setAll(groupedRows);

        // Update statistics metrics
        updateMetrics();

        // Update empty state
        boolean isEmpty = groupedRows.isEmpty();
        if (boxEmptyState != null) {
            boxEmptyState.setVisible(isEmpty);
            boxEmptyState.setManaged(isEmpty);
        }
        if (listTimeline != null) {
            listTimeline.setVisible(!isEmpty);
            listTimeline.setManaged(!isEmpty);
        }

        // Update status label
        if (lblFilterStatus != null) {
            lblFilterStatus.setText(events.size() + " " + (events.size() == 1 ? "event" : "events") + " found");
        }
    }

    @FXML
    public void handleResetFilters() {
        if (txtSearch != null) txtSearch.clear();
        if (cmbEventType != null) cmbEventType.getSelectionModel().selectFirst();
        if (cmbDateRange != null) cmbDateRange.getSelectionModel().selectFirst();
        if (dpFrom != null) dpFrom.setValue(null);
        if (dpTo != null) dpTo.setValue(null);
        if (boxCustomDates != null) {
            boxCustomDates.setVisible(false);
            boxCustomDates.setManaged(false);
        }
        handleApplyFilters();
    }

    @FXML
    public void handleRefresh() {
        loadTimelineData();
    }

    private void updateMetrics() {
        int total = timelineService.getTotalEventCount();
        int reviews = timelineService.getReviewedEventCount();
        int noteActions = timelineService.getNoteActivityCount();

        if (lblStatTotal != null) lblStatTotal.setText(String.valueOf(total));
        if (lblStatReviews != null) lblStatReviews.setText(String.valueOf(reviews));
        if (lblStatNoteActivity != null) lblStatNoteActivity.setText(String.valueOf(noteActions));
    }

    private String resolveEventTypeFilter() {
        if (cmbEventType == null) return null;
        String selected = cmbEventType.getValue();
        if (selected == null || "All Events".equals(selected)) return null;

        return switch (selected) {
            case "Reviewed Notes" -> LearningEventType.NOTE_REVIEWED.name();
            case "Created Notes" -> LearningEventType.NOTE_CREATED.name();
            case "Updated Notes" -> LearningEventType.NOTE_UPDATED.name();
            default -> null;
        };
    }

    private LocalDate[] resolveDateRange() {
        if (cmbDateRange == null) return new LocalDate[]{null, null};
        String selected = cmbDateRange.getValue();
        LocalDate now = LocalDate.now();

        if ("Today".equals(selected)) {
            return new LocalDate[]{now, now};
        } else if ("Yesterday".equals(selected)) {
            LocalDate yest = now.minusDays(1);
            return new LocalDate[]{yest, yest};
        } else if ("Last 7 Days".equals(selected)) {
            return new LocalDate[]{now.minusDays(7), now};
        } else if ("Last 30 Days".equals(selected)) {
            return new LocalDate[]{now.minusDays(30), now};
        } else if ("Custom Range".equals(selected)) {
            LocalDate from = dpFrom != null ? dpFrom.getValue() : null;
            LocalDate to = dpTo != null ? dpTo.getValue() : null;
            return new LocalDate[]{from, to};
        }
        return new LocalDate[]{null, null};
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
            if (listTimeline != null && listTimeline.getScene() != null && listTimeline.getScene().getWindow() != null) {
                stage.initOwner(listTimeline.getScene().getWindow());
            }
            stage.setScene(new Scene(root));
            controller.setDialogStage(stage);
            controller.setNote(note);
            stage.showAndWait();
            loadTimelineData();
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

    // =========================================================================
    // Virtualized ListCell: Polymorphic (Header / Event)
    // =========================================================================

    public class TimelineListCell extends ListCell<TimelineRow> {
        // Root container for header row
        private final HBox headerBox;
        private final Label lblHeaderTitle;
        private final Label lblHeaderSubtitle;
        private final Label lblHeaderCount;

        // Root container for event row
        private final HBox eventBox;
        private final VBox trackColumn;
        private final Region trackTopLine;
        private final StackPane dotNode;
        private final Label lblDotIcon;
        private final Region trackBottomLine;

        private final VBox card;
        private final Label lblEventType;
        private final Label lblTime;
        private final Label lblTitle;
        private final Label lblSubject;
        private final Label lblDifficulty;
        private final Label lblDesc;
        private final HBox actionRow;
        private final Button btnView;
        private final Button btnMindMap;

        public TimelineListCell() {
            // -------------------------------------------------------------
            // 1. Build Header Row UI (instantiated once)
            // -------------------------------------------------------------
            headerBox = new HBox(10);
            headerBox.setAlignment(Pos.CENTER_LEFT);
            headerBox.getStyleClass().add("timeline-header-container");
            headerBox.setPadding(new Insets(16, 4, 6, 8));

            Label lblCalendarIcon = new Label("📅");
            lblCalendarIcon.setStyle("-fx-font-size: 13px;");

            lblHeaderTitle = new Label();
            lblHeaderTitle.getStyleClass().add("timeline-header-title");

            lblHeaderSubtitle = new Label();
            lblHeaderSubtitle.getStyleClass().add("timeline-header-subtitle");

            Region separator = new Region();
            HBox.setHgrow(separator, Priority.ALWAYS);
            separator.setStyle("-fx-border-color: #e2e8f0; -fx-border-width: 0 0 1 0;");

            lblHeaderCount = new Label();
            lblHeaderCount.getStyleClass().add("timeline-header-count");

            headerBox.getChildren().addAll(lblCalendarIcon, lblHeaderTitle, lblHeaderSubtitle, separator, lblHeaderCount);

            // -------------------------------------------------------------
            // 2. Build Event Row UI (instantiated once)
            // -------------------------------------------------------------
            eventBox = new HBox(12);
            eventBox.setAlignment(Pos.TOP_LEFT);
            eventBox.setPadding(new Insets(3, 4, 3, 4));

            // Track column: top connector, node dot, bottom connector
            trackColumn = new VBox(0);
            trackColumn.setAlignment(Pos.TOP_CENTER);
            trackColumn.setPrefWidth(28);
            trackColumn.setMinWidth(28);
            trackColumn.setMaxWidth(28);

            trackTopLine = new Region();
            trackTopLine.setPrefWidth(2);
            trackTopLine.setMinWidth(2);
            trackTopLine.setMaxWidth(2);
            trackTopLine.setPrefHeight(8);
            trackTopLine.getStyleClass().add("timeline-connector-line");

            dotNode = new StackPane();
            dotNode.setPrefSize(22, 22);
            dotNode.setMinSize(22, 22);
            dotNode.setMaxSize(22, 22);
            dotNode.getStyleClass().add("timeline-dot");

            lblDotIcon = new Label();
            lblDotIcon.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #ffffff;");
            dotNode.getChildren().add(lblDotIcon);

            trackBottomLine = new Region();
            trackBottomLine.setPrefWidth(2);
            trackBottomLine.setMinWidth(2);
            trackBottomLine.setMaxWidth(2);
            VBox.setVgrow(trackBottomLine, Priority.ALWAYS);
            trackBottomLine.getStyleClass().add("timeline-connector-line");

            trackColumn.getChildren().addAll(trackTopLine, dotNode, trackBottomLine);

            // Event Card
            card = new VBox(6);
            card.getStyleClass().add("timeline-event-card");
            HBox.setHgrow(card, Priority.ALWAYS);

            // Row 1: Type badge, time, note title, subject, difficulty
            HBox topRow = new HBox(8);
            topRow.setAlignment(Pos.CENTER_LEFT);

            lblEventType = new Label();
            lblEventType.getStyleClass().add("timeline-type-badge");

            lblTime = new Label();
            lblTime.getStyleClass().add("timeline-time-label");

            lblTitle = new Label();
            lblTitle.getStyleClass().add("timeline-event-title");
            lblTitle.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(lblTitle, Priority.ALWAYS);

            lblSubject = new Label();
            lblSubject.getStyleClass().add("badge-subject");
            lblSubject.setStyle("-fx-font-size: 10px; -fx-padding: 1 6;");

            lblDifficulty = new Label();
            lblDifficulty.setStyle("-fx-font-size: 10px; -fx-padding: 1 6;");

            topRow.getChildren().addAll(lblEventType, lblTime, lblTitle, lblSubject, lblDifficulty);

            // Row 2: Description
            lblDesc = new Label();
            lblDesc.getStyleClass().add("timeline-event-desc");
            lblDesc.setMaxWidth(Double.MAX_VALUE);

            // Row 3: Actions
            actionRow = new HBox(8);
            actionRow.setAlignment(Pos.CENTER_RIGHT);

            btnView = new Button("👁 View Note");
            btnView.getStyleClass().addAll("timeline-action-btn", "btn-secondary");

            btnMindMap = new Button("🌐 Mind Map");
            btnMindMap.getStyleClass().addAll("timeline-action-btn", "btn-secondary");

            actionRow.getChildren().addAll(btnView, btnMindMap);

            card.getChildren().addAll(topRow, lblDesc, actionRow);
            eventBox.getChildren().addAll(trackColumn, card);

            setGraphic(null);
            setText(null);
            setPrefWidth(0);
        }

        @Override
        protected void updateItem(TimelineRow item, boolean empty) {
            super.updateItem(item, empty);

            if (empty || item == null) {
                setGraphic(null);
                setText(null);
                return;
            }

            if (item instanceof TimelineHeaderRow headerRow) {
                // Configure header
                lblHeaderTitle.setText(headerRow.getTitle());
                lblHeaderSubtitle.setText(headerRow.getSubtitle());
                lblHeaderCount.setText(headerRow.getEventCount() + " " +
                        (headerRow.getEventCount() == 1 ? "event" : "events"));
                setGraphic(headerBox);

            } else if (item instanceof TimelineEventRow eventRow) {
                TimelineEvent ev = eventRow.getEvent();
                Note note = ev.getNote();

                // 1. Configure connector track
                trackTopLine.setVisible(!eventRow.isFirstInSection());
                trackBottomLine.setVisible(!eventRow.isLastInSection());

                // 2. Configure dot node based on event type
                String type = ev.getEventType() != null ? ev.getEventType().toUpperCase() : "EVENT";
                dotNode.getStyleClass().removeAll("timeline-dot-reviewed", "timeline-dot-created", "timeline-dot-updated", "timeline-dot-default");
                lblEventType.getStyleClass().removeAll("timeline-badge-reviewed", "timeline-badge-created", "timeline-badge-updated", "timeline-badge-default");

                switch (type) {
                    case "NOTE_REVIEWED" -> {
                        dotNode.getStyleClass().add("timeline-dot-reviewed");
                        lblDotIcon.setText("✓");
                        lblEventType.setText("REVIEWED");
                        lblEventType.getStyleClass().add("timeline-badge-reviewed");
                    }
                    case "NOTE_CREATED" -> {
                        dotNode.getStyleClass().add("timeline-dot-created");
                        lblDotIcon.setText("✦");
                        lblEventType.setText("CREATED");
                        lblEventType.getStyleClass().add("timeline-badge-created");
                    }
                    case "NOTE_UPDATED" -> {
                        dotNode.getStyleClass().add("timeline-dot-updated");
                        lblDotIcon.setText("✎");
                        lblEventType.setText("UPDATED");
                        lblEventType.getStyleClass().add("timeline-badge-updated");
                    }
                    default -> {
                        dotNode.getStyleClass().add("timeline-dot-default");
                        lblDotIcon.setText("★");
                        lblEventType.setText(type.replace("NOTE_", ""));
                        lblEventType.getStyleClass().add("timeline-badge-default");
                    }
                }

                // 3. Time label
                lblTime.setText(ev.getFormattedTime());

                // 4. Note Title
                if (ev.hasNote()) {
                    String title = note.getTitle() != null ? note.getTitle() : "Untitled Note";
                    lblTitle.setText(title);
                    lblTitle.setTooltip(new Tooltip(title));
                    lblTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #0f172a;");
                } else {
                    lblTitle.setText("(Milestone / Unlinked Note)");
                    lblTitle.setTooltip(null);
                    lblTitle.setStyle("-fx-font-style: italic; -fx-text-fill: #94a3b8;");
                }

                // 5. Subject badge
                boolean hasSubject = (note != null && note.getSubject() != null && !note.getSubject().isBlank());
                lblSubject.setVisible(hasSubject);
                lblSubject.setManaged(hasSubject);
                if (hasSubject) {
                    lblSubject.setText(note.getSubject().trim());
                }

                // 6. Difficulty badge
                boolean hasDiff = (note != null && note.getDifficulty() != null && !note.getDifficulty().isBlank());
                lblDifficulty.setVisible(hasDiff);
                lblDifficulty.setManaged(hasDiff);
                if (hasDiff) {
                    String diff = note.getDifficulty().toUpperCase();
                    lblDifficulty.setText(diff);
                    lblDifficulty.getStyleClass().removeAll("badge-easy", "badge-medium", "badge-hard");
                    switch (diff) {
                        case "EASY" -> lblDifficulty.getStyleClass().add("badge-easy");
                        case "HARD" -> lblDifficulty.getStyleClass().add("badge-hard");
                        default -> lblDifficulty.getStyleClass().add("badge-medium");
                    }
                }

                // 7. Description
                String desc = ev.getDescription() != null ? ev.getDescription() : "";
                lblDesc.setText(desc);
                lblDesc.setTooltip(new Tooltip(desc));

                // 8. Actions
                boolean hasActions = ev.hasNote();
                actionRow.setVisible(hasActions);
                actionRow.setManaged(hasActions);
                if (hasActions) {
                    btnView.setOnAction(e -> handleViewNote(note));
                    btnMindMap.setOnAction(e -> handleOpenInMindMap(note));
                }

                setGraphic(eventBox);
            }
        }
    }
}
