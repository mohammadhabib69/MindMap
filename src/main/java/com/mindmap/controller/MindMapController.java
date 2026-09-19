package com.mindmap.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;

/**
 * Controller for the Mind Map (Knowledge Graph) screen.
 */
public class MindMapController {

    @FXML
    private Pane graphCanvasPane;

    @FXML
    private Button btnZoomIn;

    @FXML
    private Button btnZoomOut;

    @FXML
    private Button btnResetView;

    @FXML
    public void initialize() {
        // Graph placeholder initialized for Phase 2
    }

    @FXML
    private void handleZoomIn() {
        // Future zoom in logic
    }

    @FXML
    private void handleZoomOut() {
        // Future zoom out logic
    }

    @FXML
    private void handleResetView() {
        // Future reset graph viewport
    }
}
