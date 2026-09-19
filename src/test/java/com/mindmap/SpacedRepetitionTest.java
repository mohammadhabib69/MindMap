package com.mindmap;

import com.mindmap.database.DatabaseInitializer;
import com.mindmap.model.LearningEvent;
import com.mindmap.model.LearningEventType;
import com.mindmap.model.Note;
import com.mindmap.model.ReviewOutcome;
import com.mindmap.model.Revision;
import com.mindmap.model.RevisionStatus;
import com.mindmap.model.ScheduledReview;
import com.mindmap.repository.LearningEventRepository;
import com.mindmap.repository.NoteRepository;
import com.mindmap.repository.RevisionRepository;
import com.mindmap.repository.TagRepository;
import com.mindmap.service.NoteService;
import com.mindmap.service.RevisionService;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit & integration test suite verifying Spaced Repetition and Revision System functionality:
 * - Deterministic interval calculation for AGAIN, HARD, GOOD, EASY
 * - Initial review scheduling and duplicate prevention
 * - Due and upcoming review queue retrieval
 * - Review completion, interval updates, next review date math
 * - LearningEvent logging
 * - ScheduledReview model convenience methods
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SpacedRepetitionTest {

    private static NoteRepository noteRepository;
    private static RevisionRepository revisionRepository;
    private static LearningEventRepository learningEventRepository;
    private static RevisionService revisionService;
    private static NoteService noteService;

    private static final List<Integer> createdNoteIds = new ArrayList<>();
    private static final List<Integer> createdRevisionIds = new ArrayList<>();

    private static Note note1;
    private static Note note2;
    private static Note note3;

    @BeforeAll
    public static void setUp() throws SQLException {
        DatabaseInitializer.initialize();

        TagRepository tagRepository = new TagRepository();
        noteRepository = new NoteRepository(tagRepository);
        revisionRepository = new RevisionRepository();
        learningEventRepository = new LearningEventRepository();
        noteService = new NoteService(noteRepository);
        revisionService = new RevisionService(revisionRepository, noteRepository, learningEventRepository);

        // Create isolated test notes
        note1 = new Note("Spaced Repetition Note 1", "Memory retention concepts", "Neuroscience", "MEDIUM");
        note1.setUpdatedAt(LocalDateTime.now());
        note1 = noteRepository.createWithTags(note1, List.of("memory", "revision"));
        createdNoteIds.add(note1.getId());

        note2 = new Note("Spaced Repetition Note 2", "Ebbinghaus forgetting curve", "Psychology", "HARD");
        note2.setUpdatedAt(LocalDateTime.now());
        note2 = noteRepository.createWithTags(note2, List.of("forgetting-curve", "psychology"));
        createdNoteIds.add(note2.getId());

        note3 = new Note("Spaced Repetition Note 3", "Active Recall Techniques", "Education", "EASY");
        note3.setUpdatedAt(LocalDateTime.now());
        note3 = noteRepository.createWithTags(note3, List.of("active-recall"));
        createdNoteIds.add(note3.getId());
    }

    @AfterAll
    public static void tearDown() {
        for (int revId : createdRevisionIds) {
            try {
                revisionRepository.delete(revId);
            } catch (Exception ignored) {}
        }
        for (int noteId : createdNoteIds) {
            try {
                // Delete associated revisions first
                List<Revision> revs = revisionRepository.findByNoteId(noteId);
                for (Revision r : revs) {
                    revisionRepository.delete(r.getId());
                }
                noteRepository.delete(noteId);
            } catch (Exception ignored) {}
        }
    }

    @Test
    @Order(1)
    public void testReviewOutcomeEnumValues() {
        ReviewOutcome[] outcomes = ReviewOutcome.values();
        assertEquals(4, outcomes.length);
        assertNotNull(ReviewOutcome.valueOf("AGAIN"));
        assertNotNull(ReviewOutcome.valueOf("HARD"));
        assertNotNull(ReviewOutcome.valueOf("GOOD"));
        assertNotNull(ReviewOutcome.valueOf("EASY"));
    }

    @Test
    @Order(2)
    public void testIntervalProgressionAgain() {
        // AGAIN must always reset interval to 1 day
        assertEquals(1, revisionService.calculateNextInterval(1, ReviewOutcome.AGAIN));
        assertEquals(1, revisionService.calculateNextInterval(4, ReviewOutcome.AGAIN));
        assertEquals(1, revisionService.calculateNextInterval(14, ReviewOutcome.AGAIN));
        assertEquals(1, revisionService.calculateNextInterval(60, ReviewOutcome.AGAIN));
        assertEquals(1, revisionService.calculateNextInterval(120, ReviewOutcome.AGAIN));
    }

    @Test
    @Order(3)
    public void testIntervalProgressionHard() {
        // HARD: 1 -> 2 -> 4 -> 7 -> 12 -> 20 -> 30 (cap at 30)
        assertEquals(2, revisionService.calculateNextInterval(1, ReviewOutcome.HARD));
        assertEquals(4, revisionService.calculateNextInterval(2, ReviewOutcome.HARD));
        assertEquals(7, revisionService.calculateNextInterval(4, ReviewOutcome.HARD));
        assertEquals(12, revisionService.calculateNextInterval(7, ReviewOutcome.HARD));
        assertEquals(20, revisionService.calculateNextInterval(12, ReviewOutcome.HARD));
        assertEquals(30, revisionService.calculateNextInterval(20, ReviewOutcome.HARD));
        assertEquals(30, revisionService.calculateNextInterval(30, ReviewOutcome.HARD));
        assertEquals(30, revisionService.calculateNextInterval(50, ReviewOutcome.HARD));
    }

    @Test
    @Order(4)
    public void testIntervalProgressionGood() {
        // GOOD: 1 -> 3 -> 7 -> 14 -> 30 -> 60 -> 120 (cap at 120)
        assertEquals(3, revisionService.calculateNextInterval(1, ReviewOutcome.GOOD));
        assertEquals(7, revisionService.calculateNextInterval(3, ReviewOutcome.GOOD));
        assertEquals(14, revisionService.calculateNextInterval(7, ReviewOutcome.GOOD));
        assertEquals(30, revisionService.calculateNextInterval(14, ReviewOutcome.GOOD));
        assertEquals(60, revisionService.calculateNextInterval(30, ReviewOutcome.GOOD));
        assertEquals(120, revisionService.calculateNextInterval(60, ReviewOutcome.GOOD));
        assertEquals(120, revisionService.calculateNextInterval(120, ReviewOutcome.GOOD));
        assertEquals(120, revisionService.calculateNextInterval(200, ReviewOutcome.GOOD));
    }

    @Test
    @Order(5)
    public void testIntervalProgressionEasy() {
        // EASY: 1 -> 4 -> 10 -> 20 -> 40 -> 80 -> 160 (cap at 160)
        assertEquals(4, revisionService.calculateNextInterval(1, ReviewOutcome.EASY));
        assertEquals(10, revisionService.calculateNextInterval(4, ReviewOutcome.EASY));
        assertEquals(20, revisionService.calculateNextInterval(10, ReviewOutcome.EASY));
        assertEquals(40, revisionService.calculateNextInterval(20, ReviewOutcome.EASY));
        assertEquals(80, revisionService.calculateNextInterval(40, ReviewOutcome.EASY));
        assertEquals(160, revisionService.calculateNextInterval(80, ReviewOutcome.EASY));
        assertEquals(160, revisionService.calculateNextInterval(160, ReviewOutcome.EASY));
        assertEquals(160, revisionService.calculateNextInterval(300, ReviewOutcome.EASY));
    }

    @Test
    @Order(6)
    public void testCalculateNextReviewDate() {
        LocalDate today = LocalDate.now();
        assertEquals(today.plusDays(1), revisionService.calculateNextReviewDate(1));
        assertEquals(today.plusDays(7), revisionService.calculateNextReviewDate(7));
        assertEquals(today.plusDays(30), revisionService.calculateNextReviewDate(30));
    }

    @Test
    @Order(7)
    public void testScheduledReviewConvenienceMethods() {
        Note testNote = new Note("Test Convenience", "Content", "General", "MEDIUM");
        testNote.setId(999);

        // Due today
        Revision revToday = new Revision(999, LocalDate.now(), RevisionStatus.PENDING.name(), 1);
        ScheduledReview srToday = new ScheduledReview(revToday, testNote);
        assertTrue(srToday.isDue());
        assertFalse(srToday.isOverdue());
        assertEquals(0, srToday.getDaysUntilDue());

        // Overdue (2 days ago)
        Revision revPast = new Revision(999, LocalDate.now().minusDays(2), RevisionStatus.PENDING.name(), 3);
        ScheduledReview srPast = new ScheduledReview(revPast, testNote);
        assertTrue(srPast.isDue());
        assertTrue(srPast.isOverdue());
        assertEquals(-2, srPast.getDaysUntilDue());

        // Upcoming (4 days in future)
        Revision revFuture = new Revision(999, LocalDate.now().plusDays(4), RevisionStatus.PENDING.name(), 4);
        ScheduledReview srFuture = new ScheduledReview(revFuture, testNote);
        assertFalse(srFuture.isDue());
        assertFalse(srFuture.isOverdue());
        assertEquals(4, srFuture.getDaysUntilDue());
    }

    @Test
    @Order(8)
    public void testScheduleInitialReview() {
        Revision rev = revisionService.scheduleInitialReview(note1.getId());
        assertNotNull(rev);
        assertTrue(rev.getId() > 0);
        createdRevisionIds.add(rev.getId());

        assertEquals(note1.getId(), rev.getNoteId());
        assertEquals(1, rev.getIntervalDays());
        assertEquals(RevisionStatus.PENDING.name(), rev.getStatus());
        assertEquals(LocalDate.now(), rev.getReviewDate());
    }

    @Test
    @Order(9)
    public void testPreventDuplicateActiveSchedules() {
        // Calling scheduleInitialReview on note1 again should return the existing revision without creating a second one
        Revision duplicateAttempt = revisionService.scheduleInitialReview(note1.getId());
        assertNotNull(duplicateAttempt);

        List<Revision> allForNote = revisionRepository.findByNoteId(note1.getId());
        long pendingCount = allForNote.stream().filter(r -> RevisionStatus.PENDING.name().equals(r.getStatus())).count();
        assertEquals(1, pendingCount, "Should maintain exactly one active pending revision per note");
    }

    @Test
    @Order(10)
    public void testDueAndUpcomingRetrieval() {
        // Create an explicit due review for note2 (scheduled for today)
        Revision revDue = new Revision(note2.getId(), LocalDate.now(), RevisionStatus.PENDING.name(), 1);
        revDue = revisionRepository.create(revDue);
        createdRevisionIds.add(revDue.getId());

        // Create an explicit upcoming review for note3 (scheduled for 3 days in the future)
        Revision revUpcoming = new Revision(note3.getId(), LocalDate.now().plusDays(3), RevisionStatus.PENDING.name(), 3);
        revUpcoming = revisionRepository.create(revUpcoming);
        createdRevisionIds.add(revUpcoming.getId());

        List<ScheduledReview> due = revisionService.getDueReviews();
        List<ScheduledReview> upcoming = revisionService.getUpcomingReviews();

        assertTrue(due.stream().anyMatch(sr -> sr.getNoteId() == note2.getId()),
                "note2 should be present in due reviews");
        assertTrue(upcoming.stream().anyMatch(sr -> sr.getNoteId() == note3.getId()),
                "note3 should be present in upcoming reviews");
    }

    @Test
    @Order(11)
    public void testCompleteReviewWithGoodOutcome() {
        // Retrieve the due revision for note2
        Optional<Revision> activeOpt = revisionRepository.findActiveByNoteId(note2.getId());
        assertTrue(activeOpt.isPresent());
        Revision active = activeOpt.get();
        int initialInterval = active.getIntervalDays(); // 1

        // Complete review with GOOD outcome
        Revision updated = revisionService.completeReview(active, ReviewOutcome.GOOD);
        assertNotNull(updated);
        assertEquals(RevisionStatus.PENDING.name(), updated.getStatus());

        // GOOD progression from 1 -> 3
        assertEquals(3, updated.getIntervalDays());
        assertEquals(LocalDate.now().plusDays(3), updated.getReviewDate());

        // Verify learning event was logged
        List<LearningEvent> events = learningEventRepository.findByNoteId(note2.getId());
        assertFalse(events.isEmpty());
        assertTrue(events.stream().anyMatch(e ->
                LearningEventType.NOTE_REVIEWED.name().equals(e.getEventType()) &&
                e.getDescription().contains("GOOD") &&
                e.getDescription().contains("3 days")
        ));
    }

    @Test
    @Order(12)
    public void testCompleteReviewWithAgainOutcome() {
        // Note 2 is now at interval 3. If reviewed with AGAIN, it should reset to interval 1
        Optional<Revision> activeOpt = revisionRepository.findActiveByNoteId(note2.getId());
        assertTrue(activeOpt.isPresent());
        Revision active = activeOpt.get();

        Revision updated = revisionService.completeReview(active, ReviewOutcome.AGAIN);
        assertEquals(1, updated.getIntervalDays());
        assertEquals(LocalDate.now().plusDays(1), updated.getReviewDate());

        // Verify another learning event was logged
        List<LearningEvent> events = learningEventRepository.findByNoteId(note2.getId());
        assertTrue(events.stream().anyMatch(e ->
                LearningEventType.NOTE_REVIEWED.name().equals(e.getEventType()) &&
                e.getDescription().contains("AGAIN") &&
                e.getDescription().contains("1 day")
        ));
    }

    @Test
    @Order(13)
    public void testCompleteReviewWithHardOutcome() {
        // Note 1 is at interval 1. Completing with HARD should advance to interval 2
        Optional<Revision> activeOpt = revisionRepository.findActiveByNoteId(note1.getId());
        assertTrue(activeOpt.isPresent());
        Revision active = activeOpt.get();

        Revision updated = revisionService.completeReview(active, ReviewOutcome.HARD);
        assertEquals(2, updated.getIntervalDays());
        assertEquals(LocalDate.now().plusDays(2), updated.getReviewDate());
    }

    @Test
    @Order(14)
    public void testCompleteReviewWithEasyOutcome() {
        Note noteEasy = new Note("Easy Note", "Quick recall", "General", "EASY");
        noteEasy = noteRepository.create(noteEasy);
        createdNoteIds.add(noteEasy.getId());

        Revision revEasy = revisionService.scheduleInitialReview(noteEasy.getId());
        createdRevisionIds.add(revEasy.getId());
        assertEquals(1, revEasy.getIntervalDays());

        // Complete with EASY: 1 -> 4
        Revision updated = revisionService.completeReview(revEasy, ReviewOutcome.EASY);
        assertEquals(4, updated.getIntervalDays());
        assertEquals(LocalDate.now().plusDays(4), updated.getReviewDate());
    }

    @Test
    @Order(15)
    public void testRevisionCounts() {
        int dueCount = revisionService.getDueCount();
        int upcomingCount = revisionService.getUpcomingCount();
        int totalCount = revisionService.getTotalScheduledCount();

        assertTrue(dueCount >= 0);
        assertTrue(upcomingCount >= 3, "At least our 3 test notes should be upcoming");
        assertTrue(totalCount >= 3, "Total scheduled should include all active pending notes");
        assertTrue(totalCount >= dueCount + upcomingCount);
    }

    @Test
    @Order(16)
    public void testScheduleAllUnscheduledNotes() {
        // Create an unscheduled note
        Note unscheduled = new Note("Unscheduled Note For Bulk Test", "Bulk schedule test content", "Math", "HARD");
        unscheduled.setUpdatedAt(LocalDateTime.now());
        unscheduled = noteRepository.create(unscheduled);
        createdNoteIds.add(unscheduled.getId());

        // Schedule all
        int newlyScheduled = revisionService.scheduleAllUnscheduledNotes();
        assertTrue(newlyScheduled >= 1, "Should schedule at least the newly created unscheduled note");

        // Verify the note is now scheduled
        Optional<Revision> revOpt = revisionRepository.findActiveByNoteId(unscheduled.getId());
        assertTrue(revOpt.isPresent());
        assertEquals(1, revOpt.get().getIntervalDays());
        createdRevisionIds.add(revOpt.get().getId());

        // Calling it again immediately should return 0 since all notes are scheduled
        int secondCall = revisionService.scheduleAllUnscheduledNotes();
        assertEquals(0, secondCall, "Second call should find 0 unscheduled notes");
    }
}
