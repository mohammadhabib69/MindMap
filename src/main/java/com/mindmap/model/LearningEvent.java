package com.mindmap.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a chronological activity or study milestone in the learning timeline.
 */
public class LearningEvent {

    private int id;
    private Integer noteId; // Nullable: an event may exist without being tied to a specific note
    private String eventType;
    private LocalDateTime eventDate;
    private String description;

    public LearningEvent() {
    }

    public LearningEvent(Integer noteId, String eventType, String description) {
        this.noteId = noteId;
        this.eventType = eventType;
        this.eventDate = LocalDateTime.now();
        this.description = description;
    }

    public LearningEvent(int id, Integer noteId, String eventType, LocalDateTime eventDate, String description) {
        this.id = id;
        this.noteId = noteId;
        this.eventType = eventType;
        this.eventDate = eventDate;
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Integer getNoteId() {
        return noteId;
    }

    public void setNoteId(Integer noteId) {
        this.noteId = noteId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public LocalDateTime getEventDate() {
        return eventDate;
    }

    public void setEventDate(LocalDateTime eventDate) {
        this.eventDate = eventDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LearningEvent that = (LearningEvent) o;
        return id == that.id && Objects.equals(eventDate, that.eventDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, eventDate);
    }

    @Override
    public String toString() {
        return "LearningEvent{" +
                "id=" + id +
                ", noteId=" + noteId +
                ", eventType='" + eventType + '\'' +
                ", eventDate=" + eventDate +
                ", description='" + description + '\'' +
                '}';
    }
}
