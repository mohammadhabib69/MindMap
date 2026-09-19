package com.mindmap.controller;

import com.mindmap.model.Difficulty;
import com.mindmap.model.Note;
import com.mindmap.service.NoteService;
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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for the Note Editor dialog supporting CREATE and EDIT modes.
 */
public class NoteEditorController {

    private static final Logger LOGGER = Logger.getLogger(NoteEditorController.class.getName());

    @FXML
    private Label lblDialogTitle;

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
    }

    public void setNoteService(NoteService noteService) {
        this.noteService = noteService;
    }

    public void setNote(Note note, NoteEditorMode mode) {
        this.note = note;
        this.mode = mode;

        if (lblError != null) {
            lblError.setText("");
            lblError.setVisible(false);
        }

        if (mode == NoteEditorMode.CREATE) {
            lblDialogTitle.setText("Create New Note");
            btnSave.setText("Create Note");
            txtTitle.clear();
            txtSubject.clear();
            txtTags.clear();
            txtContent.clear();
            cmbDifficulty.setValue(Difficulty.MEDIUM.name());
        } else if (mode == NoteEditorMode.EDIT && note != null) {
            lblDialogTitle.setText("Edit Note");
            btnSave.setText("Save Changes");
            txtTitle.setText(note.getTitle());
            txtSubject.setText(note.getSubject() != null ? note.getSubject() : "");
            cmbDifficulty.setValue(note.getDifficulty() != null ? note.getDifficulty() : Difficulty.MEDIUM.name());
            txtTags.setText(note.getTagsString());
            txtContent.setText(note.getContent() != null ? note.getContent() : "");
        }
    }

    public boolean isSaved() {
        return saved;
    }

    public Note getNote() {
        return note;
    }

    @FXML
    private void handleSave() {
        String title = txtTitle.getText() != null ? txtTitle.getText().trim() : "";
        if (title.isEmpty()) {
            showError("Note title is required.");
            txtTitle.requestFocus();
            return;
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

        try {
            if (mode == NoteEditorMode.CREATE) {
                Note newNote = new Note(title, content, subject, difficulty);
                this.note = noteService.createNoteWithTags(newNote, tagNames);
            } else if (mode == NoteEditorMode.EDIT && note != null) {
                note.setTitle(title);
                note.setContent(content);
                note.setSubject(subject);
                note.setDifficulty(difficulty);
                this.note = noteService.updateNoteWithTags(note, tagNames);
            }

            saved = true;
            if (dialogStage != null) {
                dialogStage.close();
            }
        } catch (ValidationException ve) {
            showError(ve.getMessage());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error saving note: " + e.getMessage(), e);
            showError("An unexpected error occurred while saving the note.");
        }
    }

    @FXML
    private void handleCancel() {
        saved = false;
        if (dialogStage != null) {
            dialogStage.close();
        }
    }

    private void showError(String message) {
        if (lblError != null) {
            lblError.setText(message);
            lblError.setVisible(true);
        }
    }
}
