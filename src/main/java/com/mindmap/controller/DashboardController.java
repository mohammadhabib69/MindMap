package com.mindmap.controller;

import com.mindmap.service.NoteService;
import com.mindmap.service.TagService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for the Dashboard screen connecting real SQLite statistics.
 */
public class DashboardController {

    private static final Logger LOGGER = Logger.getLogger(DashboardController.class.getName());

    @FXML
    private Label lblTotalNotes;

    @FXML
    private Label lblTotalSubjects;

    @FXML
    private Label lblTotalTags;

    @FXML
    private Label lblReviewsDue;

    private final NoteService noteService;
    private final TagService tagService;

    public DashboardController() {
        this.noteService = new NoteService();
        this.tagService = new TagService();
    }

    public DashboardController(NoteService noteService, TagService tagService) {
        this.noteService = noteService;
        this.tagService = tagService;
    }

    @FXML
    public void initialize() {
        refreshStatistics();
    }

    /**
     * Queries database services and updates dashboard statistic labels.
     */
    public void refreshStatistics() {
        try {
            int notes = noteService.getNoteCount();
            int tags = tagService.getTagCount();
            setStatistics(notes, 0, tags, 0);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to load dashboard statistics: " + e.getMessage(), e);
        }
    }

    /**
     * Updates the dashboard statistical counters.
     */
    public void setStatistics(int notes, int subjects, int tags, int reviewsDue) {
        if (lblTotalNotes != null) {
            lblTotalNotes.setText(String.valueOf(notes));
        }
        if (lblTotalSubjects != null) {
            lblTotalSubjects.setText(String.valueOf(subjects));
        }
        if (lblTotalTags != null) {
            lblTotalTags.setText(String.valueOf(tags));
        }
        if (lblReviewsDue != null) {
            lblReviewsDue.setText(String.valueOf(reviewsDue));
        }
    }
}
