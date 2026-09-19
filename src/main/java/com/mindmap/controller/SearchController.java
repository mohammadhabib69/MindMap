package com.mindmap.controller;

import com.mindmap.model.ConnectionFilterPreset;
import com.mindmap.model.DateFilterPreset;
import com.mindmap.model.Note;
import com.mindmap.model.SearchCriteria;
import com.mindmap.service.NoteService;
import com.mindmap.service.SearchService;
import com.mindmap.util.AnimationUtil;
import com.mindmap.util.DateUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for the Advanced Search & Filtering screen.
 * Handles multi-criteria note searching, real-time reactive filtering,
 * result table presentation, view/edit modals, and Mind Map navigation.
 */
public class SearchController {

    private static final Logger LOGGER = Logger.getLogger(SearchController.class.getName());

    // Search Bar
    @FXML private TextField txtSearchQuery;
    @FXML private Button btnExecuteSearch;
    @FXML private Button btnClearFilters;

    // Filter Controls
    @FXML private ComboBox<String> cmbSubjectFilter;
    @FXML private ComboBox<String> cmbDifficultyFilter;
    @FXML private ComboBox<String> cmbTagFilter;
    @FXML private ComboBox<DateFilterPreset> cmbDateFilter;
    @FXML private ComboBox<ConnectionFilterPreset> cmbConnectionFilter;

    // Custom Date Range
    @FXML private HBox boxCustomDateRange;
    @FXML private DatePicker dpStartDate;
    @FXML private DatePicker dpEndDate;
    @FXML private Button btnApplyCustomDate;

    // Results Header & Action Bar
    @FXML private Label lblResultCount;
    @FXML private Button btnViewNote;
    @FXML private Button btnEditNote;
    @FXML private Button btnOpenInMindMap;

    // Table & Columns
    @FXML private TableView<Note> tableResults;
    @FXML private TableColumn<Note, String> colTitle;
    @FXML private TableColumn<Note, String> colSubject;
    @FXML private TableColumn<Note, String> colDifficulty;
    @FXML private TableColumn<Note, String> colTags;
    @FXML private TableColumn<Note, String> colConnections;
    @FXML private TableColumn<Note, String> colUpdated;

    // Placeholder
    @FXML private Label lblPlaceholderTitle;
    @FXML private Label lblPlaceholderSubtitle;

    // Services & State
    private final SearchService searchService;
    private final NoteService noteService;
    private final ObservableList<Note> resultsObservableList = FXCollections.observableArrayList();
    private boolean isUpdatingFilters = false;

    public SearchController() {
        this(new SearchService(), new NoteService());
    }

    public SearchController(SearchService searchService, NoteService noteService) {
        this.searchService = searchService;
        this.noteService = noteService;
    }

    @FXML
    public void initialize() {
        configureColumns();
        configureTableSelection();
        configureRowDoubleClicks();
        setupButtonAnimations();
        populateFilterDropdowns();
        setupFilterListeners();

        // Perform initial search to display all notes
        handleSearch();
    }

    private void setupButtonAnimations() {
        AnimationUtil.addButtonHoverEffect(btnExecuteSearch);
        AnimationUtil.addButtonHoverEffect(btnClearFilters);
        AnimationUtil.addButtonHoverEffect(btnViewNote);
        AnimationUtil.addButtonHoverEffect(btnEditNote);
        AnimationUtil.addButtonHoverEffect(btnOpenInMindMap);
        AnimationUtil.addButtonHoverEffect(btnApplyCustomDate);
    }

