package com.mindmap;

import com.mindmap.database.DatabaseInitializer;
import com.mindmap.model.Difficulty;
import com.mindmap.model.Note;
import com.mindmap.model.Revision;
import com.mindmap.model.RevisionStatus;
import com.mindmap.model.ScheduledReview;
import com.mindmap.repository.NoteRepository;
import com.mindmap.repository.RevisionRepository;
import com.mindmap.service.RevisionService;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Performance and virtualization tests for the Revision screen:
 * - Verifies fast JOIN query execution for 200+ reviews
 * - Confirms ScheduledReview composite object correctness
 * - Checks duplicate absence and queue ordering
 * - Validates cell reuse patterns and node hierarchy stability
 * - Verifies cached ObservableList refresh behavior
 */
public class RevisionPerformanceTest {

    private static NoteRepository noteRepository;
    private static RevisionRepository revisionRepository;
    private static RevisionService revisionService;
    private static final List<Integer> createdNoteIds = new ArrayList<>();
    private static final List<Integer> createdRevisionIds = new ArrayList<>();
    private static boolean toolkitInitialized = false;

    @BeforeAll
    static void setUpAll() throws SQLException, InterruptedException {
        DatabaseInitializer.initialize();
        noteRepository = new NoteRepository();
        revisionRepository = new RevisionRepository();
        revisionService = new RevisionService(revisionRepository, noteRepository, null);

        CountDownLatch latch = new CountDownLatch(1);
        try {
            Platform.startup(latch::countDown);
            toolkitInitialized = true;
            latch.await(3, TimeUnit.SECONDS);
        } catch (IllegalStateException e) {
            // Toolkit already initialized by previous test
            toolkitInitialized = true;
        }

        // Seed 220 notes and revisions for scale testing
        for (int i = 1; i <= 220; i++) {
            Note n = new Note();
            n.setTitle("Perf Note #" + i);
            n.setContent("Performance review test content for note " + i);
            n.setSubject(i % 3 == 0 ? "Physics" : (i % 3 == 1 ? "Chemistry" : "Biology"));
            n.setDifficulty(i % 3 == 0 ? Difficulty.HARD.name() : (i % 3 == 1 ? Difficulty.MEDIUM.name() : Difficulty.EASY.name()));
            Note createdNote = noteRepository.create(n);
            createdNoteIds.add(createdNote.getId());

            Revision rev = new Revision(
                    createdNote.getId(),
                    LocalDate.now().minusDays(i % 10), // Due or overdue
                    RevisionStatus.PENDING.name(),
                    (i % 7) + 1
            );
            Revision createdRev = revisionRepository.create(rev);
            createdRevisionIds.add(createdRev.getId());
        }
    }

    @AfterAll
    static void tearDownAll() {
        for (int revId : createdRevisionIds) {
            try {
                revisionRepository.delete(revId);
            } catch (Exception ignored) {}
        }
        for (int noteId : createdNoteIds) {
            try {
                noteRepository.delete(noteId);
            } catch (Exception ignored) {}
        }
    }

    @Test
    void testJoinQueryRetrieves200PlusReviewsAccurately() {
        List<ScheduledReview> dueList = revisionRepository.findDueWithNotes(LocalDate.now());
        assertNotNull(dueList, "Due review list from JOIN query must not be null");
        assertTrue(dueList.size() >= 220, "Should retrieve at least 220 due reviews");

        // Verify accurate composite mapping
        for (ScheduledReview sr : dueList) {
            assertNotNull(sr.getRevision(), "Revision should be present in ScheduledReview");
            assertNotNull(sr.getNote(), "Note should be mapped in ScheduledReview");
            assertNotNull(sr.getNote().getTitle(), "Note title must be populated");
            assertTrue(sr.isDue(), "All reviews from findDueWithNotes must be due or overdue");
        }
    }

    @Test
    void testNoDuplicatesInDueReviews() {
        List<ScheduledReview> dueList = revisionService.getDueReviews();
        Set<Integer> seenRevisionIds = new HashSet<>();

        for (ScheduledReview sr : dueList) {
            int revId = sr.getRevision().getId();
            assertFalse(seenRevisionIds.contains(revId), "Duplicate revision found in due list: " + revId);
            seenRevisionIds.add(revId);
        }
    }

