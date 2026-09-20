package com.mindmap.controller;

import com.mindmap.concurrency.TaskExecutor;
import com.mindmap.model.Note;
import com.mindmap.model.Tag;
import com.mindmap.service.NoteService;
import com.mindmap.service.TagService;
import com.mindmap.util.AnimationUtil;
import com.mindmap.util.DateUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.mindmap.util.UiUtils;

/**
 * Controller for the Notes management screen handling real SQLite CRUD, searching, and filtering.
 */
public class NotesController {

    private static final Logger LOGGER = Logger.getLogger(NotesController.class.getName());
    private static final String ALL_TAGS_LABEL = "All Tags";
    private final AtomicLong notesRequestId = new AtomicLong(0);

    @FXML
    private TextField txtSearchNotes;

    @FXML
    private ComboBox<String> cmbTagFilter;

    @FXML
    private Button btnClearFilter;

    @FXML
    private Button btnNewNote;

    @FXML
    private Button btnViewNote;
    @FXML
    private Button btnExportPdf;

    @FXML
    private Button btnEditNote;

    @FXML
    private Button btnDeleteNote;

    @FXML
    private TableView<Note> tableNotes;

    @FXML
    private TableColumn<Note, String> colTitle;

    @FXML
    private TableColumn<Note, String> colSubject;

    @FXML
    private TableColumn<Note, String> colDifficulty;

    @FXML
    private TableColumn<Note, String> colTags;

    @FXML
    private TableColumn<Note, String> colUpdated;

    @FXML
    private Label lblPlaceholderTitle;

    @FXML
    private Label lblPlaceholderSubtitle;

    private final NoteService noteService;
    private final TagService tagService;
    private final ObservableList<Note> notesObservableList = FXCollections.observableArrayList();

    public NotesController() {
        this.noteService = new NoteService();
        this.tagService = new TagService();
    }

    public NotesController(NoteService noteService, TagService tagService) {
        this.noteService = noteService;
        this.tagService = tagService;
    }

    @FXML
    public void initialize() {
        configureColumns();
        configureTableSelection();
        configureRowDoubleClicks();
        configureFilterListeners();
        setupButtonAnimations();

        loadTagFilters();
        applyFilterSync();
    }

    private void setupButtonAnimations() {
        AnimationUtil.addButtonHoverEffect(btnNewNote);
        AnimationUtil.addButtonHoverEffect(btnViewNote);
        AnimationUtil.addButtonHoverEffect(btnEditNote);
        AnimationUtil.addButtonHoverEffect(btnDeleteNote);
        AnimationUtil.addButtonHoverEffect(btnClearFilter);
    }

