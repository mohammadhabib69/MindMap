package com.mindmap.controller;

import com.mindmap.model.Connection;
import com.mindmap.model.Note;
import com.mindmap.service.ConnectionService;
import com.mindmap.service.NoteService;
import com.mindmap.util.AnimationUtil;
import com.mindmap.util.ValidationException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.stage.Stage;

import java.util.Comparator;
import java.util.List;

/**
 * Controller for the Connect Notes modal dialog.
 */
public class ConnectionDialogController {

    @FXML
    private ComboBox<Note> cmbSourceNote;

    @FXML
    private ComboBox<Note> cmbTargetNote;

    @FXML
    private ComboBox<String> cmbRelation;

    @FXML
    private Label lblError;

    @FXML
    private Button btnCancel;

    @FXML
    private Button btnConnect;

    private ConnectionService connectionService;
    private NoteService noteService;
    private Stage dialogStage;
    private boolean connected = false;
    private Connection createdConnection;

    @FXML
    public void initialize() {
        setupRelationComboBox();
        setupNoteCellFactories();
        setupButtonAnimations();
    }

    private void setupButtonAnimations() {
        AnimationUtil.addButtonHoverEffect(btnConnect);
        AnimationUtil.addButtonHoverEffect(btnCancel);
    }

    private void setupRelationComboBox() {
        cmbRelation.setItems(FXCollections.observableArrayList(ConnectionService.STANDARD_RELATIONS));
        cmbRelation.setValue(ConnectionService.DEFAULT_RELATION);
    }

    private void setupNoteCellFactories() {
        cmbSourceNote.setCellFactory(lv -> createNoteListCell());
        cmbSourceNote.setButtonCell(createNoteListCell());

        cmbTargetNote.setCellFactory(lv -> createNoteListCell());
        cmbTargetNote.setButtonCell(createNoteListCell());
    }

    private ListCell<Note> createNoteListCell() {
        return new ListCell<>() {
            @Override
            protected void updateItem(Note note, boolean empty) {
                super.updateItem(note, empty);
                if (empty || note == null) {
                    setText(null);
                } else {
                    String text = note.getTitle();
                    if (note.getSubject() != null && !note.getSubject().trim().isEmpty()) {
                        text += "  (" + note.getSubject().trim() + ")";
                    }
                    setText(text);
                }
            }
        };
    }

    public void setServices(ConnectionService connectionService, NoteService noteService) {
        this.connectionService = connectionService;
        this.noteService = noteService;
        loadNotes();
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setInitialSelection(Note sourceNote, Note targetNote) {
        if (sourceNote != null) {
            for (Note n : cmbSourceNote.getItems()) {
                if (n.getId() == sourceNote.getId()) {
                    cmbSourceNote.setValue(n);
                    break;
                }
            }
        }
        if (targetNote != null) {
            for (Note n : cmbTargetNote.getItems()) {
                if (n.getId() == targetNote.getId()) {
                    cmbTargetNote.setValue(n);
                    break;
                }
            }
        }
    }

    private void loadNotes() {
        if (noteService == null) return;

        List<Note> notes = noteService.getAllNotes();
        notes.sort(Comparator.comparing(Note::getTitle, String.CASE_INSENSITIVE_ORDER));
        ObservableList<Note> observableNotes = FXCollections.observableArrayList(notes);

        cmbSourceNote.setItems(observableNotes);
        cmbTargetNote.setItems(observableNotes);
    }

    @FXML
    private void handleConnect() {
        lblError.setText("");

        Note source = cmbSourceNote.getValue();
        Note target = cmbTargetNote.getValue();

        if (source == null) {
            lblError.setText("Please select a source note.");
            return;
        }
        if (target == null) {
            lblError.setText("Please select a target note.");
            return;
        }

        String relationText = cmbRelation.getEditor().getText();
        if (relationText == null || relationText.trim().isEmpty()) {
            relationText = cmbRelation.getValue();
        }

        try {
            createdConnection = connectionService.createConnection(source.getId(), target.getId(), relationText);
            connected = true;
            if (dialogStage != null) {
                dialogStage.close();
            }
        } catch (ValidationException ex) {
            lblError.setText(ex.getMessage());
        } catch (Exception ex) {
            lblError.setText("Failed to create connection: " + ex.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        if (dialogStage != null) {
            dialogStage.close();
        }
    }

    public boolean isConnected() {
        return connected;
    }

    public Connection getCreatedConnection() {
        return createdConnection;
    }
}
