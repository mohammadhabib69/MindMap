package com.mindmap.controller;

import com.mindmap.database.DatabaseManager;
import com.mindmap.model.Note;
import com.mindmap.util.AnimationUtil;
import com.mindmap.util.ViewManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;

import java.util.logging.Logger;
import com.mindmap.util.UiUtils;

/**
 * Main application controller managing sidebar navigation and dynamic content loading.
 */
public class MainController {

    private static final Logger LOGGER = Logger.getLogger(MainController.class.getName());

    @FXML
    private Button btnDashboard;

    @FXML
    private Button btnNotes;

    @FXML
    private Button btnMindMap;

    @FXML
    private Button btnSearch;

    @FXML
    private Button btnRevision;

    @FXML
    private Button btnTimeline;
    @FXML
    private Button btnResearch;


    @FXML
    private Button btnSettings;

    @FXML
    private Button btnAbout;

    @FXML
    private Button btnExit;

    @FXML
    private StackPane contentArea;

    private static MainController instance;

    public static MainController getInstance() {
        return instance;
    }

    private Button currentActiveButton;

    @FXML
    public void initialize() {
        instance = this;
        setupSidebarAnimations();
        // Load default dashboard screen on startup
        showDashboard();
    }

    private void setupSidebarAnimations() {
        AnimationUtil.addSidebarNavHoverEffect(btnDashboard);
        AnimationUtil.addSidebarNavHoverEffect(btnNotes);
        AnimationUtil.addSidebarNavHoverEffect(btnMindMap);
        AnimationUtil.addSidebarNavHoverEffect(btnSearch);
        AnimationUtil.addSidebarNavHoverEffect(btnRevision);
        AnimationUtil.addSidebarNavHoverEffect(btnTimeline);
        AnimationUtil.addSidebarNavHoverEffect(btnResearch);

        AnimationUtil.addSidebarNavHoverEffect(btnSettings);
        AnimationUtil.addSidebarNavHoverEffect(btnAbout);
        AnimationUtil.addSidebarNavHoverEffect(btnExit);
    }

    @FXML
    public void showDashboard() {
        navigateTo("/fxml/dashboard.fxml", btnDashboard);
    }

    @FXML
    public void showNotes() {
        navigateTo("/fxml/notes.fxml", btnNotes);
    }

    @FXML
    public void showMindMap() {
        navigateTo("/fxml/mindmap.fxml", btnMindMap);
    }

    @FXML
    public void showSearch() {
        navigateTo("/fxml/search.fxml", btnSearch);
    }

    @FXML
    public void showRevision() {
        navigateTo("/fxml/revision.fxml", btnRevision);
    }

    @FXML
    public void showTimeline() {
        navigateTo("/fxml/timeline.fxml", btnTimeline);
    }

    @FXML
    public void showResearch() {
        navigateTo("/fxml/research.fxml", btnResearch);
    }


    public void showSettings() {
        navigateTo("/fxml/settings.fxml", btnSettings);
    }

