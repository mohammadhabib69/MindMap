package com.mindmap.model;

import java.time.LocalDate;

/**
 * Encapsulates multi-criteria parameters for searching and filtering notes.
 */
public class SearchCriteria {

    public static final String ALL_SUBJECTS = "All Subjects";
    public static final String ALL_DIFFICULTIES = "All Difficulties";
    public static final String ALL_TAGS = "All Tags";

    private String query;
    private String subject;
    private String difficulty;
    private String tag;
    private DateFilterPreset dateFilter = DateFilterPreset.ALL_TIME;
    private LocalDate customStartDate;
    private LocalDate customEndDate;
    private ConnectionFilterPreset connectionFilter = ConnectionFilterPreset.ALL;

    public SearchCriteria() {
    }

    public SearchCriteria(String query) {
        this.query = query;
    }

    public boolean hasQuery() {
        return query != null && !query.trim().isEmpty();
    }

    public boolean hasSubject() {
        return subject != null && !subject.trim().isEmpty() && !ALL_SUBJECTS.equalsIgnoreCase(subject.trim());
    }

    public boolean hasDifficulty() {
        return difficulty != null && !difficulty.trim().isEmpty() && !ALL_DIFFICULTIES.equalsIgnoreCase(difficulty.trim());
    }

    public boolean hasTag() {
        return tag != null && !tag.trim().isEmpty() && !ALL_TAGS.equalsIgnoreCase(tag.trim());
    }

    public boolean hasDateFilter() {
        return dateFilter != null && dateFilter != DateFilterPreset.ALL_TIME;
    }

    public boolean hasConnectionFilter() {
        return connectionFilter != null && connectionFilter != ConnectionFilterPreset.ALL;
    }

    public boolean isEmpty() {
        return !hasQuery() && !hasSubject() && !hasDifficulty() && !hasTag() && !hasDateFilter() && !hasConnectionFilter();
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public SearchCriteria withQuery(String query) {
        this.query = query;
        return this;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public SearchCriteria withSubject(String subject) {
        this.subject = subject;
        return this;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public SearchCriteria withDifficulty(String difficulty) {
        this.difficulty = difficulty;
        return this;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public SearchCriteria withTag(String tag) {
        this.tag = tag;
        return this;
    }

    public DateFilterPreset getDateFilter() {
        return dateFilter;
    }

    public void setDateFilter(DateFilterPreset dateFilter) {
        this.dateFilter = (dateFilter != null) ? dateFilter : DateFilterPreset.ALL_TIME;
    }

    public SearchCriteria withDateFilter(DateFilterPreset dateFilter) {
        setDateFilter(dateFilter);
        return this;
    }

    public LocalDate getCustomStartDate() {
        return customStartDate;
    }

    public void setCustomStartDate(LocalDate customStartDate) {
        this.customStartDate = customStartDate;
    }

    public SearchCriteria withCustomStartDate(LocalDate customStartDate) {
        this.customStartDate = customStartDate;
        return this;
    }

    public LocalDate getCustomEndDate() {
        return customEndDate;
    }

    public void setCustomEndDate(LocalDate customEndDate) {
        this.customEndDate = customEndDate;
    }

    public SearchCriteria withCustomEndDate(LocalDate customEndDate) {
        this.customEndDate = customEndDate;
        return this;
    }

    public ConnectionFilterPreset getConnectionFilter() {
        return connectionFilter;
    }

    public void setConnectionFilter(ConnectionFilterPreset connectionFilter) {
        this.connectionFilter = (connectionFilter != null) ? connectionFilter : ConnectionFilterPreset.ALL;
    }

    public SearchCriteria withConnectionFilter(ConnectionFilterPreset connectionFilter) {
        setConnectionFilter(connectionFilter);
        return this;
    }

    @Override
    public String toString() {
        return "SearchCriteria{" +
                "query='" + query + '\'' +
                ", subject='" + subject + '\'' +
                ", difficulty='" + difficulty + '\'' +
                ", tag='" + tag + '\'' +
                ", dateFilter=" + dateFilter +
                ", customStartDate=" + customStartDate +
                ", customEndDate=" + customEndDate +
                ", connectionFilter=" + connectionFilter +
                '}';
    }
}
