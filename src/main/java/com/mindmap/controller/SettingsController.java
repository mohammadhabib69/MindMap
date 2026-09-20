package com.mindmap.controller;

import com.mindmap.database.DatabaseManager;
import com.mindmap.export.dto.MindMapExport;
import com.mindmap.service.ImportExportService;
import com.mindmap.concurrency.TaskExecutor;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for the Settings screen.
 */
public class SettingsController {

    private static final Logger LOGGER = Logger.getLogger(SettingsController.class.getName());
    private final ImportExportService importExportService = new ImportExportService();

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
            lblAppPhase.setText("Phase 12 - Data Import/Export");
        }
    }

    @FXML
    private void handleExport() {
        Window window = lblDatabaseUrl.getScene().getWindow();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export MindMap Data");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("MindMap JSON", "*.json"));
        fileChooser.setInitialFileName("mindmap_export.json");
        
        File file = fileChooser.showSaveDialog(window);
        if (file != null) {
            // Show importing dialog or just run in background
            TaskExecutor.execute(() -> {
                try {
                    importExportService.exportToFile(file);
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Export Successful");
                        alert.setHeaderText(null);
                        alert.setContentText("MindMap data exported successfully.");
                        alert.showAndWait();
                    });
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Export failed", e);
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Export Failed");
                        alert.setHeaderText("Failed to export data");
                        alert.setContentText(e.getMessage());
                        alert.showAndWait();
                    });
                }
            });
        }
    }

    @FXML
    private void handleImport() {
        Window window = lblDatabaseUrl.getScene().getWindow();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Import MindMap Data");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("MindMap JSON", "*.json"));
        
        File file = fileChooser.showOpenDialog(window);
        if (file != null) {
            TaskExecutor.execute(() -> {
                try {
                    MindMapExport data = importExportService.validateAndPreview(file);
                    
                    Platform.runLater(() -> {
                        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                        confirm.setTitle("Import Preview");
                        confirm.setHeaderText("Ready to import from " + file.getName());
                        
                        String previewText = String.format(
                            "%d Notes\n%d Tags\n%d Connections\n%d Revisions\n%d Learning Events\n\nFormat Version: %d",
                            data.getNotes().size(),
                            data.getTags().size(),
                            data.getConnections().size(),
                            data.getRevisions().size(),
                            data.getLearningEvents().size(),
                            data.getFormatVersion()
                        );
                        confirm.setContentText(previewText);
                        
                        Optional<ButtonType> result = confirm.showAndWait();
                        if (result.isPresent() && result.get() == ButtonType.OK) {
                            executeImport(data);
                        }
                    });
                    
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Import validation failed", e);
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Import Failed");
                        alert.setHeaderText("Failed to validate JSON file");
                        alert.setContentText("No existing data was modified.\n" + e.getMessage());
                        alert.showAndWait();
                    });
                }
            });
        }
    }

    private void executeImport(MindMapExport data) {
        TaskExecutor.execute(() -> {
            try {
                importExportService.importData(data);
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Import Successful");
                    alert.setHeaderText(null);
                    alert.setContentText("Import completed successfully.\nPlease navigate to Dashboard or Notes to see the changes.");
                    alert.showAndWait();
                });
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Import transaction failed", e);
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Import Failed");
                    alert.setHeaderText("An error occurred during import");
                    alert.setContentText("No existing data was modified.\n" + e.getMessage());
                    alert.showAndWait();
                });
            }
        });
    }

    @FXML
    private void handleExportPdfAll() {
        javafx.stage.Window window = lblDatabaseUrl.getScene().getWindow();
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Export All Notes to PDF");
        fileChooser.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("PDF Document", "*.pdf"));
        fileChooser.setInitialFileName("MindMap_Notes.pdf");
        
        java.io.File file = fileChooser.showSaveDialog(window);
        if (file != null) {
            final com.mindmap.export.pdf.PdfExportService pdfService = new com.mindmap.export.pdf.PdfExportService();
            final com.mindmap.service.NoteService noteService = new com.mindmap.service.NoteService();
            
            com.mindmap.concurrency.TaskExecutor.runAsync(
                () -> {
                    java.util.List<com.mindmap.model.Note> allNotes = noteService.getAllNotes();
                    if (allNotes == null || allNotes.isEmpty()) {
                        throw new com.mindmap.export.pdf.PdfExportException("No notes available to export.");
                    }
                    // Eagerly load tags for all notes
                    for (com.mindmap.model.Note n : allNotes) {
                        noteService.getNoteWithTags(n.getId()).ifPresent(fresh -> n.setTags(fresh.getTags()));
                    }
                    pdfService.exportMultipleNotes(allNotes, file);
                    return true;
                },
                success -> {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Export Successful");
                    alert.setHeaderText(null);
                    alert.setContentText("All notes exported to PDF successfully.");
                    alert.showAndWait();
                },
                error -> {
                    LOGGER.log(Level.SEVERE, "Failed to export PDF", error);
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Export Failed");
                    alert.setHeaderText("An error occurred while generating the PDF");
                    alert.setContentText(error.getMessage());
                    alert.showAndWait();
                }
            );
        }
    }
}
