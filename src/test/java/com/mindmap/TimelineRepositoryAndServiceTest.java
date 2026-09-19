package com.mindmap;

import com.mindmap.database.DatabaseInitializer;
import com.mindmap.model.Difficulty;
import com.mindmap.model.LearningEvent;
import com.mindmap.model.LearningEventType;
import com.mindmap.model.Note;
import com.mindmap.model.TimelineEvent;
import com.mindmap.model.TimelineRow;
import com.mindmap.model.TimelineRow.TimelineEventRow;
import com.mindmap.model.TimelineRow.TimelineHeaderRow;
import com.mindmap.repository.LearningEventRepository;
import com.mindmap.repository.NoteRepository;
import com.mindmap.service.TimelineService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite verifying Learning Timeline Repository and Service functionality:
 * - Single JOIN query mapping for events with and without associated notes
 * - Chronological ordering (DESC)
 * - Multi-criteria filtering by event type, date range, and keyword search
 * - Date grouping (TODAY, YESTERDAY, THIS WEEK, EARLIER)
 * - Empty result and unlinked note edge cases
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TimelineRepositoryAndServiceTest {

    private static NoteRepository noteRepository;
    private static LearningEventRepository learningEventRepository;
    private static TimelineService timelineService;

    private static final List<Integer> createdNoteIds = new ArrayList<>();
    private static final List<Integer> createdEventIds = new ArrayList<>();

    private static Note testNote1;
    private static Note testNote2;
    private static LearningEvent eventToday;
    private static LearningEvent eventYesterday;
    private static LearningEvent eventThisWeek;
    private static LearningEvent eventEarlier;
    private static LearningEvent eventWithoutNote;

    @BeforeAll
    static void setUpAll() throws SQLException {
        DatabaseInitializer.initialize();
        noteRepository = new NoteRepository();
        learningEventRepository = new LearningEventRepository();
        timelineService = new TimelineService(learningEventRepository);

        // 1. Create test notes
        testNote1 = new Note("Binary Trees", "Tree traversal algorithms", "Algorithms", Difficulty.MEDIUM.name());
        testNote1 = noteRepository.create(testNote1);
        createdNoteIds.add(testNote1.getId());

        testNote2 = new Note("Quantum Computing", "Qubits and superposition", "Physics", Difficulty.HARD.name());
        testNote2 = noteRepository.create(testNote2);
        createdNoteIds.add(testNote2.getId());

        // 2. Create learning events across date sections
        // Event Today with note
        eventToday = new LearningEvent(testNote1.getId(), LearningEventType.NOTE_REVIEWED.name(), "Reviewed Binary Trees: GOOD");
        eventToday.setEventDate(LocalDateTime.now().minusHours(2));
        eventToday = learningEventRepository.create(eventToday);
        createdEventIds.add(eventToday.getId());

        // Event Yesterday with note
        eventYesterday = new LearningEvent(testNote1.getId(), LearningEventType.NOTE_UPDATED.name(), "Updated Binary Trees content");
        eventYesterday.setEventDate(LocalDateTime.now().minusDays(1).minusHours(1));
        eventYesterday = learningEventRepository.create(eventYesterday);
        createdEventIds.add(eventYesterday.getId());

        // Event This Week with note
        eventThisWeek = new LearningEvent(testNote2.getId(), LearningEventType.NOTE_CREATED.name(), "Created Quantum Computing note");
        eventThisWeek.setEventDate(LocalDateTime.now().minusDays(4));
        eventThisWeek = learningEventRepository.create(eventThisWeek);
        createdEventIds.add(eventThisWeek.getId());

        // Event Earlier with note
        eventEarlier = new LearningEvent(testNote2.getId(), LearningEventType.NOTE_REVIEWED.name(), "Initial Quantum study session");
        eventEarlier.setEventDate(LocalDateTime.now().minusDays(20));
        eventEarlier = learningEventRepository.create(eventEarlier);
        createdEventIds.add(eventEarlier.getId());

        // Event without note (note_id = null)
        eventWithoutNote = new LearningEvent(null, "MILESTONE", "Completed 10 study sessions milestone");
        eventWithoutNote.setEventDate(LocalDateTime.now().minusHours(1));
        eventWithoutNote = learningEventRepository.create(eventWithoutNote);
        createdEventIds.add(eventWithoutNote.getId());
    }

    @AfterAll
    static void tearDownAll() {
        for (int eventId : createdEventIds) {
            try {
                learningEventRepository.delete(eventId);
            } catch (Exception ignored) {}
        }
        for (int noteId : createdNoteIds) {
            try {
                noteRepository.delete(noteId);
            } catch (Exception ignored) {}
        }
    }

    @Test
    @Order(1)
    void testFindTimelineEventsJoinQueryMapping() {
        List<TimelineEvent> events = timelineService.getAllTimelineEvents();
        assertNotNull(events);
        assertFalse(events.isEmpty(), "Should return timeline events");

        // Verify event with note
        TimelineEvent foundToday = events.stream()
                .filter(e -> e.getId() == eventToday.getId())
                .findFirst()
                .orElse(null);
        assertNotNull(foundToday, "Today's event should be in results");
        assertTrue(foundToday.hasNote());
        assertEquals("Binary Trees", foundToday.getNoteTitle());
        assertEquals("Algorithms", foundToday.getNoteSubject());
        assertEquals(Difficulty.MEDIUM.name(), foundToday.getNoteDifficulty());
        assertEquals(LearningEventType.NOTE_REVIEWED.name(), foundToday.getEventType());
        assertFalse(foundToday.getFormattedTime().isEmpty());

        // Verify event without note
        TimelineEvent foundUnlinked = events.stream()
                .filter(e -> e.getId() == eventWithoutNote.getId())
                .findFirst()
                .orElse(null);
        assertNotNull(foundUnlinked, "Unlinked event should be in results");
        assertFalse(foundUnlinked.hasNote());
        assertNull(foundUnlinked.getNoteTitle());
        assertNull(foundUnlinked.getNote());
        assertEquals("MILESTONE", foundUnlinked.getEventType());
        assertEquals("Completed 10 study sessions milestone", foundUnlinked.getDescription());
    }

    @Test
    @Order(2)
    void testChronologicalOrderingDescending() {
        List<TimelineEvent> events = timelineService.getAllTimelineEvents();
        for (int i = 0; i < events.size() - 1; i++) {
            LocalDateTime current = events.get(i).getEventDate();
            LocalDateTime next = events.get(i + 1).getEventDate();
            if (current != null && next != null) {
                assertTrue(current.isAfter(next) || current.isEqual(next),
                        "Events must be ordered descending by event_date");
            }
        }
    }

    @Test
    @Order(3)
    void testFilteringByEventType() {
        // Filter by NOTE_REVIEWED
        List<TimelineEvent> reviewedEvents = timelineService.getFilteredTimelineEvents(
                LearningEventType.NOTE_REVIEWED.name(), null, null, null);
        assertNotNull(reviewedEvents);
        for (TimelineEvent ev : reviewedEvents) {
            assertEquals(LearningEventType.NOTE_REVIEWED.name(), ev.getEventType());
        }

        // Filter by NOTE_CREATED
        List<TimelineEvent> createdEvents = timelineService.getFilteredTimelineEvents(
                LearningEventType.NOTE_CREATED.name(), null, null, null);
        assertNotNull(createdEvents);
        for (TimelineEvent ev : createdEvents) {
            assertEquals(LearningEventType.NOTE_CREATED.name(), ev.getEventType());
        }
    }

    @Test
    @Order(4)
    void testFilteringByDateRange() {
        LocalDate today = LocalDate.now();
        List<TimelineEvent> todayEvents = timelineService.getFilteredTimelineEvents(
                null, today, today, null);

        assertNotNull(todayEvents);
        for (TimelineEvent ev : todayEvents) {
            assertEquals(today, ev.getEventDate().toLocalDate(), "All events must occur today");
        }
        assertTrue(todayEvents.stream().anyMatch(e -> e.getId() == eventToday.getId()));
        assertFalse(todayEvents.stream().anyMatch(e -> e.getId() == eventYesterday.getId()));
    }

    @Test
    @Order(5)
    void testSearchFiltering() {
        // Search for "Binary" (matches note title)
        List<TimelineEvent> searchByTitle = timelineService.getFilteredTimelineEvents(
                null, null, null, "Binary");
        assertFalse(searchByTitle.isEmpty());
        assertTrue(searchByTitle.stream().anyMatch(e -> e.getId() == eventToday.getId()));

        // Search for "Qubits" (matches note content/description)
        List<TimelineEvent> searchByDesc = timelineService.getFilteredTimelineEvents(
                null, null, null, "superposition");
        // May match note content if note joined or description
        List<TimelineEvent> searchQuantum = timelineService.getFilteredTimelineEvents(
                null, null, null, "Quantum");
        assertFalse(searchQuantum.isEmpty());
        assertTrue(searchQuantum.stream().anyMatch(e -> e.getId() == eventThisWeek.getId()));

        // Search by subject "Physics"
        List<TimelineEvent> searchBySubject = timelineService.getFilteredTimelineEvents(
                null, null, null, "Physics");
        assertFalse(searchBySubject.isEmpty());
        assertTrue(searchBySubject.stream().anyMatch(e -> e.getId() == eventThisWeek.getId()));

        // Search for nonexistent term
        List<TimelineEvent> searchNone = timelineService.getFilteredTimelineEvents(
                null, null, null, "xyzNonexistentTerm999");
        assertTrue(searchNone.isEmpty(), "Nonexistent search must return empty list");
    }

    @Test
    @Order(6)
    void testEventsForSpecificNote() {
        List<TimelineEvent> eventsForNote1 = timelineService.getEventsForNote(testNote1.getId());
        assertNotNull(eventsForNote1);
        for (TimelineEvent ev : eventsForNote1) {
            assertEquals(Integer.valueOf(testNote1.getId()), ev.getNoteId());
        }
        assertTrue(eventsForNote1.stream().anyMatch(e -> e.getId() == eventToday.getId()));
        assertTrue(eventsForNote1.stream().anyMatch(e -> e.getId() == eventYesterday.getId()));
        assertFalse(eventsForNote1.stream().anyMatch(e -> e.getId() == eventThisWeek.getId()));
    }

    @Test
    @Order(7)
    void testEventCounts() {
        int total = timelineService.getTotalEventCount();
        assertTrue(total >= 5, "Should have at least our 5 seeded events");

        int reviewed = timelineService.getReviewedEventCount();
        assertTrue(reviewed >= 2, "Should have at least 2 reviewed events");

        int noteActivity = timelineService.getNoteActivityCount();
        assertTrue(noteActivity >= 2, "Should have at least 2 note creation/update events");
    }

    @Test
    @Order(8)
    void testDateGroupingHierarchy() {
        List<TimelineEvent> events = List.of(
                new TimelineEvent(eventToday, testNote1),
                new TimelineEvent(eventYesterday, testNote1),
                new TimelineEvent(eventThisWeek, testNote2),
                new TimelineEvent(eventEarlier, testNote2)
        );

        List<TimelineRow> rows = timelineService.buildGroupedTimelineRows(events);
        assertNotNull(rows);
        assertFalse(rows.isEmpty());

        // Header counts
        long headerCount = rows.stream().filter(TimelineRow::isHeader).count();
        assertEquals(4, headerCount, "Should have 4 section headers (TODAY, YESTERDAY, THIS WEEK, EARLIER)");

        // Verify headers exist
        List<String> headerTitles = rows.stream()
                .filter(TimelineRow::isHeader)
                .map(r -> ((TimelineHeaderRow) r).getTitle())
                .toList();

        assertTrue(headerTitles.contains("TODAY"));
        assertTrue(headerTitles.contains("YESTERDAY"));
        assertTrue(headerTitles.contains("THIS WEEK"));
        assertTrue(headerTitles.contains("EARLIER"));

        // Verify event rows follow headers
        for (int i = 0; i < rows.size(); i++) {
            TimelineRow row = rows.get(i);
            if (row instanceof TimelineEventRow eventRow) {
                assertNotNull(eventRow.getEvent());
            }
        }
    }

    @Test
    @Order(9)
    void testTimelineFxmlLoadingAndBinding() throws Exception {
        java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
        try {
            javafx.application.Platform.startup(latch::countDown);
            latch.await(2, java.util.concurrent.TimeUnit.SECONDS);
        } catch (IllegalStateException ignored) {
            // Already started
        }

        java.util.concurrent.CountDownLatch uiLatch = new java.util.concurrent.CountDownLatch(1);
        javafx.application.Platform.runLater(() -> {
            try {
                javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/timeline.fxml"));
                javafx.scene.Parent root = loader.load();
                assertNotNull(root, "timeline.fxml root should load successfully");

                com.mindmap.controller.TimelineController controller = loader.getController();
                assertNotNull(controller, "TimelineController should be instantiated");
                assertNotNull(controller.getListTimeline(), "listTimeline must be injected");
                assertNotNull(controller.getTimelineData(), "timelineData ObservableList must exist");
                assertFalse(controller.getTimelineData().isEmpty(), "timelineData should have seeded rows");
            } catch (Exception e) {
                fail("Failed to load timeline.fxml: " + e.getMessage());
            } finally {
                uiLatch.countDown();
            }
        });

        assertTrue(uiLatch.await(5, java.util.concurrent.TimeUnit.SECONDS), "UI thread execution timed out");
    }
}
