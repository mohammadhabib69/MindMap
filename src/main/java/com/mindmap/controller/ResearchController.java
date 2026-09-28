package com.mindmap.controller;

import com.mindmap.concurrency.TaskExecutor;
import com.mindmap.external.ExternalApiException;
import com.mindmap.external.WikipediaService;
import com.mindmap.external.dto.WikipediaPageSummary;
import com.mindmap.external.dto.WikipediaSearchResult;
import com.mindmap.model.Note;
import com.mindmap.util.ViewManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;

import java.awt.Desktop;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.mindmap.util.UiUtils;

public class ResearchController {
    private static final Logger LOGGER = Logger.getLogger(ResearchController.class.getName());

    private final WikipediaService wikipediaService = new WikipediaService();

    @FXML private TextField txtSearch;
    @FXML private Button btnSearch;
    @FXML private ProgressIndicator progressIndicator;
    @FXML private Label lblStatus;
    
    @FXML private ListView<WikipediaSearchResult> listResults;
    @FXML private Label lblTitle;
    @FXML private Label lblSummary;
    @FXML private HBox boxSource;

    @FXML private VBox boxEmptyResults;
    @FXML private VBox boxEmptySelection;
    @FXML private VBox boxSelectionDetails;
    @FXML private VBox boxLoadingSelection;
    private String currentLoadingTitle;

    @FXML private Label lblSource;
    @FXML private HBox boxActions;

    private WikipediaPageSummary currentSummary;

    @FXML
    public void initialize() {
        listResults.setCellFactory(param -> new ListCell<>() {
            private final VBox vBox = new VBox(2);
            private final javafx.scene.control.Label titleLbl = new javafx.scene.control.Label();
            private final javafx.scene.control.Label snippetLbl = new javafx.scene.control.Label();

            {
                titleLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #0f172a; -fx-font-size: 13px;");
                titleLbl.setWrapText(true);
                snippetLbl.setStyle("-fx-text-fill: #64748b; -fx-font-size: 11px;");
                snippetLbl.setWrapText(true);
                vBox.getChildren().addAll(titleLbl, snippetLbl);
                // Bind to list cell width minus some padding to prevent horizontal scroll
                vBox.prefWidthProperty().bind(widthProperty().subtract(30));
                vBox.maxWidthProperty().bind(widthProperty().subtract(30));
            }

            @Override
            protected void updateItem(WikipediaSearchResult item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    titleLbl.setText(item.getTitle());
                    snippetLbl.setText(item.getSnippet());
                    setGraphic(vBox);
                    setText(null);
                }
            }
        });

