package com.mindmap.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a scheduled revision session for a study note.
 */
public class Revision {

    private int id;
    private int noteId;
    private LocalDate reviewDate;
    private String status;
    private int intervalDays;
    private LocalDateTime createdAt;

    public Revision() {
    }

    public Revision(int noteId, LocalDate reviewDate, String status, int intervalDays) {
        this.noteId = noteId;
        this.reviewDate = reviewDate;
        this.status = status;
        this.intervalDays = intervalDays;
        this.createdAt = LocalDateTime.now();
    }

    public Revision(int id, int noteId, LocalDate reviewDate, String status, int intervalDays, LocalDateTime createdAt) {
        this.id = id;
        this.noteId = noteId;
        this.reviewDate = reviewDate;
        this.status = status;
        this.intervalDays = intervalDays;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getNoteId() {
        return noteId;
    }

    public void setNoteId(int noteId) {
        this.noteId = noteId;
    }

    public LocalDate getReviewDate() {
        return reviewDate;
    }

    public void setReviewDate(LocalDate reviewDate) {
        this.reviewDate = reviewDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getIntervalDays() {
        return intervalDays;
    }

    public void setIntervalDays(int intervalDays) {
        this.intervalDays = intervalDays;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Revision revision = (Revision) o;
        return id == revision.id && noteId == revision.noteId && Objects.equals(reviewDate, revision.reviewDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, noteId, reviewDate);
    }

    @Override
    public String toString() {
        return "Revision{" +
                "id=" + id +
                ", noteId=" + noteId +
                ", reviewDate=" + reviewDate +
                ", status='" + status + '\'' +
                ", intervalDays=" + intervalDays +
                ", createdAt=" + createdAt +
                '}';
    }
}
