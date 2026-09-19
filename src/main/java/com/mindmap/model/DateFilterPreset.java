package com.mindmap.model;

/**
 * Presets for filtering notes by date ranges.
 */
public enum DateFilterPreset {
    ALL_TIME("All Dates"),
    TODAY("Today"),
    LAST_7_DAYS("Last 7 Days"),
    LAST_30_DAYS("Last 30 Days"),
    CUSTOM("Custom Range");

    private final String displayName;

    DateFilterPreset(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static DateFilterPreset fromDisplayName(String text) {
        if (text == null) {
            return ALL_TIME;
        }
        for (DateFilterPreset preset : values()) {
            if (preset.displayName.equalsIgnoreCase(text.trim()) || preset.name().equalsIgnoreCase(text.trim())) {
                return preset;
            }
        }
        return ALL_TIME;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