    private void configureColumns() {
        colTitle.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getTitle()));

        colSubject.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getSubject() != null ? cellData.getValue().getSubject() : ""));

        colDifficulty.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getDifficulty() != null ? cellData.getValue().getDifficulty() : ""));

        colTags.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getTagsString()));

        colUpdated.setCellValueFactory(cellData ->
                new SimpleStringProperty(DateUtil.formatDisplay(cellData.getValue().getUpdatedAt())));

        tableNotes.setItems(notesObservableList);
    }

    private void configureTableSelection() {
        tableNotes.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            boolean hasSelection = newSel != null;
            if (btnViewNote != null) btnViewNote.setDisable(!hasSelection);
            if (btnEditNote != null) btnEditNote.setDisable(!hasSelection);
            if (btnDeleteNote != null) btnDeleteNote.setDisable(!hasSelection);
        });
    }

    private void configureRowDoubleClicks() {
        tableNotes.setRowFactory(tv -> {
            TableRow<Note> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    handleViewNote();
                }
            });
            return row;
        });
    }

    private void configureFilterListeners() {
        if (txtSearchNotes != null) {
            txtSearchNotes.textProperty().addListener((obs, oldVal, newVal) -> applyFilter());
        }
        if (cmbTagFilter != null) {
            cmbTagFilter.valueProperty().addListener((obs, oldVal, newVal) -> applyFilter());
        }
    }

    public CompletableFuture<NotesFilterSnapshot> loadNotes() {
        return applyFilter();
    }

    private void loadTagFilters() {
        if (cmbTagFilter == null) {
            return;
        }
        String currentSelection = cmbTagFilter.getValue();
        List<String> tagNames = tagService.getAllTags().stream()
                .map(Tag::getName)
                .toList();

        ObservableList<String> items = FXCollections.observableArrayList();
        items.add(ALL_TAGS_LABEL);
        items.addAll(tagNames);

        cmbTagFilter.setItems(items);
        if (currentSelection != null && items.contains(currentSelection)) {
            cmbTagFilter.setValue(currentSelection);
        } else {
            cmbTagFilter.setValue(ALL_TAGS_LABEL);
        }
    }

    public record NotesFilterSnapshot(List<Note> results, int totalNoteCount, String query, String selectedTag) {}

    public CompletableFuture<NotesFilterSnapshot> applyFilter() {
        String query = txtSearchNotes != null ? txtSearchNotes.getText() : null;
        String selectedTag = cmbTagFilter != null ? cmbTagFilter.getValue() : null;
        long reqId = notesRequestId.incrementAndGet();
        final NoteService service = this.noteService;
        CompletableFuture<NotesFilterSnapshot> future = new CompletableFuture<>();

        TaskExecutor.runAsync(
                () -> {
                    List<Note> results = service.searchAndFilterNotes(query, selectedTag);
                    int count = service.getNoteCount();
                    return new NotesFilterSnapshot(results, count, query, selectedTag);
                },
                snapshot -> {
                    if (reqId != notesRequestId.get()) {
                        future.cancel(true);
                        return;
                    }
                    try {
                        notesObservableList.setAll(snapshot.results());
                        updatePlaceholder(snapshot.query(), snapshot.selectedTag(), snapshot.totalNoteCount());
                        future.complete(snapshot);
                    } catch (Exception e) {
                        LOGGER.log(Level.SEVERE, "Error updating notes UI: " + e.getMessage(), e);
                        future.completeExceptionally(e);
                    }
                },
                throwable -> {
                    if (reqId == notesRequestId.get()) {
                        LOGGER.log(Level.SEVERE, "Background notes filter error: " + throwable.getMessage(), throwable);
                    }
                    future.completeExceptionally(throwable);
                }
        );

        return future;
    }

    public void applyFilterSync() {
        String query = txtSearchNotes != null ? txtSearchNotes.getText() : null;
        String selectedTag = cmbTagFilter != null ? cmbTagFilter.getValue() : null;
        List<Note> results = noteService.searchAndFilterNotes(query, selectedTag);
        int count = noteService.getNoteCount();
        notesObservableList.setAll(results);
        updatePlaceholder(query, selectedTag, count);
    }

    private void updatePlaceholder(String query, String selectedTag, int totalNoteCount) {
        if (lblPlaceholderTitle == null || lblPlaceholderSubtitle == null) {
            return;
        }

        boolean hasQuery = (query != null && !query.trim().isEmpty());
        boolean hasTag = (selectedTag != null && !selectedTag.trim().isEmpty() && !ALL_TAGS_LABEL.equalsIgnoreCase(selectedTag.trim()));

        if (totalNoteCount == 0) {
            lblPlaceholderTitle.setText("No notes found");
            lblPlaceholderSubtitle.setText("Click '+ New Note' above to create your first note.");
        } else if (hasQuery || hasTag) {
            lblPlaceholderTitle.setText("No matching notes found");
            lblPlaceholderSubtitle.setText("Try adjusting your search query or tag filter.");
        } else {
            lblPlaceholderTitle.setText("No notes found");
            lblPlaceholderSubtitle.setText("Click '+ New Note' above to create your first note.");
        }
    }

    @FXML
    private void handleNewNote() {
        openEditorForNote(null, NoteEditorMode.CREATE);
    }

    @FXML
    private void handleViewNote() {
        Note selected = tableNotes.getSelectionModel().getSelectedItem();
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
            if (tableNotes.getScene() != null && tableNotes.getScene().getWindow() != null) {
                stage.initOwner(tableNotes.getScene().getWindow());
            }
            stage.setScene(new Scene(root));
            controller.setDialogStage(stage);
            controller.setNote(noteToView);
            controller.setEditHandler(note -> openEditorForNote(note, NoteEditorMode.EDIT));

            stage.showAndWait();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to open note view dialog: " + e.getMessage(), e);
            UiUtils.showError("Error", "Could not open note viewer: " + e.getMessage());
        }
    }

    @FXML
    private void handleEditNote() {
        Note selected = tableNotes.getSelectionModel().getSelectedItem();
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
            if (tableNotes.getScene() != null && tableNotes.getScene().getWindow() != null) {
                stage.initOwner(tableNotes.getScene().getWindow());
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
                loadTagFilters();
                applyFilter();
                if (controller.getNote() != null) {
                    tableNotes.getSelectionModel().select(controller.getNote());
                }
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to open note editor dialog: " + e.getMessage(), e);
            UiUtils.showError("Error", "Could not open note editor: " + e.getMessage());
        }
    }

    @FXML
    private void handleDeleteNote() {
        Note selected = tableNotes.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }

        boolean proceed = UiUtils.showConfirmation("Delete Note", "Delete this note?\n\nDeleting this note may also remove its associated connections and related data.");
        if (proceed) {
            if (btnDeleteNote != null) btnDeleteNote.setDisable(true);
            final NoteService service = this.noteService;
            final int noteId = selected.getId();

            TaskExecutor.runAsync(
                    () -> service.deleteNote(noteId),
                    deleted -> {
                        if (btnDeleteNote != null) btnDeleteNote.setDisable(false);
                        if (deleted) {
                            loadTagFilters();
                            applyFilter();
                        } else {
                            UiUtils.showError("Delete Failed", "The note could not be deleted from the database.");
                        }
                    },
                    throwable -> {
                        if (btnDeleteNote != null) btnDeleteNote.setDisable(false);
                        LOGGER.log(Level.SEVERE, "Error deleting note ID " + noteId, throwable);
                        UiUtils.showError("Database Error", "An error occurred while deleting the note.");
                    }
            );
        }
    }

    @FXML
    private void handleClearFilter() {
        if (txtSearchNotes != null) {
            txtSearchNotes.clear();
        }
        if (cmbTagFilter != null) {
            cmbTagFilter.setValue(ALL_TAGS_LABEL);
        }
        applyFilter();
    }

    @FXML
    private void handleExportPdf() {
        com.mindmap.model.Note selected = tableNotes.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Export Note as PDF");
        fileChooser.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("PDF Document", "*.pdf"));
        
        String safeName = selected.getTitle() != null ? selected.getTitle().replaceAll("[\\\\/:*?\"<>|]", "_") : "Note";
        fileChooser.setInitialFileName(safeName + ".pdf");
        
        java.io.File file = fileChooser.showSaveDialog(tableNotes.getScene().getWindow());
        if (file != null) {
            final com.mindmap.export.pdf.PdfExportService pdfService = new com.mindmap.export.pdf.PdfExportService();
            final com.mindmap.model.Note noteToExport = noteService.getNoteWithTags(selected.getId()).orElse(selected);
            
            com.mindmap.concurrency.TaskExecutor.runAsync(
                () -> {
                    pdfService.exportSingleNote(noteToExport, file);
                    return true;
                },
                success -> {
                    UiUtils.showInfo("Export Successful", "PDF exported successfully.");
                },
                error -> {
                    LOGGER.log(java.util.logging.Level.SEVERE, "Failed to export PDF", error);
                    UiUtils.showError("Export Failed", "Unable to export the PDF. Please choose another location.\n" + error.getMessage());
                }
            );
        }
    }
}
