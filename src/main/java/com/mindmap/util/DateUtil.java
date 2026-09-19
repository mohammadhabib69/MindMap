package com.mindmap.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utility for formatting and parsing ISO-8601 dates and date-times.
 */
public final class DateUtil {

    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    private DateUtil() {
        // Prevent instantiation
    }

    /**
     * Formats a LocalDateTime into an ISO-8601 string (e.g. 2026-09-19T10:30:00).
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATE_TIME_FORMATTER) : null;
    }

    /**
     * Parses an ISO-8601 string into a LocalDateTime.
     */
    public static LocalDateTime parseDateTime(String text) {
        return (text != null && !text.trim().isEmpty()) ? LocalDateTime.parse(text.trim(), DATE_TIME_FORMATTER) : null;
    }

    /**
     * Formats a LocalDate into an ISO-8601 string (e.g. 2026-09-19).
     */
    public static String formatDate(LocalDate date) {
        return date != null ? date.format(DATE_FORMATTER) : null;
    }

    /**
     * Parses an ISO-8601 string into a LocalDate.
     */
    public static LocalDate parseDate(String text) {
        return (text != null && !text.trim().isEmpty()) ? LocalDate.parse(text.trim(), DATE_FORMATTER) : null;
    }
}
