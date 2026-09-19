package com.mindmap;

import com.mindmap.database.DatabaseInitializer;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Application entry point for MindMap.
 */
public class Main extends Application {

    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());
    private static final String APP_TITLE = "MindMap - Personal Knowledge Base";
    private static final int WINDOW_WIDTH = 1050;
    private static final int WINDOW_HEIGHT = 680;
    private static final int MIN_WIDTH = 720;
    private static final int MIN_HEIGHT = 480;

    private String initErrorMessage = null;

    @Override
    public void init() {
        try {
            // Initialize SQLite database and ensure test schema exists
            DatabaseInitializer.initialize();
            LOGGER.info("Application initialization completed successfully.");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database initialization failed: " + e.getMessage(), e);
            initErrorMessage = "Failed to initialize database: " + e.getMessage();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error during initialization: " + e.getMessage(), e);
            initErrorMessage = "Unexpected initialization error: " + e.getMessage();
        }
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle(APP_TITLE);

        if (initErrorMessage != null) {
            showErrorScene(primaryStage, initErrorMessage);
            return;
        }

        try {
            URL fxmlUrl = getClass().getResource("/fxml/main.fxml");
            if (fxmlUrl == null) {
                throw new IOException("FXML resource not found: /fxml/main.fxml");
            }

            Parent root = FXMLLoader.load(fxmlUrl);
            Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);

            URL cssUrl = getClass().getResource("/css/style.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            } else {
                LOGGER.warning("CSS resource not found: /css/style.css. Proceeding with default styles.");
            }

            primaryStage.setScene(scene);
            primaryStage.setMinWidth(MIN_WIDTH);
            primaryStage.setMinHeight(MIN_HEIGHT);
            primaryStage.show();
            LOGGER.info("MindMap window displayed successfully.");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to load application view: " + e.getMessage(), e);
            showErrorScene(primaryStage, "Application could not load the main view: " + e.getMessage());
        }
    }

    private void showErrorScene(Stage stage, String userMessage) {
        VBox errorLayout = new VBox(15);
        errorLayout.setAlignment(Pos.CENTER);
        errorLayout.setPadding(new Insets(30));
        errorLayout.setStyle("-fx-background-color: #fff1f2;");

        Label titleLabel = new Label("MindMap - Startup Warning");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #9f1239;");

        Label messageLabel = new Label(userMessage);
        messageLabel.setWrapText(true);
        messageLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #881337;");

        errorLayout.getChildren().addAll(titleLabel, messageLabel);

        Scene errorScene = new Scene(errorLayout, WINDOW_WIDTH, WINDOW_HEIGHT);
        stage.setScene(errorScene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
