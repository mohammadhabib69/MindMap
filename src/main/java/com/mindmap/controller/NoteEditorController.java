package com.mindmap.controller;

import com.mindmap.concurrency.TaskExecutor;
import com.mindmap.model.Difficulty;
import com.mindmap.model.Note;
import com.mindmap.service.NoteService;
import com.mindmap.service.RevisionService;
import com.mindmap.util.ValidationException;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.mindmap.util.UiUtils;
import java.util.Objects;

/**
 * Controller for the Note Editor dialog supporting CREATE and EDIT modes.
 */
public class NoteEditorController {

    private static final Logger LOGGER = Logger.getLogger(NoteEditorController.class.getName());

    @FXML
    private Label lblDialogTitle;
    @FXML
    private javafx.scene.control.ComboBox<String> cmbTemplate;

    @FXML
    private javafx.scene.control.ToggleButton btnFavorite;


    @FXML
    private TextField txtTitle;

    @FXML
    private TextField txtSubject;

    @FXML
    private ComboBox<String> cmbDifficulty;

    @FXML
    private TextField txtTags;

    @FXML
    private TextArea txtContent;

    @FXML
    private Label lblError;

    @FXML
    private Button btnSave;

    @FXML
    private Button btnCancel;

    private Stage dialogStage;
    private NoteService noteService;
    private Note note;
    private NoteEditorMode mode = NoteEditorMode.CREATE;
    private boolean saved = false;
    private String originalTitle = "";
    private String originalSubject = "";
    private String originalTags = "";
    private String originalContent = "";
    private String originalDifficulty = "";

    @FXML
    public void initialize() {
        cmbDifficulty.setItems(FXCollections.observableArrayList(
                Difficulty.EASY.name(),
                Difficulty.MEDIUM.name(),
                Difficulty.HARD.name()
        ));
        cmbDifficulty.setValue(Difficulty.MEDIUM.name());

        if (lblError != null) {
            lblError.setText("");
            lblError.setVisible(false);
        }
    }


    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
        this.dialogStage.setOnCloseRequest(event -> {
            if (hasUnsavedChanges()) {
                event.consume();
                boolean confirm = UiUtils.showConfirmation("Discard Changes?", "You have unsaved changes. Are you sure you want to discard them and close?");
                if (confirm) {
                    dialogStage.close();
                }
            }
        });
        
