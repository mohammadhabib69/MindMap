package com.mindmap.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Polymorphic item representing a row in the virtualized Timeline ListView.
 * Can be either a Date Section Header or a Timeline Event Card.
 */
public abstract class TimelineRow {

    private static final DateTimeFormatter LONG_DATE_FORMATTER = DateTimeFormatter.ofPattern("MMMM dd, yyyy");

    public abstract boolean isHeader();

    public static TimelineHeaderRow header(String title, LocalDate date, int count) {
        String formattedDate = date != null ? date.format(LONG_DATE_FORMATTER) : "";
        return new TimelineHeaderRow(title, formattedDate, count);
    }

    public static TimelineHeaderRow header(String title, String subtitle, int count) {
        return new TimelineHeaderRow(title, subtitle, count);
    }

    public static TimelineEventRow event(TimelineEvent event, boolean isFirst, boolean isLast) {
        return new TimelineEventRow(event, isFirst, isLast);
    }

    public static class TimelineHeaderRow extends TimelineRow {
        private final String title;
        private final String subtitle;
        private final int eventCount;

        public TimelineHeaderRow(String title, String subtitle, int eventCount) {
            this.title = title;
            this.subtitle = subtitle;
            this.eventCount = eventCount;
        }

        @Override
        public boolean isHeader() {
            return true;
        }

        public String getTitle() {
            return title;
        }

        public String getSubtitle() {
            return subtitle;
        }

        public int getEventCount() {
            return eventCount;
        }
    }

    public static class TimelineEventRow extends TimelineRow {
        private final TimelineEvent event;
        private final boolean isFirstInSection;
        private final boolean isLastInSection;

        public TimelineEventRow(TimelineEvent event, boolean isFirstInSection, boolean isLastInSection) {
            this.event = event;
            this.isFirstInSection = isFirstInSection;
            this.isLastInSection = isLastInSection;
        }

        @Override
        public boolean isHeader() {
            return false;
        }

        public TimelineEvent getEvent() {
            return event;
        }

        public boolean isFirstInSection() {
            return isFirstInSection;
        }

        public boolean isLastInSection() {
            return isLastInSection;
        }
    }
}
