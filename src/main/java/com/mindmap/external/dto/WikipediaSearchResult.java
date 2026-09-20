package com.mindmap.external.dto;

public class WikipediaSearchResult {
    private String title;
    private String snippet;
    private String pageUrl;

    public WikipediaSearchResult() {}

    public WikipediaSearchResult(String title, String snippet, String pageUrl) {
        this.title = title;
        this.snippet = snippet;
        this.pageUrl = pageUrl;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getSnippet() { return snippet; }
    public void setSnippet(String snippet) { this.snippet = snippet; }

    public String getPageUrl() { return pageUrl; }
    public void setPageUrl(String pageUrl) { this.pageUrl = pageUrl; }
    
    @Override
    public String toString() {
        return title;
    }
}
