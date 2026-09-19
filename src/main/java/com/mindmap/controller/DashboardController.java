package com.mindmap.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * Controller for the Dashboard screen.
 */
public class DashboardController {

    @FXML
    private Label lblTotalNotes;

    @FXML
    private Label lblTotalSubjects;

    @FXML
    private Label lblTotalTags;

    @FXML
    private Label lblReviewsDue;

    @FXML
    public void initialize() {
        // Placeholder statistics for Phase 2; prepared for real SQLite data in later phases
        setStatistics(0, 0, 0, 0);
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
