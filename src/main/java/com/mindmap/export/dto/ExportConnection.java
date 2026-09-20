package com.mindmap.export.dto;

public class ExportConnection {
    private String fromNoteExportId;
    private String toNoteExportId;
    private String relation;

    // Getters and Setters
    public String getFromNoteExportId() { return fromNoteExportId; }
    public void setFromNoteExportId(String fromNoteExportId) { this.fromNoteExportId = fromNoteExportId; }
    
    public String getToNoteExportId() { return toNoteExportId; }
    public void setToNoteExportId(String toNoteExportId) { this.toNoteExportId = toNoteExportId; }
    
    public String getRelation() { return relation; }
    public void setRelation(String relation) { this.relation = relation; }
}
