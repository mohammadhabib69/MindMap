package com.mindmap.model;

/**
 * Types of chronological learning events tracked across the application.
 */
public enum LearningEventType {
    NOTE_CREATED,
    NOTE_UPDATED,
    NOTE_REVIEWED;

    /**
     * Parses a string into a LearningEventType enum safely (case-insensitive).
     *
     * @param value String representation.
     * @return Matching LearningEventType, or null if null or unknown.
     */
    public static LearningEventType fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return LearningEventType.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
