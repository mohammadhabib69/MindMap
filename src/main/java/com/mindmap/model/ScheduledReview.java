package com.mindmap.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * Composite view model binding a {@link Revision} schedule to its target {@link Note}.
 */
public class ScheduledReview {

    private final Revision revision;
    private final Note note;

    public ScheduledReview(Revision revision, Note note) {
        this.revision = Objects.requireNonNull(revision, "Revision cannot be null");
        this.note = Objects.requireNonNull(note, "Note cannot be null");
    }

    public Revision getRevision() {
        return revision;
    }

    public Note getNote() {
        return note;
    }

    public int getNoteId() {
        return revision.getNoteId();
    }

    public LocalDate getReviewDate() {
        return revision.getReviewDate();
    }

    public int getIntervalDays() {
        return revision.getIntervalDays();
    }

    public String getStatus() {
        return revision.getStatus();
    }

    public String getTitle() {
        return note.getTitle();
    }

    public String getSubject() {
        return note.getSubject();
    }

    public String getDifficulty() {
        return note.getDifficulty();
    }

    public String getContent() {
        return note.getContent();
    }

    public boolean isDue(LocalDate today) {
        if (revision.getReviewDate() == null) return false;
        return !revision.getReviewDate().isAfter(today);
    }

    public boolean isDue() {
        return isDue(LocalDate.now());
    }

    public boolean isOverdue(LocalDate today) {
        if (revision.getReviewDate() == null) return false;
        return revision.getReviewDate().isBefore(today);
    }

    public boolean isOverdue() {
        return isOverdue(LocalDate.now());
    }

    public boolean isDueToday(LocalDate today) {
        if (revision.getReviewDate() == null) return false;
        return revision.getReviewDate().isEqual(today);
    }

    public boolean isDueToday() {
        return isDueToday(LocalDate.now());
    }

    public long getDaysUntilDue(LocalDate today) {
        if (revision.getReviewDate() == null) return 0;
        return ChronoUnit.DAYS.between(today, revision.getReviewDate());
    }

    public long getDaysUntilDue() {
        return getDaysUntilDue(LocalDate.now());
    }


    @Override
    public String toString() {
        return "ScheduledReview{" +
                "noteTitle='" + note.getTitle() + '\'' +
                ", reviewDate=" + revision.getReviewDate() +
                ", intervalDays=" + revision.getIntervalDays() +
                ", status='" + revision.getStatus() + '\'' +
                '}';
    }
}
