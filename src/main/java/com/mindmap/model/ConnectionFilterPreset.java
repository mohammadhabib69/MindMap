package com.mindmap.model;

/**
 * Presets for filtering notes by connection status.
 */
public enum ConnectionFilterPreset {
    ALL("All Notes"),
    HAS_CONNECTIONS("Has Connections"),
    NO_CONNECTIONS("No Connections (Orphan)");

    private final String displayName;

    ConnectionFilterPreset(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static ConnectionFilterPreset fromDisplayName(String text) {
        if (text == null) {
            return ALL;
        }
        for (ConnectionFilterPreset preset : values()) {
            if (preset.displayName.equalsIgnoreCase(text.trim()) || preset.name().equalsIgnoreCase(text.trim())) {
                return preset;
            }
        }
        return ALL;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