        // Add Ctrl+S / Cmd+S shortcut
        dialogStage.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                javafx.scene.input.KeyCombination saveCombo = new javafx.scene.input.KeyCodeCombination(
                        javafx.scene.input.KeyCode.S, javafx.scene.input.KeyCombination.SHORTCUT_DOWN);
                newScene.getAccelerators().put(saveCombo, this::handleSave);
            }
        });
    }


    public void setNoteService(NoteService noteService) {
        this.noteService = noteService;
    }

    public void setNote(Note note, NoteEditorMode mode) {
        if (note != null && mode == NoteEditorMode.EDIT) {
            com.mindmap.concurrency.TaskExecutor.execute(() -> {
                new com.mindmap.service.NoteService().updateLastViewed(note.getId());
            });
        }
        this.note = note;
        this.mode = mode;

        if (lblError != null) {
            lblError.setText("");
            lblError.setVisible(false);
        }

        if (mode == NoteEditorMode.CREATE) {
            lblDialogTitle.setText("Create New Note");
            btnSave.setText("Create Note");
            if (note != null) {
                txtTitle.setText(note.getTitle() != null ? note.getTitle() : "");
                txtSubject.setText(note.getSubject() != null ? note.getSubject() : "");
                txtTags.setText(note.getTagsString() != null ? note.getTagsString() : "");
                txtContent.setText(note.getContent() != null ? note.getContent() : "");
            originalTitle = txtTitle.getText();
            originalSubject = txtSubject.getText();
            originalTags = txtTags.getText();
            originalContent = txtContent.getText();
            originalDifficulty = cmbDifficulty.getValue();
                cmbDifficulty.setValue(note.getDifficulty() != null ? note.getDifficulty() : Difficulty.MEDIUM.name());
                originalTitle = txtTitle.getText();
                originalSubject = txtSubject.getText();
                originalTags = txtTags.getText();
                originalContent = txtContent.getText();
                originalDifficulty = cmbDifficulty.getValue();
            } else {
                txtTitle.clear();
                originalTitle = "";
                originalSubject = "";
                originalTags = "";
                originalContent = "";
                originalDifficulty = Difficulty.MEDIUM.name();
                txtSubject.clear();
                txtTags.clear();
                txtContent.clear();
                cmbDifficulty.setValue(Difficulty.MEDIUM.name());
            }
        } else if (mode == NoteEditorMode.EDIT && note != null) {
            lblDialogTitle.setText("Edit Note");
            btnSave.setText("Save Changes");
            txtTitle.setText(note.getTitle());
            txtSubject.setText(note.getSubject() != null ? note.getSubject() : "");
            cmbDifficulty.setValue(note.getDifficulty() != null ? note.getDifficulty() : Difficulty.MEDIUM.name());
                originalTitle = txtTitle.getText();
                originalSubject = txtSubject.getText();
                originalTags = txtTags.getText();
                originalContent = txtContent.getText();
                originalDifficulty = cmbDifficulty.getValue();
            txtTags.setText(note.getTagsString());
            txtContent.setText(note.getContent() != null ? note.getContent() : "");
            originalTitle = txtTitle.getText();
            originalSubject = txtSubject.getText();
            originalTags = txtTags.getText();
            originalContent = txtContent.getText();
            originalDifficulty = cmbDifficulty.getValue();
        }
    }

    public boolean isSaved() {
        return saved;
    }

    public Note getNote() {
        return note;
    }

    @FXML
    public CompletableFuture<Note> handleSave() {
        String title = txtTitle.getText() != null ? txtTitle.getText().trim() : "";
        if (title.isEmpty()) {
            showError("Note title is required.");
            txtTitle.requestFocus();
            return CompletableFuture.completedFuture(null);
        }

        String subject = txtSubject.getText() != null ? txtSubject.getText().trim() : "";
        String difficulty = cmbDifficulty.getValue() != null ? cmbDifficulty.getValue() : Difficulty.MEDIUM.name();
        String content = txtContent.getText() != null ? txtContent.getText() : "";
        String rawTags = txtTags.getText() != null ? txtTags.getText().trim() : "";

        List<String> tagNames = Arrays.stream(rawTags.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .toList();

        if (btnSave != null) {
            btnSave.setDisable(true);
            btnSave.setText(mode == NoteEditorMode.CREATE ? "Creating..." : "Saving...");
        }

        final NoteService service = this.noteService;
        final NoteEditorMode currentMode = this.mode;
        final Note currentNote = this.note;
        CompletableFuture<Note> future = new CompletableFuture<>();

        TaskExecutor.runAsync(
                () -> {
                    if (currentMode == NoteEditorMode.CREATE) {
                        Note newNote = new Note(title, content, subject, difficulty);
                        Note created = service.createNoteWithTags(newNote, tagNames);
                        if (created != null && created.getId() > 0) {
                            try {
                                new RevisionService().scheduleInitialReview(created.getId());
                            } catch (Exception e) {
                                LOGGER.log(Level.WARNING, "Could not auto-schedule initial revision: " + e.getMessage());
                            }
                        }
                        return created;
                    } else if (currentMode == NoteEditorMode.EDIT && currentNote != null) {
                        currentNote.setTitle(title);
                        currentNote.setContent(content);
                        currentNote.setSubject(subject);
                        currentNote.setDifficulty(difficulty);
                        return service.updateNoteWithTags(currentNote, tagNames);
                    }
                    return currentNote;
                },
                resultNote -> {
                    this.note = resultNote;
                    this.saved = true;
                    UiUtils.showInfo(currentMode == NoteEditorMode.CREATE ? "Note Created" : "Note Updated",
                                     currentMode == NoteEditorMode.CREATE ? "Note created successfully." : "Note updated successfully.");
                    if (btnSave != null) {
                        btnSave.setDisable(false);
                        btnSave.setText(currentMode == NoteEditorMode.CREATE ? "Create Note" : "Save Changes");
                    }
                    if (dialogStage != null) {
                        dialogStage.close();
                    }
                    future.complete(resultNote);
                },
                throwable -> {
                    if (btnSave != null) {
                        btnSave.setDisable(false);
                        btnSave.setText(currentMode == NoteEditorMode.CREATE ? "Create Note" : "Save Changes");
                    }
                    if (throwable instanceof ValidationException ve) {
                        showError(ve.getMessage());
                    } else {
                        LOGGER.log(Level.SEVERE, "Unexpected error saving note: " + throwable.getMessage(), throwable);
                        showError("An unexpected error occurred while saving the note.");
                    }
                    future.completeExceptionally(throwable);
                }
        );

        return future;
    }
    @FXML
    private void handleCancel() {
        if (hasUnsavedChanges()) {
            boolean discard = UiUtils.showConfirmation("Unsaved Changes", "You have unsaved changes. Leave without saving?");
            if (!discard) {
                return;
            }
        }
        saved = false;
        if (dialogStage != null) {
            dialogStage.close();
        }
    }

    private boolean hasUnsavedChanges() {
        if (!Objects.equals(originalTitle, txtTitle.getText())) return true;
        if (!Objects.equals(originalSubject, txtSubject.getText())) return true;
        if (!Objects.equals(originalTags, txtTags.getText())) return true;
        if (!Objects.equals(originalContent, txtContent.getText())) return true;
        if (!Objects.equals(originalDifficulty, cmbDifficulty.getValue())) return true;
        return false;
    }

    private void showError(String message) {
        if (lblError != null) {
            lblError.setText(message);
            lblError.setVisible(true);
        }
    }


    @FXML
    private void handleToggleFavorite() {
        updateFavoriteButtonUI();
    }

    private void updateFavoriteButtonUI() {
        if (btnFavorite != null) {
            if (btnFavorite.isSelected()) {
                btnFavorite.setText("★ Favorite");
                btnFavorite.setStyle("-fx-text-fill: #eab308; -fx-font-weight: bold;");
            } else {
                btnFavorite.setText("☆ Favorite");
                btnFavorite.setStyle("");
            }
        }
    }

    private void applyTemplate(String templateName) {
        if (templateName == null || templateName.equals("Blank Note")) {
            if (txtContent.getText().trim().isEmpty() || txtContent.getText().contains("Key Concepts:")) {
                txtContent.setText("");
            }
            return;
        }

        if (!txtContent.getText().trim().isEmpty() && !txtContent.getText().startsWith("Topic:")) {
            boolean confirm = com.mindmap.util.UiUtils.showConfirmation("Overwrite Content?", "Applying a template will replace current content.");
            if (!confirm) return;
        }

        String templateText = "";
        switch (templateName) {
            case "Lecture Note":
                templateText = """
Topic:

Key Concepts:

Important Points:

Examples:

Questions:

Summary:
""";
                break;
            case "Concept":
                templateText = """
Definition:

Core Idea:

Examples:

Related Concepts:
""";
                break;
            case "Programming":
                templateText = """
Problem:

Approach:

Code / Pseudocode:

Complexity:

Notes:
""";
                break;
            case "Research":
                templateText = """
Source:

Summary:

Key Findings:

References:
""";
                break;
            case "Problem Solving":
                templateText = """
Problem:

Given:

Approach:

Solution:

Explanation:
""";
                break;
        }
        txtContent.setText(templateText);
    }

}