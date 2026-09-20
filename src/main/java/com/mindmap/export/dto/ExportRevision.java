package com.mindmap.export.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ExportRevision {
    private String noteExportId;
    private LocalDate reviewDate;
    private String status;
    private int intervalDays;
    private LocalDateTime createdAt;

    // Getters and Setters
    public String getNoteExportId() { return noteExportId; }
    public void setNoteExportId(String noteExportId) { this.noteExportId = noteExportId; }
    
    public LocalDate getReviewDate() { return reviewDate; }
    public void setReviewDate(LocalDate reviewDate) { this.reviewDate = reviewDate; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public int getIntervalDays() { return intervalDays; }
    public void setIntervalDays(int intervalDays) { this.intervalDays = intervalDays; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
