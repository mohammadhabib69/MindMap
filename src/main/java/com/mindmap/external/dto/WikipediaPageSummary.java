package com.mindmap.external.dto;

public class WikipediaPageSummary {
    private String title;
    private String extract;
    private String pageUrl;
    private String thumbnailUrl;

    public WikipediaPageSummary() {}

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getExtract() { return extract; }
    public void setExtract(String extract) { this.extract = extract; }

    public String getPageUrl() { return pageUrl; }
    public void setPageUrl(String pageUrl) { this.pageUrl = pageUrl; }

    public String getThumbnailUrl() { return thumbnailUrl; }
    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }
}
