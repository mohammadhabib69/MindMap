package com.mindmap.util;

import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility to load FXML views safely with error handling and fallback UI.
 */
public final class ViewManager {

    private static final Logger LOGGER = Logger.getLogger(ViewManager.class.getName());

    private ViewManager() {
        // Prevent instantiation
    }

    /**
     * Loads an FXML view from the specified path.
     * If loading fails, returns a fallback error component instead of throwing an exception.
     *
     * @param fxmlPath Resource path to the FXML file (e.g., "/fxml/dashboard.fxml").
     * @return The loaded Parent node, or an error node if loading failed.
     */
    public static Parent loadView(String fxmlPath) {
        try {
            URL resource = ViewManager.class.getResource(fxmlPath);
            if (resource == null) {
                throw new IOException("FXML resource not found: " + fxmlPath);
            }
            return FXMLLoader.load(resource);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to load FXML view from: " + fxmlPath, e);
            return createErrorPlaceholder(fxmlPath, e.getMessage());
        }
    }

    private static Parent createErrorPlaceholder(String fxmlPath, String errorDetail) {
        VBox errorBox = new VBox(12);
        errorBox.setAlignment(Pos.CENTER);
        errorBox.setPadding(new Insets(40));
        errorBox.setStyle("-fx-background-color: #fff1f2; -fx-border-color: #fecdd3; -fx-border-radius: 8px; -fx-background-radius: 8px;");

        Label title = new Label("Screen Load Error");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #9f1239;");

        Label pathLabel = new Label("Failed to load view: " + fxmlPath);
        pathLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #881337; -fx-font-weight: bold;");

        Label detail = new Label("Error details: " + errorDetail);
        detail.setWrapText(true);
        detail.setStyle("-fx-font-size: 12px; -fx-text-fill: #be123c;");

        errorBox.getChildren().addAll(title, pathLabel, detail);
        return errorBox;
    }
}
