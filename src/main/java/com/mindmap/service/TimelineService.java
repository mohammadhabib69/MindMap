package com.mindmap.service;

import com.mindmap.model.LearningEventType;
import com.mindmap.model.TimelineEvent;
import com.mindmap.model.TimelineRow;
import com.mindmap.repository.LearningEventRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * Service managing learning activity history, timeline event retrieval,
 * multi-criteria filtering, and chronological date grouping.
 */
public class TimelineService {

    private static final Logger LOGGER = Logger.getLogger(TimelineService.class.getName());

    private final LearningEventRepository learningEventRepository;

    public TimelineService() {
        this(new LearningEventRepository());
    }

    public TimelineService(LearningEventRepository learningEventRepository) {
        this.learningEventRepository = Objects.requireNonNull(learningEventRepository, "learningEventRepository cannot be null");
    }

    /**
     * Retrieves all timeline events ordered chronologically descending (newest first).
     * Single JOIN query to avoid N+1.
     */
    public List<TimelineEvent> getAllTimelineEvents() {
        return learningEventRepository.findTimelineEvents();
    }

    /**
     * Retrieves filtered timeline events by event type, date range, and search query.
     */
    public List<TimelineEvent> getFilteredTimelineEvents(String eventType, LocalDate fromDate, LocalDate toDate, String searchQuery) {
        return learningEventRepository.findTimelineEventsFiltered(eventType, fromDate, toDate, searchQuery);
    }

    /**
     * Retrieves recent events up to a given limit.
     */
    public List<TimelineEvent> getRecentEvents(int limit) {
        List<TimelineEvent> all = learningEventRepository.findTimelineEvents();
        if (all.size() <= limit) {
            return all;
        }
        return all.subList(0, limit);
    }

    /**
     * Retrieves timeline events for a specific note.
     */
    public List<TimelineEvent> getEventsForNote(int noteId) {
        return learningEventRepository.findTimelineEventsByNoteId(noteId);
    }

    /**
     * Total count of learning events.
     */
    public int getTotalEventCount() {
        return learningEventRepository.countEvents();
    }

    /**
     * Count of review completion events.
     */
    public int getReviewedEventCount() {
        return learningEventRepository.countEventsByType(LearningEventType.NOTE_REVIEWED.name());
    }

    /**
     * Count of note creation and update events.
     */
    public int getNoteActivityCount() {
        return learningEventRepository.countEventsByType(LearningEventType.NOTE_CREATED.name()) +
               learningEventRepository.countEventsByType(LearningEventType.NOTE_UPDATED.name());
    }

    /**
     * Transforms a list of sorted TimelineEvent objects into virtualized TimelineRows
     * with human-readable Date Section Headers (TODAY, YESTERDAY, THIS WEEK, EARLIER).
     */
    public List<TimelineRow> buildGroupedTimelineRows(List<TimelineEvent> events) {
        List<TimelineRow> rows = new ArrayList<>();
        if (events == null || events.isEmpty()) {
            return rows;
        }

        LocalDate today = LocalDate.now();

        // Group into ordered buckets
        Map<String, List<TimelineEvent>> groups = new LinkedHashMap<>();
        groups.put("TODAY", new ArrayList<>());
        groups.put("YESTERDAY", new ArrayList<>());
        groups.put("THIS WEEK", new ArrayList<>());
        groups.put("EARLIER", new ArrayList<>());

        for (TimelineEvent event : events) {
            String section = event.getDateSection(today);
            if (!groups.containsKey(section)) {
                groups.put(section, new ArrayList<>());
            }
            groups.get(section).add(event);
        }

        for (Map.Entry<String, List<TimelineEvent>> entry : groups.entrySet()) {
            String sectionKey = entry.getKey();
            List<TimelineEvent> sectionEvents = entry.getValue();

            if (sectionEvents.isEmpty()) {
                continue;
            }

            // Determine subtitle for header
            String subtitle = switch (sectionKey) {
                case "TODAY" -> today.format(java.time.format.DateTimeFormatter.ofPattern("MMMM dd, yyyy"));
                case "YESTERDAY" -> today.minusDays(1).format(java.time.format.DateTimeFormatter.ofPattern("MMMM dd, yyyy"));
                case "THIS WEEK" -> "Past 7 days";
                default -> "Previous learning activity";
            };

            rows.add(TimelineRow.header(sectionKey, subtitle, sectionEvents.size()));

            for (int i = 0; i < sectionEvents.size(); i++) {
                TimelineEvent ev = sectionEvents.get(i);
                boolean isFirst = (i == 0);
                boolean isLast = (i == sectionEvents.size() - 1);
                rows.add(TimelineRow.event(ev, isFirst, isLast));
            }
        }

        return rows;
    }
}
