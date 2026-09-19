package com.mindmap.model;

/**
 * Status of a revision session.
 */
public enum RevisionStatus {
    PENDING,
    COMPLETED,
    SKIPPED;

    /**
     * Parses a string into a RevisionStatus enum safely (case-insensitive).
     *
     * @param value String representation.
     * @return Matching RevisionStatus, or null if null or unknown.
     */
    public static RevisionStatus fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return RevisionStatus.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
