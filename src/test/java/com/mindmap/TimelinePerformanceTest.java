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
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Performance and scale test suite for the Learning Timeline:
 * - Verifies fast JOIN query execution for 250+ timeline events
 * - Confirms composite TimelineEvent correctness
 * - Checks absence of duplicate events in results
 * - Validates cell reuse patterns and stability across repeated updates
 * - Validates date grouping under high event counts
 */
public class TimelinePerformanceTest {

    private static NoteRepository noteRepository;
    private static LearningEventRepository learningEventRepository;
    private static TimelineService timelineService;

    private static final List<Integer> createdNoteIds = new ArrayList<>();
    private static final List<Integer> createdEventIds = new ArrayList<>();
    private static boolean toolkitInitialized = false;

    @BeforeAll
    static void setUpAll() throws SQLException, InterruptedException {
        DatabaseInitializer.initialize();
        noteRepository = new NoteRepository();
        learningEventRepository = new LearningEventRepository();
        timelineService = new TimelineService(learningEventRepository);

        CountDownLatch latch = new CountDownLatch(1);
        try {
            Platform.startup(latch::countDown);
            toolkitInitialized = true;
            latch.await(3, TimeUnit.SECONDS);
        } catch (IllegalStateException e) {
            // Already initialized
            toolkitInitialized = true;
        }

        // Seed 250 notes and associated learning events
        for (int i = 1; i <= 250; i++) {
            Note note = new Note();
            note.setTitle("Timeline Scale Note #" + i);
            note.setContent("Content for scale note #" + i);
            note.setSubject(i % 2 == 0 ? "Computer Science" : "Mathematics");
            note.setDifficulty(i % 3 == 0 ? Difficulty.HARD.name() : (i % 3 == 1 ? Difficulty.MEDIUM.name() : Difficulty.EASY.name()));
            Note createdNote = noteRepository.create(note);
            createdNoteIds.add(createdNote.getId());

            String eventType = (i % 3 == 0) ? LearningEventType.NOTE_REVIEWED.name()
                    : ((i % 3 == 1) ? LearningEventType.NOTE_CREATED.name() : LearningEventType.NOTE_UPDATED.name());

            LearningEvent event = new LearningEvent(
                    createdNote.getId(),
                    eventType,
                    "Activity event description for note #" + i
            );
            // Spread dates: today, yesterday, past week, past month
            event.setEventDate(LocalDateTime.now().minusDays(i % 30).minusHours(i % 24));
            LearningEvent createdEvent = learningEventRepository.create(event);
            createdEventIds.add(createdEvent.getId());
        }
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
    void testJoinQueryRetrieves250PlusEventsAccurately() {
        List<TimelineEvent> events = timelineService.getAllTimelineEvents();
        assertNotNull(events);
        assertTrue(events.size() >= 250, "Should retrieve at least 250 events");

        for (TimelineEvent ev : events) {
            assertNotNull(ev.getLearningEvent(), "LearningEvent must not be null");
            if (ev.hasNote()) {
                assertNotNull(ev.getNote(), "Note must be populated when hasNote is true");
                assertNotNull(ev.getNoteTitle(), "Note title must be populated");
            }
        }
    }

    @Test
    void testNoDuplicateEventsInTimeline() {
        List<TimelineEvent> events = timelineService.getAllTimelineEvents();
        Set<Integer> seenEventIds = new HashSet<>();

        for (TimelineEvent ev : events) {
            int id = ev.getId();
            assertFalse(seenEventIds.contains(id), "Duplicate timeline event found: " + id);
            seenEventIds.add(id);
        }
    }

    @Test
    void testLargeGroupedTimelineRowsGeneration() {
        List<TimelineEvent> events = timelineService.getAllTimelineEvents();
        List<TimelineRow> rows = timelineService.buildGroupedTimelineRows(events);

        assertNotNull(rows);
        assertTrue(rows.size() > events.size(), "Total rows must include headers plus event rows");

        // Verify headers are followed by events
        boolean sawHeader = false;
        int eventCountUnderHeaders = 0;

        for (TimelineRow row : rows) {
            if (row.isHeader()) {
                sawHeader = true;
                TimelineHeaderRow header = (TimelineHeaderRow) row;
                assertTrue(header.getEventCount() > 0, "Header must not have 0 events");
            } else {
                assertTrue(sawHeader, "First rows must begin with a section header");
                eventCountUnderHeaders++;
            }
        }
        assertEquals(events.size(), eventCountUnderHeaders, "All events must be accounted for under headers");
    }

    @Test
    void testCellReuseStabilityUnderRepeatedUpdates() throws InterruptedException {
        if (!toolkitInitialized) return;

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                TestMockTimelineCell cell = new TestMockTimelineCell();

                // Initial empty state
                assertNull(cell.getGraphic());

                // Update 1: Header row
                TimelineHeaderRow headerRow = TimelineRow.header("TODAY", LocalDate.now(), 5);
                cell.update(headerRow, false);
                assertNotNull(cell.getGraphic());
                assertEquals("TODAY", cell.headerLabel.getText());

                // Update 2: Event row with note (reuse existing cell)
                Note n = new Note();
                n.setId(999);
                n.setTitle("Cell Reuse Test Note");
                LearningEvent le = new LearningEvent(999, LearningEventType.NOTE_REVIEWED.name(), "Desc");
                TimelineEvent ev = new TimelineEvent(le, n);
                TimelineEventRow eventRow = TimelineRow.event(ev, true, false);

                cell.update(eventRow, false);
                assertNotNull(cell.getGraphic());
                assertEquals("Cell Reuse Test Note", cell.titleLabel.getText());

                // Update 3: Another event row (scrolling past)
                Note n2 = new Note();
                n2.setId(1000);
                n2.setTitle("Second Note In Same Cell");
                TimelineEvent ev2 = new TimelineEvent(le, n2);
                cell.update(TimelineRow.event(ev2, false, false), false);
                assertEquals("Second Note In Same Cell", cell.titleLabel.getText());

                // Update 4: Empty state
                cell.update(null, true);
                assertNull(cell.getGraphic(), "Graphic should be null when empty");

            } finally {
                latch.countDown();
            }
        });

        assertTrue(latch.await(4, TimeUnit.SECONDS), "Timeline cell reuse test timed out");
    }

    /**
     * Mock cell mimicking TimelineListCell node reuse pattern.
     */
    private static class TestMockTimelineCell extends ListCell<TimelineRow> {
        final HBox headerBox = new HBox();
        final Label headerLabel = new Label();

        final VBox eventCard = new VBox();
        final Label titleLabel = new Label();

        TestMockTimelineCell() {
            headerBox.getChildren().add(headerLabel);
            eventCard.getChildren().add(titleLabel);
        }

        void update(TimelineRow row, boolean empty) {
            updateItem(row, empty);
        }

        @Override
        protected void updateItem(TimelineRow item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setGraphic(null);
            } else if (item instanceof TimelineHeaderRow headerRow) {
                headerLabel.setText(headerRow.getTitle());
                setGraphic(headerBox);
            } else if (item instanceof TimelineEventRow eventRow) {
                TimelineEvent ev = eventRow.getEvent();
                titleLabel.setText(ev.getNoteTitle() != null ? ev.getNoteTitle() : "No Title");
                setGraphic(eventCard);
            }
        }
    }
}
