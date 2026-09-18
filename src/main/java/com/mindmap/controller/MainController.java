package com.mindmap.controller;

import com.mindmap.database.DatabaseManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * Controller for the Phase 1 test view.
 */
public class MainController {

    @FXML
    private Label statusLabel;

    @FXML
    public void initialize() {
        if (statusLabel != null) {
            statusLabel.setText("Database: Connected (" + DatabaseManager.getDatabasePath() + ")");
        }
    }
}
