package com.mindmap.export.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MindMapExport {
    private int formatVersion = 1;
    private LocalDateTime exportedAt;
    private String application = "MindMap";
    
    private List<ExportNote> notes = new ArrayList<>();
    private List<ExportTag> tags = new ArrayList<>();
    private List<ExportConnection> connections = new ArrayList<>();
    private List<ExportRevision> revisions = new ArrayList<>();
    private List<ExportLearningEvent> learningEvents = new ArrayList<>();

    // Getters and Setters
    public int getFormatVersion() { return formatVersion; }
    public void setFormatVersion(int formatVersion) { this.formatVersion = formatVersion; }
    
    public LocalDateTime getExportedAt() { return exportedAt; }
    public void setExportedAt(LocalDateTime exportedAt) { this.exportedAt = exportedAt; }
    
    public String getApplication() { return application; }
    public void setApplication(String application) { this.application = application; }
    
    public List<ExportNote> getNotes() { return notes; }
    public void setNotes(List<ExportNote> notes) { this.notes = notes; }
    
    public List<ExportTag> getTags() { return tags; }
    public void setTags(List<ExportTag> tags) { this.tags = tags; }
    
    public List<ExportConnection> getConnections() { return connections; }
    public void setConnections(List<ExportConnection> connections) { this.connections = connections; }
    
    public List<ExportRevision> getRevisions() { return revisions; }
    public void setRevisions(List<ExportRevision> revisions) { this.revisions = revisions; }
    
    public List<ExportLearningEvent> getLearningEvents() { return learningEvents; }
    public void setLearningEvents(List<ExportLearningEvent> learningEvents) { this.learningEvents = learningEvents; }
}
