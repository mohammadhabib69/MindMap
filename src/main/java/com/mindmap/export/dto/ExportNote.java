package com.mindmap.export.dto;

import java.time.LocalDateTime;
import java.util.List;

public class ExportNote {
    private String exportId;
    private String title;
    private String content;
    private String subject;
    private String difficulty;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<String> tags;

    // Getters and Setters
    public String getExportId() { return exportId; }
    public void setExportId(String exportId) { this.exportId = exportId; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    
    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
}
