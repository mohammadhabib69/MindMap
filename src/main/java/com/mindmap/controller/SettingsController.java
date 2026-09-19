package com.mindmap.controller;

import com.mindmap.database.DatabaseManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * Controller for the Settings screen.
 */
public class SettingsController {

    @FXML
    private Label lblDatabaseUrl;

    @FXML
    private Label lblAppVersion;

    @FXML
    private Label lblAppPhase;

    @FXML
    public void initialize() {
        if (lblDatabaseUrl != null) {
            lblDatabaseUrl.setText(DatabaseManager.JDBC_URL);
        }
        if (lblAppVersion != null) {
            lblAppVersion.setText("1.0-SNAPSHOT");
        }
        if (lblAppPhase != null) {
            lblAppPhase.setText("Phase 6 - Interactive 3D Knowledge Space");
        }
    }
}
