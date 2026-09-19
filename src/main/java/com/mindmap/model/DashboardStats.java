package com.mindmap.model;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Data-transfer object representing a consolidated snapshot of learning metrics,
 * knowledge graph distribution, revision progress, and recent activity for the Dashboard.
 */
public class DashboardStats {

    private final int totalNotes;
    private final int totalConnections;
    private final int totalTags;
    private final int dueToday;
    private final int upcomingReviews;
    private final int totalScheduled;
    private final int completedToday;
    private final int connectedNotes;
    private final int isolatedNotes;

    private final Map<String, Integer> notesBySubject;
    private final Map<String, Integer> notesByDifficulty;
    private final List<Note> recentNotes;
    private final List<TimelineEvent> recentActivity;

    public DashboardStats(int totalNotes,
                          int totalConnections,
                          int totalTags,
                          int dueToday,
                          int upcomingReviews,
                          int totalScheduled,
                          int completedToday,
                          int connectedNotes,
                          int isolatedNotes,
                          Map<String, Integer> notesBySubject,
                          Map<String, Integer> notesByDifficulty,
                          List<Note> recentNotes,
                          List<TimelineEvent> recentActivity) {
        this.totalNotes = totalNotes;
        this.totalConnections = totalConnections;
        this.totalTags = totalTags;
        this.dueToday = dueToday;
        this.upcomingReviews = upcomingReviews;
        this.totalScheduled = totalScheduled;
        this.completedToday = completedToday;
        this.connectedNotes = connectedNotes;
        this.isolatedNotes = isolatedNotes;
        this.notesBySubject = notesBySubject != null ? notesBySubject : Collections.emptyMap();
        this.notesByDifficulty = notesByDifficulty != null ? notesByDifficulty : Collections.emptyMap();
        this.recentNotes = recentNotes != null ? recentNotes : Collections.emptyList();
        this.recentActivity = recentActivity != null ? recentActivity : Collections.emptyList();
    }

    public int getTotalNotes() {
        return totalNotes;
    }

    public int getTotalConnections() {
        return totalConnections;
    }

    public int getTotalTags() {
        return totalTags;
    }

    public int getDueToday() {
        return dueToday;
    }

    public int getUpcomingReviews() {
        return upcomingReviews;
    }

    public int getTotalScheduled() {
        return totalScheduled;
    }

    public int getCompletedToday() {
        return completedToday;
    }

    public int getConnectedNotes() {
        return connectedNotes;
    }

    public int getIsolatedNotes() {
        return isolatedNotes;
    }

    public Map<String, Integer> getNotesBySubject() {
        return notesBySubject;
    }

    public Map<String, Integer> getNotesByDifficulty() {
        return notesByDifficulty;
    }

    public List<Note> getRecentNotes() {
        return recentNotes;
    }

    public List<TimelineEvent> getRecentActivity() {
        return recentActivity;
    }

    /**
     * Calculates the percentage of reviews completed today out of total due + completed.
     * Returns 100.0 if there were no reviews due today (all caught up).
     */
    public double getReviewProgressRatio() {
        int totalForToday = dueToday + completedToday;
        if (totalForToday == 0) {
            return 1.0;
        }
        return (double) completedToday / totalForToday;
    }

    public int getReviewProgressPercent() {
        return (int) Math.round(getReviewProgressRatio() * 100.0);
    }

    /**
     * Calculates the percentage of total notes that are connected to at least one other note.
     */
    public double getConnectedRatio() {
        if (totalNotes == 0) {
            return 0.0;
        }
        return (double) connectedNotes / totalNotes;
    }

    public int getConnectedPercent() {
        return (int) Math.round(getConnectedRatio() * 100.0);
    }
}
