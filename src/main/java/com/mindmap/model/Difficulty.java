package com.mindmap.model;

/**
 * Difficulty levels for notes and learning material.
 */
public enum Difficulty {
    EASY,
    MEDIUM,
    HARD;

    /**
     * Parses a string into a Difficulty enum safely (case-insensitive).
     *
     * @param value String representation.
     * @return Matching Difficulty, or null if null or unknown.
     */
    public static Difficulty fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Difficulty.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
