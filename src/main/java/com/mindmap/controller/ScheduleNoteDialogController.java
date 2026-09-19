package com.mindmap.controller;

import com.mindmap.model.Note;
import com.mindmap.model.Revision;
import com.mindmap.service.NoteService;
import com.mindmap.service.RevisionService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.List;

/**
 * Controller for the dialog that allows users to pick an unscheduled note
 * and add it to the Spaced Repetition queue.
 */
public class ScheduleNoteDialogController {

    @FXML private TextField txtSearchNote;
    @FXML private ListView<Note> listUnscheduledNotes;
    @FXML private Button btnSchedule;

    private Stage dialogStage;
    private RevisionService revisionService;
    private NoteService noteService;
    private boolean scheduled = false;

    private final ObservableList<Note> masterData = FXCollections.observableArrayList();
    private FilteredList<Note> filteredData;

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setServices(RevisionService revisionService, NoteService noteService) {
        this.revisionService = revisionService;
        this.noteService = noteService;
        loadUnscheduledNotes();
    }

    public boolean isScheduled() {
        return scheduled;
    }

    @FXML
    public void initialize() {
        filteredData = new FilteredList<>(masterData, p -> true);
        listUnscheduledNotes.setItems(filteredData);

        // Custom list cell styling
        listUnscheduledNotes.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Note item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    String subject = item.getSubject() != null && !item.getSubject().isBlank()
                            ? " [" + item.getSubject().trim() + "]" : "";
                    String diff = item.getDifficulty() != null ? " • " + item.getDifficulty().toUpperCase() : "";
                    setText(item.getTitle() + subject + diff);
                    setStyle("-fx-font-size: 12px; -fx-padding: 6 8;");
                }
            }
        });

        // Enable button when item selected
        listUnscheduledNotes.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            btnSchedule.setDisable(newV == null);
        });

        // Filter listener
        txtSearchNote.textProperty().addListener((obs, oldV, newV) -> {
            filteredData.setPredicate(note -> {
                if (newV == null || newV.trim().isEmpty()) {
                    return true;
                }
                String q = newV.trim().toLowerCase();
                boolean titleMatch = note.getTitle() != null && note.getTitle().toLowerCase().contains(q);
                boolean subjMatch = note.getSubject() != null && note.getSubject().toLowerCase().contains(q);
                return titleMatch || subjMatch;
            });
        });
    }

    private void loadUnscheduledNotes() {
        if (noteService == null || revisionService == null) return;

        List<Note> allNotes = noteService.getAllNotes();
        masterData.clear();

        for (Note note : allNotes) {
            // Only show notes that do not currently have a pending revision
            boolean hasActive = revisionService.getDueReviews().stream().anyMatch(r -> r.getNoteId() == note.getId())
                    || revisionService.getUpcomingReviews().stream().anyMatch(r -> r.getNoteId() == note.getId());
            if (!hasActive) {
                masterData.add(note);
            }
        }
    }

    @FXML
    private void handleSchedule() {
        Note selected = listUnscheduledNotes.getSelectionModel().getSelectedItem();
        if (selected != null && revisionService != null) {
            revisionService.scheduleInitialReview(selected.getId());
            scheduled = true;
            if (dialogStage != null) {
                dialogStage.close();
            }
        }
    }

    @FXML
    private void handleCancel() {
        if (dialogStage != null) {
            dialogStage.close();
        }
    }
}
