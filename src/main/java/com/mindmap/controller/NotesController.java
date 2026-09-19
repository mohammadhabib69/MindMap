package com.mindmap.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

/**
 * Controller for the Notes placeholder screen.
 */
public class NotesController {

    @FXML
    private TextField txtSearchNotes;

    @FXML
    private Button btnNewNote;

    @FXML
    private TableView<?> tableNotes;

    @FXML
    private TableColumn<?, String> colTitle;

    @FXML
    private TableColumn<?, String> colSubject;

    @FXML
    private TableColumn<?, String> colDifficulty;

    @FXML
    private TableColumn<?, String> colUpdated;

    @FXML
    public void initialize() {
        // TableView initialized empty for Phase 2; CRUD will be plugged in later phases
    }

    @FXML
    private void handleNewNote() {
        // Placeholder for future Note creation modal/view
    }
}
