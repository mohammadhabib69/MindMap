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
import javafx.scene.layout.StackPane;

import java.awt.Desktop;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

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
    @FXML private Label lblSource;
    @FXML private HBox boxActions;

    private WikipediaPageSummary currentSummary;

    @FXML
    public void initialize() {
        listResults.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(WikipediaSearchResult item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getTitle() + "\n" + item.getSnippet());
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
        }, error -> {
            LOGGER.log(Level.WARNING, "Search failed", error);
            handleError(error);
        });
    }

    private void loadSummary(String title) {
        setLoadingState(true, "Loading summary for " + title + "...");
        
        TaskExecutor.runAsync(() -> {
            return wikipediaService.getSummary(title);
        }, summary -> {
            setLoadingState(false, "Loaded summary.");
            currentSummary = summary;
            displaySummary(summary);
        }, error -> {
            LOGGER.log(Level.WARNING, "Failed to load summary", error);
            handleError(error);
        });
    }

    private void displaySummary(WikipediaPageSummary summary) {
        if (summary != null) {
            lblTitle.setText(summary.getTitle());
            lblSummary.setText(summary.getExtract());
            boxSource.setVisible(true);
            boxSource.setManaged(true);
            boxActions.setVisible(true);
        }
    }

    private void clearDetails() {
        lblTitle.setText("");
        lblSummary.setText("");
        boxSource.setVisible(false);
        boxSource.setManaged(false);
        boxActions.setVisible(false);
        currentSummary = null;
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
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("Wikipedia Integration Error");
            if (error instanceof ExternalApiException) {
                if (error.getMessage().contains("interrupted") || error.getMessage().contains("Network error") || error.getMessage().contains("timed out")) {
                    alert.setTitle("Connection Error");
                    alert.setContentText("Unable to connect to Wikipedia.\nPlease check your internet connection and try again.");
                } else {
                    alert.setTitle("API Error");
                    alert.setContentText("Wikipedia returned an error.\nPlease try again later.\nDetails: " + error.getMessage());
                }
            } else {
                alert.setTitle("Unexpected Error");
                alert.setContentText("An unexpected error occurred: " + error.getMessage());
            }
            alert.show();
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
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Could not open note editor: " + e.getMessage());
            alert.showAndWait();
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

