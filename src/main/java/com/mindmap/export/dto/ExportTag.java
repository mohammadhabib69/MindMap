package com.mindmap.export.dto;

public class ExportTag {
    private String name;

    public ExportTag() {}

    public ExportTag(String name) {
        this.name = name;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