        listResults.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                loadSummary(newVal.getTitle());
            } else {
                clearDetails();
            }
        });
        
        clearDetails();
    }

    @FXML
    private void handleSearch() {
        String query = txtSearch.getText();
        if (query == null || query.trim().isEmpty()) {
            return;
        }

        setLoadingState(true, "Searching Wikipedia...");
        listResults.getItems().clear();
        clearDetails();

        TaskExecutor.runAsync(() -> {
            return wikipediaService.search(query);
        }, results -> {
            setLoadingState(false, results.isEmpty() ? "No matching Wikipedia articles found." : results.size() + " results found.");
            listResults.getItems().setAll(results);
            if (boxEmptyResults != null) {
                boolean empty = results.isEmpty();
                boxEmptyResults.setVisible(empty);
                boxEmptyResults.setManaged(empty);
            }

        }, error -> {
            LOGGER.log(Level.WARNING, "Search failed", error);
            handleError(error);
        });
    }

    private void loadSummary(String title) {
        currentLoadingTitle = title;
        setLoadingState(true, "Loading summary for " + title + "...");
        
        if (boxEmptySelection != null) {
            boxEmptySelection.setVisible(false);
            boxEmptySelection.setManaged(false);
        }
        if (boxSelectionDetails != null) {
            boxSelectionDetails.setVisible(false);
            boxSelectionDetails.setManaged(false);
        }
        if (boxLoadingSelection != null) {
            boxLoadingSelection.setVisible(true);
            boxLoadingSelection.setManaged(true);
        }
        
        TaskExecutor.runAsync(() -> {
            return wikipediaService.getSummary(title);
        }, summary -> {
            if (!title.equals(currentLoadingTitle)) return;
            setLoadingState(false, "Loaded summary.");
            currentSummary = summary;
            
            if (boxLoadingSelection != null) {
                boxLoadingSelection.setVisible(false);
                boxLoadingSelection.setManaged(false);
            }
            displaySummary(summary);
        }, error -> {
            if (!title.equals(currentLoadingTitle)) return;
            LOGGER.log(Level.WARNING, "Failed to load summary", error);
            if (boxLoadingSelection != null) {
                boxLoadingSelection.setVisible(false);
                boxLoadingSelection.setManaged(false);
            }
            if (boxEmptySelection != null) {
                boxEmptySelection.setVisible(true);
                boxEmptySelection.setManaged(true);
            }
            lblStatus.setText("Unable to load this Wikipedia summary.");
        });
    }

    private void displaySummary(WikipediaPageSummary summary) {
        if (summary != null) {
            lblTitle.setText(summary.getTitle());
            lblSummary.setText(summary.getExtract());
            if (lblSource != null) {
                lblSource.setText("Wikipedia");
            }
            if (boxSource != null) {
                boxSource.setVisible(true);
                boxSource.setManaged(true);
            }
            if (boxActions != null) {
                boxActions.setVisible(true);
            }
            
            if (boxEmptySelection != null) {
                boxEmptySelection.setVisible(false);
                boxEmptySelection.setManaged(false);
            }
            if (boxSelectionDetails != null) {
                boxSelectionDetails.setVisible(true);
                boxSelectionDetails.setManaged(true);
            }
        }
    }

    private void clearDetails() {
        currentLoadingTitle = null;
        lblTitle.setText("");
        lblSummary.setText("");
        currentSummary = null;
        if (boxEmptySelection != null) {
            boxEmptySelection.setVisible(true);
            boxEmptySelection.setManaged(true);
        }
        if (boxSelectionDetails != null) {
            boxSelectionDetails.setVisible(false);
            boxSelectionDetails.setManaged(false);
        }
        if (boxLoadingSelection != null) {
            boxLoadingSelection.setVisible(false);
            boxLoadingSelection.setManaged(false);
        }
    }

    private void setLoadingState(boolean isLoading, String status) {
        Platform.runLater(() -> {
            btnSearch.setDisable(isLoading);
            txtSearch.setDisable(isLoading);
            progressIndicator.setVisible(isLoading);
            progressIndicator.setManaged(isLoading);
            lblStatus.setText(status);
        });
    }

    private void handleError(Throwable error) {
        setLoadingState(false, "An error occurred.");
        Platform.runLater(() -> {
            
            if (error instanceof ExternalApiException) {
                if (error.getMessage().contains("interrupted") || error.getMessage().contains("Network error") || error.getMessage().contains("timed out")) {
                    UiUtils.showError("Connection Error", "Unable to connect to Wikipedia.\nPlease check your internet connection and try again.");
                } else {
                    UiUtils.showError("API Error", "Wikipedia returned an error.\nPlease try again later.\nDetails: " + error.getMessage());
                }
            } else {
                UiUtils.showError("Unexpected Error", "An unexpected error occurred: " + error.getMessage());
            }

        });
    }

    @FXML
    private void handleOpenSource() {
        if (currentSummary != null && currentSummary.getPageUrl() != null && Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().browse(new URI(currentSummary.getPageUrl()));
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Failed to open browser", e);
            }
        }
    }

    @FXML
    private void handleCreateNote() {
        if (currentSummary == null) return;
        
        Note draftNote = createDraftNote(currentSummary);

        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/note_editor.fxml"));
            javafx.scene.Parent root = loader.load();

            NoteEditorController controller = loader.getController();
            controller.setNoteService(new com.mindmap.service.NoteService());

            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("Create Note from Research");
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            if (txtSearch.getScene() != null && txtSearch.getScene().getWindow() != null) {
                stage.initOwner(txtSearch.getScene().getWindow());
            }
            stage.setScene(new javafx.scene.Scene(root));
            controller.setDialogStage(stage);
            controller.setNote(draftNote, NoteEditorMode.CREATE);

            stage.showAndWait();
        } catch (java.io.IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to open note editor dialog from Research", e);
            UiUtils.showError("Error", "Could not open note editor: " + e.getMessage());
        }
    }
    // Visible for testing
    public Note createDraftNote(WikipediaPageSummary summary) {
        Note draftNote = new Note();
        draftNote.setTitle(summary.getTitle() != null ? summary.getTitle() : "Untitled");
        
        String extract = summary.getExtract() != null ? summary.getExtract() : "";
        String url = summary.getPageUrl() != null ? summary.getPageUrl() : "";
        
        String content = extract;
        if (!url.isEmpty()) {
            content += "\n\n---\nSource: Wikipedia\n" + url;
        }
        
        draftNote.setContent(content);
        draftNote.setSubject("Research");
        draftNote.setDifficulty("Medium");
        draftNote.setCreatedAt(LocalDateTime.now());
        draftNote.setUpdatedAt(LocalDateTime.now());
        
        draftNote.addTag(new com.mindmap.model.Tag("wikipedia"));
        if (summary.getTitle() != null && !summary.getTitle().trim().isEmpty()) {
            String topicTag = summary.getTitle().trim().toLowerCase().replace(" ", "-");
            draftNote.addTag(new com.mindmap.model.Tag(topicTag));
        }
        
        return draftNote;
    }
}