    private void configureColumns() {
        colTitle.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getTitle()));

        colSubject.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getSubject() != null ? cellData.getValue().getSubject() : ""));

        // Styled Difficulty Badge Column
        colDifficulty.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getDifficulty() != null ? cellData.getValue().getDifficulty() : ""));
        colDifficulty.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item.trim().isEmpty()) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Label badge = new Label(item.toUpperCase());
                    badge.getStyleClass().add("badge-difficulty");
                    String diff = item.trim().toUpperCase();
                    if ("EASY".equals(diff)) {
                        badge.getStyleClass().add("badge-easy");
                    } else if ("MEDIUM".equals(diff)) {
                        badge.getStyleClass().add("badge-medium");
                    } else if ("HARD".equals(diff)) {
                        badge.getStyleClass().add("badge-hard");
                    }
                    HBox box = new HBox(badge);
                    box.setAlignment(Pos.CENTER_LEFT);
                    setGraphic(box);
                    setText(null);
                }
            }
        });

        colTags.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getTagsString()));

        // Connections Column
        colConnections.setCellValueFactory(cellData -> {
            int count = cellData.getValue().getConnectionCount();
            String text = (count == 0) ? "Isolated (0)" : (count == 1 ? "1 connection" : count + " connections");
            return new SimpleStringProperty(text);
        });

        colUpdated.setCellValueFactory(cellData ->
                new SimpleStringProperty(DateUtil.formatDisplay(cellData.getValue().getUpdatedAt())));

        tableResults.setItems(resultsObservableList);
    }

    private void configureTableSelection() {
        tableResults.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            boolean hasSelection = newSel != null;
            if (btnViewNote != null) btnViewNote.setDisable(!hasSelection);
            if (btnEditNote != null) btnEditNote.setDisable(!hasSelection);
            if (btnOpenInMindMap != null) btnOpenInMindMap.setDisable(!hasSelection);
        });
    }

    private void configureRowDoubleClicks() {
        tableResults.setRowFactory(tv -> {
            TableRow<Note> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    handleViewNote();
                }
            });
            return row;
        });
    }

    private void populateFilterDropdowns() {
        isUpdatingFilters = true;
        try {
            // Subjects
            List<String> subjects = searchService.getAvailableSubjects();
            cmbSubjectFilter.setItems(FXCollections.observableArrayList(subjects));
            cmbSubjectFilter.setValue(SearchCriteria.ALL_SUBJECTS);

            // Difficulties
            List<String> difficulties = searchService.getAvailableDifficulties();
            cmbDifficultyFilter.setItems(FXCollections.observableArrayList(difficulties));
            cmbDifficultyFilter.setValue(SearchCriteria.ALL_DIFFICULTIES);

            // Tags
            List<String> tags = searchService.getAvailableTags();
            cmbTagFilter.setItems(FXCollections.observableArrayList(tags));
            cmbTagFilter.setValue(SearchCriteria.ALL_TAGS);

            // Date Ranges
            cmbDateFilter.setItems(FXCollections.observableArrayList(searchService.getDateFilterPresets()));
            cmbDateFilter.setValue(DateFilterPreset.ALL_TIME);

            // Connections
            cmbConnectionFilter.setItems(FXCollections.observableArrayList(searchService.getConnectionFilterPresets()));
            cmbConnectionFilter.setValue(ConnectionFilterPreset.ALL);
        } finally {
            isUpdatingFilters = false;
        }
    }

    private void setupFilterListeners() {
        // Text field live search
        if (txtSearchQuery != null) {
            txtSearchQuery.textProperty().addListener((obs, oldVal, newVal) -> {
                if (!isUpdatingFilters) {
                    handleSearch();
                }
            });
        }

        // Dropdown listeners
        cmbSubjectFilter.valueProperty().addListener((obs, oldVal, newVal) -> onFilterChanged());
        cmbDifficultyFilter.valueProperty().addListener((obs, oldVal, newVal) -> onFilterChanged());
        cmbTagFilter.valueProperty().addListener((obs, oldVal, newVal) -> onFilterChanged());
        cmbConnectionFilter.valueProperty().addListener((obs, oldVal, newVal) -> onFilterChanged());

        // Date dropdown listener
        cmbDateFilter.valueProperty().addListener((obs, oldVal, newVal) -> {
            boolean isCustom = newVal == DateFilterPreset.CUSTOM;
            if (boxCustomDateRange != null) {
                boxCustomDateRange.setVisible(isCustom);
                boxCustomDateRange.setManaged(isCustom);
            }
            onFilterChanged();
        });

        // Date picker listeners
        if (dpStartDate != null) {
            dpStartDate.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (cmbDateFilter.getValue() == DateFilterPreset.CUSTOM) {
                    onFilterChanged();
                }
            });
        }
        if (dpEndDate != null) {
            dpEndDate.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (cmbDateFilter.getValue() == DateFilterPreset.CUSTOM) {
                    onFilterChanged();
                }
            });
        }
    }

    private void onFilterChanged() {
        if (!isUpdatingFilters) {
            handleSearch();
        }
    }

    @FXML
    public void handleSearch() {
        SearchCriteria criteria = buildSearchCriteria();
        List<Note> results = searchService.searchNotes(criteria);
        resultsObservableList.setAll(results);

        updateResultCount(results.size(), criteria);
    }

    private SearchCriteria buildSearchCriteria() {
        SearchCriteria criteria = new SearchCriteria();

        if (txtSearchQuery != null && txtSearchQuery.getText() != null) {
            criteria.setQuery(txtSearchQuery.getText().trim());
        }
        if (cmbSubjectFilter != null) {
            criteria.setSubject(cmbSubjectFilter.getValue());
        }
        if (cmbDifficultyFilter != null) {
            criteria.setDifficulty(cmbDifficultyFilter.getValue());
        }
        if (cmbTagFilter != null) {
            criteria.setTag(cmbTagFilter.getValue());
        }
        if (cmbDateFilter != null) {
            criteria.setDateFilter(cmbDateFilter.getValue());
            if (criteria.getDateFilter() == DateFilterPreset.CUSTOM) {
                if (dpStartDate != null) {
                    criteria.setCustomStartDate(dpStartDate.getValue());
                }
                if (dpEndDate != null) {
                    criteria.setCustomEndDate(dpEndDate.getValue());
                }
            }
        }
        if (cmbConnectionFilter != null) {
            criteria.setConnectionFilter(cmbConnectionFilter.getValue());
        }

        return criteria;
    }

    private void updateResultCount(int count, SearchCriteria criteria) {
        if (lblResultCount != null) {
            if (count == 0) {
                lblResultCount.setText("No notes found");
            } else if (count == 1) {
                lblResultCount.setText("Found 1 note");
            } else {
                lblResultCount.setText("Found " + count + " notes");
            }
        }

        if (lblPlaceholderTitle != null && lblPlaceholderSubtitle != null) {
            if (criteria.isEmpty()) {
                lblPlaceholderTitle.setText("No notes in notebook");
                lblPlaceholderSubtitle.setText("Create notes in the Notes screen to begin exploring your knowledge base.");
            } else {
                lblPlaceholderTitle.setText("No matching notes found");
                lblPlaceholderSubtitle.setText("Try adjusting your keywords or clearing active filters.");
            }
        }
    }

    @FXML
    public void handleResetFilters() {
        isUpdatingFilters = true;
        try {
            if (txtSearchQuery != null) {
                txtSearchQuery.clear();
            }
            if (cmbSubjectFilter != null) {
                cmbSubjectFilter.setValue(SearchCriteria.ALL_SUBJECTS);
            }
            if (cmbDifficultyFilter != null) {
                cmbDifficultyFilter.setValue(SearchCriteria.ALL_DIFFICULTIES);
            }
            if (cmbTagFilter != null) {
                cmbTagFilter.setValue(SearchCriteria.ALL_TAGS);
            }
            if (cmbDateFilter != null) {
                cmbDateFilter.setValue(DateFilterPreset.ALL_TIME);
            }
            if (cmbConnectionFilter != null) {
                cmbConnectionFilter.setValue(ConnectionFilterPreset.ALL);
            }
            if (dpStartDate != null) {
                dpStartDate.setValue(null);
            }
            if (dpEndDate != null) {
                dpEndDate.setValue(null);
            }
            if (boxCustomDateRange != null) {
                boxCustomDateRange.setVisible(false);
                boxCustomDateRange.setManaged(false);
            }
        } finally {
            isUpdatingFilters = false;
        }

        handleSearch();
    }

    @FXML
    private void handleViewNote() {
        Note selected = tableResults.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }

        Optional<Note> fresh = noteService.getNoteWithTags(selected.getId());
        Note noteToView = fresh.orElse(selected);

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/note_view.fxml"));
            Parent root = loader.load();

            NoteViewController controller = loader.getController();
            Stage stage = new Stage();
            stage.setTitle("View Note - " + noteToView.getTitle());
            stage.initModality(Modality.APPLICATION_MODAL);
            if (tableResults.getScene() != null && tableResults.getScene().getWindow() != null) {
                stage.initOwner(tableResults.getScene().getWindow());
            }
            stage.setScene(new Scene(root));
            controller.setDialogStage(stage);
            controller.setNote(noteToView);
            controller.setEditHandler(note -> openEditorForNote(note, NoteEditorMode.EDIT));

            stage.showAndWait();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to open note viewer: " + e.getMessage(), e);
            showErrorAlert("Error", "Could not open note viewer: " + e.getMessage());
        }
    }

    @FXML
    private void handleEditNote() {
        Note selected = tableResults.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        openEditorForNote(selected, NoteEditorMode.EDIT);
    }

    private void openEditorForNote(Note note, NoteEditorMode mode) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/note_editor.fxml"));
            Parent root = loader.load();

            NoteEditorController controller = loader.getController();
            controller.setNoteService(noteService);

            Stage stage = new Stage();
            stage.setTitle(mode == NoteEditorMode.CREATE ? "Create Note" : "Edit Note");
            stage.initModality(Modality.APPLICATION_MODAL);
            if (tableResults.getScene() != null && tableResults.getScene().getWindow() != null) {
                stage.initOwner(tableResults.getScene().getWindow());
            }
            stage.setScene(new Scene(root));
            controller.setDialogStage(stage);

            Note targetNote = note;
            if (mode == NoteEditorMode.EDIT && note != null) {
                targetNote = noteService.getNoteWithTags(note.getId()).orElse(note);
            }
            controller.setNote(targetNote, mode);

            stage.showAndWait();

            if (controller.isSaved()) {
                refreshFilterMetadata();
                handleSearch();
                if (controller.getNote() != null) {
                    tableResults.getSelectionModel().select(controller.getNote());
                }
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to open note editor: " + e.getMessage(), e);
            showErrorAlert("Error", "Could not open note editor: " + e.getMessage());
        }
    }

    private void refreshFilterMetadata() {
        String currentSubject = cmbSubjectFilter.getValue();
        String currentTag = cmbTagFilter.getValue();

        isUpdatingFilters = true;
        try {
            List<String> subjects = searchService.getAvailableSubjects();
            cmbSubjectFilter.setItems(FXCollections.observableArrayList(subjects));
            if (subjects.contains(currentSubject)) {
                cmbSubjectFilter.setValue(currentSubject);
            } else {
                cmbSubjectFilter.setValue(SearchCriteria.ALL_SUBJECTS);
            }

            List<String> tags = searchService.getAvailableTags();
            cmbTagFilter.setItems(FXCollections.observableArrayList(tags));
            if (tags.contains(currentTag)) {
                cmbTagFilter.setValue(currentTag);
            } else {
                cmbTagFilter.setValue(SearchCriteria.ALL_TAGS);
            }
        } finally {
            isUpdatingFilters = false;
        }
    }

    @FXML
    private void handleOpenInMindMap() {
        Note selected = tableResults.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }

        MainController main = MainController.getInstance();
        if (main != null) {
            main.openInMindMap(selected);
        } else {
            LOGGER.log(Level.WARNING, "MainController instance is null; cannot navigate to Mind Map.");
        }
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
