package com.mindmap.controller;

import com.mindmap.model.Note;
import com.mindmap.model.Tag;
import com.mindmap.util.DateUtil;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;

import java.util.function.Consumer;

/**
 * Controller for viewing note details in a modal dialog.
 */
public class NoteViewController {

    @FXML
    private Label lblTitle;

    @FXML
    private Label lblSubject;

    @FXML
    private Label lblDifficulty;

    @FXML
    private FlowPane flowTags;

    @FXML
    private Label lblCreatedAt;

    @FXML
    private Label lblUpdatedAt;

    @FXML
    private TextArea txtContent;

    @FXML
    private Button btnEdit;

    @FXML
    private Button btnClose;

    private Stage dialogStage;
    private Note note;
    private Consumer<Note> editHandler;

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setEditHandler(Consumer<Note> editHandler) {
        this.editHandler = editHandler;
    }

    public void setNote(Note note) {
        if (note != null) {
            com.mindmap.concurrency.TaskExecutor.execute(() -> {
                new com.mindmap.service.NoteService().updateLastViewed(note.getId());
            });
        }

        this.note = note;
        if (note == null) {
            return;
        }

        lblTitle.setText(note.getTitle());
        lblSubject.setText(note.getSubject() != null && !note.getSubject().isEmpty() ? note.getSubject() : "No subject");

        String diff = note.getDifficulty() != null ? note.getDifficulty() : "MEDIUM";
        lblDifficulty.setText(diff);
        lblDifficulty.getStyleClass().removeAll("badge-easy", "badge-medium", "badge-hard");
        switch (diff.toUpperCase()) {
            case "EASY" -> lblDifficulty.getStyleClass().add("badge-easy");
            case "HARD" -> lblDifficulty.getStyleClass().add("badge-hard");
            default -> lblDifficulty.getStyleClass().add("badge-medium");
        }

        flowTags.getChildren().clear();
        if (note.getTags() != null && !note.getTags().isEmpty()) {
            for (Tag tag : note.getTags()) {
                Label tagBadge = new Label(tag.getName());
                tagBadge.getStyleClass().add("tag-badge");
                flowTags.getChildren().add(tagBadge);
            }
        } else {
            Label noTagsLabel = new Label("No tags");
            noTagsLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12px; -fx-font-style: italic;");
            flowTags.getChildren().add(noTagsLabel);
        }

        lblCreatedAt.setText(note.getCreatedAt() != null ? DateUtil.formatDisplay(note.getCreatedAt()) : "N/A");
        lblUpdatedAt.setText(note.getUpdatedAt() != null ? DateUtil.formatDisplay(note.getUpdatedAt()) : "N/A");

        txtContent.setText(note.getContent() != null ? note.getContent() : "");
    }

    @FXML
    private void handleEdit() {
        if (dialogStage != null) {
            dialogStage.close();
        }
        if (editHandler != null && note != null) {
            editHandler.accept(note);
        }
    }

    @FXML
    private void handleClose() {
        if (dialogStage != null) {
            dialogStage.close();
        }
    }
}
