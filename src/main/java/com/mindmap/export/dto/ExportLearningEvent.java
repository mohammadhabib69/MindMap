package com.mindmap.export.dto;

import java.time.LocalDateTime;

public class ExportLearningEvent {
    private String noteExportId; // can be null
    private String eventType;
    private LocalDateTime eventDate;
    private String description;

    // Getters and Setters
    public String getNoteExportId() { return noteExportId; }
    public void setNoteExportId(String noteExportId) { this.noteExportId = noteExportId; }
    
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    
    public LocalDateTime getEventDate() { return eventDate; }
    public void setEventDate(LocalDateTime eventDate) { this.eventDate = eventDate; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
