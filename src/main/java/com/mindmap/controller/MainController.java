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
    public void showSettings() {
        navigateTo("/fxml/settings.fxml", btnSettings);
    }

    @FXML
    private void handleAbout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About MindMap");
        alert.setHeaderText("MindMap: Personal Knowledge Base & Study Organizer");
        alert.setContentText("""
                Version: 1.0-SNAPSHOT
                Phase: Phase 7 - Advanced Search & Filtering
                Framework: JavaFX 21 & SQLite JDBC
                Database: """ + DatabaseManager.JDBC_URL + """

                
                MindMap helps you take notes, organize subjects, discover connections with interactive 2D & 3D knowledge spaces, and retain information through spaced repetition.
                """);
        alert.showAndWait();
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
