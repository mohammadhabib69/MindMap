package com.mindmap.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Composite model representing a Timeline Event:
 * encapsulates a LearningEvent and its associated Note (if available).
 */
public class TimelineEvent {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("hh:mm a");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy");

    private final LearningEvent learningEvent;
    private final Note note; // Nullable: note may be deleted or event may be a general milestone

    public TimelineEvent(LearningEvent learningEvent, Note note) {
        this.learningEvent = Objects.requireNonNull(learningEvent, "learningEvent cannot be null");
        this.note = note;
    }

    public LearningEvent getLearningEvent() {
        return learningEvent;
    }

    public Note getNote() {
        return note;
    }

    public boolean hasNote() {
        return note != null && note.getId() > 0;
    }

    public int getId() {
        return learningEvent.getId();
    }

    public Integer getNoteId() {
        return learningEvent.getNoteId();
    }

    public String getEventType() {
        return learningEvent.getEventType();
    }

    public LocalDateTime getEventDate() {
        return learningEvent.getEventDate();
    }

    public String getDescription() {
        return learningEvent.getDescription();
    }

    public String getNoteTitle() {
        return note != null ? note.getTitle() : null;
    }

    public String getNoteSubject() {
        return note != null ? note.getSubject() : null;
    }

    public String getNoteDifficulty() {
        return note != null ? note.getDifficulty() : null;
    }

    public String getFormattedTime() {
        if (learningEvent.getEventDate() != null) {
            return learningEvent.getEventDate().format(TIME_FORMATTER);
        }
        return "";
    }

    public String getFormattedDate() {
        if (learningEvent.getEventDate() != null) {
            return learningEvent.getEventDate().format(DATE_FORMATTER);
        }
        return "";
    }

    /**
     * Determines human-readable date section (TODAY, YESTERDAY, THIS WEEK, EARLIER)
     * relative to a reference date (usually today).
     */
    public String getDateSection(LocalDate referenceDate) {
        if (learningEvent.getEventDate() == null) {
            return "UNKNOWN";
        }
        LocalDate date = learningEvent.getEventDate().toLocalDate();
        if (date.equals(referenceDate)) {
            return "TODAY";
        } else if (date.equals(referenceDate.minusDays(1))) {
            return "YESTERDAY";
        } else if (date.isAfter(referenceDate.minusDays(7)) && date.isBefore(referenceDate)) {
            return "THIS WEEK";
        } else {
            return "EARLIER";
        }
    }

    public String getDateSection() {
        return getDateSection(LocalDate.now());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TimelineEvent that = (TimelineEvent) o;
        return Objects.equals(learningEvent, that.learningEvent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(learningEvent);
    }

    @Override
    public String toString() {
        return "TimelineEvent{" +
                "id=" + getId() +
                ", eventType='" + getEventType() + '\'' +
                ", eventDate=" + getEventDate() +
                ", noteTitle='" + getNoteTitle() + '\'' +
                '}';
    }
}