    @FXML
        private void handleAbout() {
        javafx.scene.control.Dialog<Void> dialog = new javafx.scene.control.Dialog<>();
        dialog.setTitle("About MindMap");
        
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
        dialog.getDialogPane().getStyleClass().add("card");
        
        javafx.scene.layout.VBox content = new javafx.scene.layout.VBox(20);
        content.setPadding(new javafx.geometry.Insets(24, 32, 24, 32));
        content.setAlignment(javafx.geometry.Pos.CENTER);
        
        javafx.scene.layout.VBox header = new javafx.scene.layout.VBox(4);
        header.setAlignment(javafx.geometry.Pos.CENTER);
        javafx.scene.control.Label title = new javafx.scene.control.Label("MindMap");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");
        javafx.scene.control.Label subtitle = new javafx.scene.control.Label("Personal Knowledge Base\n& Study Organizer");
        subtitle.setStyle("-fx-font-size: 16px; -fx-text-fill: #475569; -fx-alignment: center; -fx-text-alignment: center;");
        javafx.scene.control.Label version = new javafx.scene.control.Label("1.0-SNAPSHOT");
        version.setStyle("-fx-font-size: 12px; -fx-text-fill: #94a3b8; -fx-padding: 8 0 0 0;");
        header.getChildren().addAll(title, subtitle, version);
        
        javafx.scene.control.Separator sep1 = new javafx.scene.control.Separator();
        
        javafx.scene.control.Label desc = new javafx.scene.control.Label(
            "MindMap helps you take notes, organize subjects,\n" +
            "discover connections with interactive 2D and 3D\n" +
            "knowledge spaces, and retain information through\n" +
            "spaced repetition."
        );
        desc.setStyle("-fx-font-size: 14px; -fx-text-fill: #334155; -fx-text-alignment: center; -fx-alignment: center;");
        
        javafx.scene.layout.VBox tech = new javafx.scene.layout.VBox(8);
        tech.setAlignment(javafx.geometry.Pos.CENTER);
        javafx.scene.control.Label techHeader = new javafx.scene.control.Label("TECHNOLOGY");
        techHeader.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #64748b;");
        javafx.scene.control.Label techList = new javafx.scene.control.Label("JavaFX 21\nSQLite\nJackson\nOpenPDF");
        techList.setStyle("-fx-font-size: 13px; -fx-text-fill: #1e293b; -fx-text-alignment: center; -fx-alignment: center;");
        tech.getChildren().addAll(techHeader, techList);
        
        javafx.scene.layout.VBox proj = new javafx.scene.layout.VBox(8);
        proj.setAlignment(javafx.geometry.Pos.CENTER);
        javafx.scene.control.Label projHeader = new javafx.scene.control.Label("PROJECT");
        projHeader.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #64748b;");
        javafx.scene.control.Label projList = new javafx.scene.control.Label("Phase 18 - Final Release QA\nDatabase: SQLite");
        projList.setStyle("-fx-font-size: 13px; -fx-text-fill: #1e293b; -fx-text-alignment: center; -fx-alignment: center;");
        proj.getChildren().addAll(projHeader, projList);
        
        content.getChildren().addAll(header, sep1, desc, tech, proj);
        
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(javafx.scene.control.ButtonType.CLOSE);
        
        dialog.showAndWait();
    }

    @FXML
    private void handleExit() {
        LOGGER.info("Application exit requested by user.");
        Platform.exit();
    }

    /**
     * Navigates to the specified FXML screen and updates the active navigation button state.
     *
     * @param fxmlPath The path to the FXML file.
     * @param targetButton The corresponding sidebar button.
     */
    private void navigateTo(String fxmlPath, Button targetButton) {
        Parent view = ViewManager.loadView(fxmlPath);
        if (contentArea != null && view != null) {
            contentArea.getChildren().setAll(view);
            setActiveButton(targetButton);
        }
    }

    /**
     * Navigates to the Mind Map view, selects the specified note, and focuses the camera/canvas on it.
     *
     * @param note The note to display and focus in Mind Map.
     */
    public void openInMindMap(Note note) {
        ViewManager.ViewResult result = ViewManager.loadViewWithController("/fxml/mindmap.fxml");
        if (contentArea != null && result.getRoot() != null) {
            contentArea.getChildren().setAll(result.getRoot());
            setActiveButton(btnMindMap);
            if (result.getController() instanceof MindMapController mindMapController && note != null) {
                Platform.runLater(() -> mindMapController.focusNote(note));
            }
        }
    }

    /**
     * Updates the visual active state on the navigation buttons.
     *
     * @param button The button to activate.
     */
    private void setActiveButton(Button button) {
        if (currentActiveButton != null) {
            currentActiveButton.getStyleClass().remove("active");
            currentActiveButton.setTranslateX(0);
            currentActiveButton.setScaleX(1.0);
            currentActiveButton.setScaleY(1.0);
        }
        if (button != null) {
            if (!button.getStyleClass().contains("active")) {
                button.getStyleClass().add("active");
            }
            button.setTranslateX(0);
            button.setScaleX(1.0);
            button.setScaleY(1.0);
            currentActiveButton = button;
        }
    }
}