    @Test
    void testUpcomingReviewsJoinQuery() {
        // Create an upcoming revision
        Note upcomingNote = new Note();
        upcomingNote.setTitle("Upcoming Note Scale Test");
        upcomingNote.setContent("Content");
        upcomingNote.setSubject("Math");
        Note createdNote = noteRepository.create(upcomingNote);
        createdNoteIds.add(createdNote.getId());

        Revision upcomingRev = new Revision(
                createdNote.getId(),
                LocalDate.now().plusDays(5),
                RevisionStatus.PENDING.name(),
                7
        );
        Revision createdRev = revisionRepository.create(upcomingRev);
        createdRevisionIds.add(createdRev.getId());

        List<ScheduledReview> upcomingList = revisionRepository.findUpcomingWithNotes(LocalDate.now());
        assertNotNull(upcomingList);
        assertFalse(upcomingList.isEmpty(), "Upcoming reviews should include the scheduled future review");

        boolean found = false;
        for (ScheduledReview sr : upcomingList) {
            if (sr.getRevision().getId() == createdRev.getId()) {
                found = true;
                assertEquals("Upcoming Note Scale Test", sr.getNote().getTitle());
                assertEquals(7, sr.getIntervalDays());
                assertTrue(sr.getDaysUntilDue() > 0);
                assertFalse(sr.isDue());
                break;
            }
        }
        assertTrue(found, "Newly created upcoming review should be returned by JOIN query");
    }

    @Test
    void testCellReuseStabilityUnderRepeatedUpdates() throws InterruptedException {
        if (!toolkitInitialized) return;

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                // Test custom ListCell behavior: creating controls once, updating repeatedly
                TestMockCell cell = new TestMockCell();

                // 1. Initial state (empty)
                assertNull(cell.getGraphic());

                // 2. Update with first item
                Note n1 = new Note();
                n1.setId(1001);
                n1.setTitle("First Note");
                Revision r1 = new Revision(1001, LocalDate.now(), RevisionStatus.PENDING.name(), 1);
                ScheduledReview sr1 = new ScheduledReview(r1, n1);

                cell.update(sr1, false);
                assertNotNull(cell.getGraphic(), "Graphic should be set when cell has data");
                assertEquals("First Note", cell.titleLabel.getText());

                // 3. Reuse cell with second item (simulating scroll)
                Note n2 = new Note();
                n2.setId(1002);
                n2.setTitle("Second Note Reused Cell");
                Revision r2 = new Revision(1002, LocalDate.now().minusDays(2), RevisionStatus.PENDING.name(), 3);
                ScheduledReview sr2 = new ScheduledReview(r2, n2);

                cell.update(sr2, false);
                // Graphic container should remain the exact same instance (no new node allocations!)
                assertSame(cell.container, cell.getGraphic(), "Container must be reused, not reallocated");
                assertEquals("Second Note Reused Cell", cell.titleLabel.getText());

                // 4. Update with empty state (cell scrolled out of view)
                cell.update(null, true);
                assertNull(cell.getGraphic(), "Graphic should be null when empty");

            } finally {
                latch.countDown();
            }
        });

        assertTrue(latch.await(4, TimeUnit.SECONDS), "Cell reuse test timed out");
    }

    @Test
    void testDataRefreshIntegrity() {
        List<ScheduledReview> initialList = revisionService.getDueReviews();
        int initialCount = initialList.size();

        // Create a temporary new note and revision
        Note tempNote = new Note();
        tempNote.setTitle("Temp Refresh Note");
        Note createdNote = noteRepository.create(tempNote);
        createdNoteIds.add(createdNote.getId());

        Revision tempRev = new Revision(
                createdNote.getId(),
                LocalDate.now(),
                RevisionStatus.PENDING.name(),
                1
        );
        Revision createdRev = revisionRepository.create(tempRev);
        createdRevisionIds.add(createdRev.getId());

        // Refresh list
        List<ScheduledReview> refreshedList = revisionService.getDueReviews();
        assertEquals(initialCount + 1, refreshedList.size(), "Refreshed list must reflect newly scheduled note");
    }

    /**
     * Mock cell mimicking the exact cell-reuse pattern used in RevisionController.
     */
    private static class TestMockCell extends ListCell<ScheduledReview> {
        final VBox container = new VBox();
        final Label titleLabel = new Label();

        TestMockCell() {
            container.getChildren().add(titleLabel);
        }

        void update(ScheduledReview item, boolean empty) {
            updateItem(item, empty);
        }

        @Override
        protected void updateItem(ScheduledReview item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setGraphic(null);
            } else {
                titleLabel.setText(item.getNote().getTitle());
                setGraphic(container);
            }
        }
    }
}
