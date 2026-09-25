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

    private javafx.scene.layout.StackPane quickSearchOverlay;
    private javafx.scene.control.TextField txtQuickSearch;
    private javafx.scene.control.ListView<com.mindmap.model.Note> listQuickSearch;
    private com.mindmap.service.NoteService noteService = new com.mindmap.service.NoteService();


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
        
        // Delay keyboard shortcut setup until scene is available
        javafx.application.Platform.runLater(this::setupKeyboardShortcuts);
    }
    
    private void setupKeyboardShortcuts() {
        javafx.scene.Scene scene = contentArea.getScene();
        if (scene != null) {
            scene.getAccelerators().put(new javafx.scene.input.KeyCodeCombination(javafx.scene.input.KeyCode.K, javafx.scene.input.KeyCombination.SHORTCUT_DOWN), this::toggleQuickSearch);
            scene.getAccelerators().put(new javafx.scene.input.KeyCodeCombination(javafx.scene.input.KeyCode.F, javafx.scene.input.KeyCombination.SHORTCUT_DOWN), this::showNotesAndFocusSearch);
            scene.getAccelerators().put(new javafx.scene.input.KeyCodeCombination(javafx.scene.input.KeyCode.N, javafx.scene.input.KeyCombination.SHORTCUT_DOWN), () -> {
                
                try {
                    javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/note_editor.fxml"));
                    javafx.scene.Parent rt = loader.load();
                    com.mindmap.controller.NoteEditorController controller = loader.getController();
                    controller.setNoteService(noteService);
                    javafx.stage.Stage stage = new javafx.stage.Stage();
                    stage.setTitle("Create Note");
                    stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
                    stage.setScene(new javafx.scene.Scene(rt));
                    controller.setDialogStage(stage);
                    controller.setNote(null, com.mindmap.controller.NoteEditorMode.CREATE);
                    stage.showAndWait();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            });
            scene.addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED, e -> {
                if (e.getCode() == javafx.scene.input.KeyCode.ESCAPE) {
                    if (quickSearchOverlay != null && quickSearchOverlay.isVisible()) {
                        hideQuickSearch();
                        e.consume();
                    }
                }
            });
        }
    }
    
    private void toggleQuickSearch() {
        if (quickSearchOverlay == null) {
            buildQuickSearchUI();
            contentArea.getChildren().add(quickSearchOverlay);
        }
        if (quickSearchOverlay.isVisible()) {
            hideQuickSearch();
        } else {
            quickSearchOverlay.setVisible(true);
            txtQuickSearch.clear();
            listQuickSearch.getItems().clear();
            txtQuickSearch.requestFocus();
        }
    }
    
    private void hideQuickSearch() {
        if (quickSearchOverlay != null) {
            quickSearchOverlay.setVisible(false);
            contentArea.requestFocus();
        }
    }
    
    private void buildQuickSearchUI() {
        quickSearchOverlay = new javafx.scene.layout.StackPane();
        quickSearchOverlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.4);");
        quickSearchOverlay.setVisible(false);
        
        javafx.scene.layout.VBox dialog = new javafx.scene.layout.VBox(10);
        dialog.setMaxWidth(600);
        dialog.setMaxHeight(400);
        dialog.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-padding: 20; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 20, 0, 0, 10);");
        
        javafx.scene.control.Label lbl = new javafx.scene.control.Label("🔎 Global Quick Search");
        lbl.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #475569;");
        
        txtQuickSearch = new javafx.scene.control.TextField();
        txtQuickSearch.setPromptText("Search notes, subjects, tags... (Press ESC to close)");
        txtQuickSearch.setStyle("-fx-font-size: 16px; -fx-padding: 10;");
        
        listQuickSearch = new javafx.scene.control.ListView<>();
        listQuickSearch.setStyle("-fx-background-color: transparent;");
        listQuickSearch.setCellFactory(lv -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(com.mindmap.model.Note item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    javafx.scene.layout.VBox box = new javafx.scene.layout.VBox(4);
                    javafx.scene.control.Label title = new javafx.scene.control.Label(item.getTitle());
                    title.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
                    javafx.scene.control.Label subj = new javafx.scene.control.Label(item.getSubject() + " | Tags: " + item.getTagsString());
                    subj.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b;");
                    box.getChildren().addAll(title, subj);
                    setGraphic(box);
                }
            }
        });
        
        txtQuickSearch.textProperty().addListener((obs, old, val) -> {
            if (val != null && val.length() >= 2) {
                com.mindmap.concurrency.TaskExecutor.execute(() -> {
                    java.util.List<com.mindmap.model.Note> results = noteService.searchNotes(val);
                    javafx.application.Platform.runLater(() -> listQuickSearch.getItems().setAll(results));
                });
            } else {
                listQuickSearch.getItems().clear();
            }
        });
        
        listQuickSearch.setOnKeyPressed(e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.ENTER) {
                com.mindmap.model.Note selected = listQuickSearch.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    hideQuickSearch();
                    
                    if (!com.mindmap.util.SecurityHelper.verifyPin(selected)) return;
                    try {
                        javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/note_view.fxml"));
                        javafx.scene.Parent rt = loader.load();
                        com.mindmap.controller.NoteViewController controller = loader.getController();
                        javafx.stage.Stage stage = new javafx.stage.Stage();
                        stage.setTitle("Note Details");
                        stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
                        stage.setScene(new javafx.scene.Scene(rt));
                        controller.setNote(selected);
                        stage.showAndWait();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }

                }
            }
        });
        
        listQuickSearch.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                com.mindmap.model.Note selected = listQuickSearch.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    hideQuickSearch();
                    
                    if (!com.mindmap.util.SecurityHelper.verifyPin(selected)) return;
                    try {
                        javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/note_view.fxml"));
                        javafx.scene.Parent rt = loader.load();
                        com.mindmap.controller.NoteViewController controller = loader.getController();
                        javafx.stage.Stage stage = new javafx.stage.Stage();
                        stage.setTitle("Note Details");
                        stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
                        stage.setScene(new javafx.scene.Scene(rt));
                        controller.setNote(selected);
                        stage.showAndWait();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }

                }
            }
        });
        
        dialog.getChildren().addAll(lbl, txtQuickSearch, listQuickSearch);
        quickSearchOverlay.getChildren().add(dialog);
        
        quickSearchOverlay.setOnMouseClicked(e -> {
            if (e.getTarget() == quickSearchOverlay) {
                hideQuickSearch();
            }
        });
    }


    private void setupSidebarAnimations() {
        AnimationUtil.addSidebarNavHoverEffect(btnDashboard);
        AnimationUtil.addSidebarNavHoverEffect(btnNotes);
        AnimationUtil.addSidebarNavHoverEffect(btnMindMap);
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
    public void showNotesAndFocusSearch() {
        showNotes();
        javafx.application.Platform.runLater(() -> {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {}
            javafx.application.Platform.runLater(() -> {
                javafx.scene.Node searchBox = contentArea.lookup("#txtSearchQuery");
                if (searchBox != null) {
                    searchBox.requestFocus();
                }
            });
        });
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
        
        javafx.scene.layout.VBox content = new javafx.scene.layout.VBox(24);
        content.setPadding(new javafx.geometry.Insets(32, 48, 24, 48));
        content.setAlignment(javafx.geometry.Pos.CENTER);
        
        javafx.scene.layout.VBox header = new javafx.scene.layout.VBox(8);
        header.setAlignment(javafx.geometry.Pos.CENTER);
        javafx.scene.control.Label title = new javafx.scene.control.Label("MindMap");
        title.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");
        javafx.scene.control.Label subtitle = new javafx.scene.control.Label("Personal Knowledge Base & Study Organizer");
        subtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #475569;");
        header.getChildren().addAll(title, subtitle);
        
        javafx.scene.control.Label version = new javafx.scene.control.Label("Version 1.0.0");
        version.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b; -fx-font-weight: bold; -fx-padding: 8px 16px; -fx-background-color: #f1f5f9; -fx-background-radius: 20px;");
        
        javafx.scene.control.Label desc = new javafx.scene.control.Label(
            "An integrated platform for capturing thoughts,\n" +
            "connecting ideas visually, and retaining\n" +
            "knowledge through spaced repetition."
        );
        desc.setWrapText(true);
        desc.setMinHeight(javafx.scene.layout.Region.USE_PREF_SIZE);
        desc.setStyle("-fx-font-size: 14px; -fx-text-fill: #334155; -fx-text-alignment: center; -fx-alignment: center; -fx-padding: 10 0 10 0;");
        
        javafx.scene.control.Label copyright = new javafx.scene.control.Label("© 2026 MindMap");
        copyright.setStyle("-fx-font-size: 12px; -fx-text-fill: #94a3b8;");
        
        content.getChildren().addAll(header, version, desc, copyright);
        
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
