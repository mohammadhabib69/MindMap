package com.mindmap.service;

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

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service managing business logic, deterministic spaced repetition calculations,
 * and review scheduling for Note entities.
 */
public class RevisionService {

    private static final Logger LOGGER = Logger.getLogger(RevisionService.class.getName());

    private final RevisionRepository revisionRepository;
    private final NoteRepository noteRepository;
    private final LearningEventRepository learningEventRepository;

    public RevisionService() {
        this(new RevisionRepository(), new NoteRepository(), new LearningEventRepository());
    }

    public RevisionService(RevisionRepository revisionRepository,
                           NoteRepository noteRepository,
                           LearningEventRepository learningEventRepository) {
        this.revisionRepository = revisionRepository;
        this.noteRepository = noteRepository;
        this.learningEventRepository = learningEventRepository;
    }

    /**
     * Schedules an initial revision for a note if an active schedule does not already exist.
     * Prevents duplicate active schedules for the same note.
     *
     * @param noteId The ID of the note to schedule.
     * @return The active or newly created Revision.
     */
    public Revision scheduleInitialReview(int noteId) {
        // 1. Check if active pending revision exists
        Optional<Revision> active = revisionRepository.findActiveByNoteId(noteId);
        if (active.isPresent()) {
            return active.get();
        }

        // 2. Check if a previously completed revision exists that can be reactivated
        List<Revision> existing = revisionRepository.findByNoteId(noteId);
        if (!existing.isEmpty()) {
            Revision rev = existing.get(0);
            rev.setStatus(RevisionStatus.PENDING.name());
            rev.setIntervalDays(1);
            rev.setReviewDate(LocalDate.now());
            revisionRepository.update(rev);
            return rev;
        }

        // 3. Create fresh revision due today with 1-day interval
        Revision newRev = new Revision(noteId, LocalDate.now(), RevisionStatus.PENDING.name(), 1);
        return revisionRepository.create(newRev);
    }

    /**
     * Retrieves all scheduled reviews that are due on or before today.
     * Uses a single JOIN query for efficiency with large review lists.
     *
     * @return List of due ScheduledReview items with Note details.
     */
    public List<ScheduledReview> getDueReviews() {
        return revisionRepository.findDueWithNotes(LocalDate.now());
    }

    /**
     * Retrieves all scheduled reviews due strictly after today, sorted ascending by review date.
     * Uses a single JOIN query for efficiency with large review lists.
     *
     * @return List of upcoming ScheduledReview items with Note details.
     */
    public List<ScheduledReview> getUpcomingReviews() {
        return revisionRepository.findUpcomingWithNotes(LocalDate.now());
    }

    /**
     * Completes a review, calculates the next interval and review date, updates SQLite,
     * and records a LearningEvent for timeline tracking.
     *
     * @param revision The revision being completed.
     * @param outcome  The user's rating outcome (AGAIN, HARD, GOOD, EASY).
     * @return The updated Revision.
     */
    public Revision completeReview(Revision revision, ReviewOutcome outcome) {
        if (revision == null) {
            throw new IllegalArgumentException("Revision cannot be null");
        }
        if (outcome == null) {
            outcome = ReviewOutcome.GOOD;
        }

        int currentInterval = revision.getIntervalDays();
        int nextInterval = calculateNextInterval(currentInterval, outcome);
        LocalDate nextReviewDate = calculateNextReviewDate(nextInterval);

        revision.setIntervalDays(nextInterval);
        revision.setReviewDate(nextReviewDate);
        revision.setStatus(RevisionStatus.PENDING.name());

        // Update revision in database
        revisionRepository.update(revision);

        // Record learning event
        try {
            String description = String.format("Review completed: %s, next interval %d %s",
                    outcome.name(), nextInterval, nextInterval == 1 ? "day" : "days");
            LearningEvent event = new LearningEvent(
                    revision.getNoteId(),
                    LearningEventType.NOTE_REVIEWED.name(),
                    description
            );
            learningEventRepository.create(event);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to log learning event for review: " + e.getMessage(), e);
        }

        return revision;
    }

    /**
     * Deterministically calculates the next interval in days based on the current interval
     * and review outcome grade.
     * <p>
     * AGAIN: Resets to 1 day.
     * HARD: Gradual increase (1 -> 2 -> 4 -> 7 -> 12 -> 20 -> 30).
     * GOOD: Standard increase (1 -> 3 -> 7 -> 14 -> 30 -> 60 -> 120).
     * EASY: Accelerated increase (1 -> 4 -> 10 -> 20 -> 40 -> 80 -> 160).
     *
     * @param currentInterval The current interval in days.
     * @param outcome         The retention grade selected.
     * @return Next interval in days (minimum 1, maximum 365).
     */
    public int calculateNextInterval(int currentInterval, ReviewOutcome outcome) {
        if (outcome == null) {
            outcome = ReviewOutcome.GOOD;
        }
        int interval = Math.max(1, currentInterval);

        if (outcome == ReviewOutcome.AGAIN) {
            return 1;
        }

        if (outcome == ReviewOutcome.HARD) {
            if (interval == 1) return 2;
            if (interval <= 3) return 4;
            if (interval <= 5) return 7;
            if (interval <= 8) return 12;
            if (interval <= 15) return 20;
            return 30;
        }

        if (outcome == ReviewOutcome.GOOD) {
            if (interval == 1) return 3;
            if (interval <= 4) return 7;
            if (interval <= 8) return 14;
            if (interval <= 18) return 30;
            if (interval <= 35) return 60;
            return 120;
        }

        if (outcome == ReviewOutcome.EASY) {
            if (interval == 1) return 4;
            if (interval <= 5) return 10;
            if (interval <= 12) return 20;
            if (interval <= 25) return 40;
            if (interval <= 50) return 80;
            return 160;
        }


        return Math.max(1, interval);
    }

    /**
     * Calculates the next review date given the interval in days from today.
     *
     * @param intervalDays Number of days until next review.
     * @return The calculated LocalDate.
     */
    public LocalDate calculateNextReviewDate(int intervalDays) {
        return LocalDate.now().plusDays(Math.max(1, intervalDays));
    }

    /**
     * Returns the count of reviews due on or before today.
     */
    public int getDueCount() {
        return revisionRepository.countDue(LocalDate.now());
    }

    /**
     * Returns the count of upcoming reviews scheduled after today.
     */
    public int getUpcomingCount() {
        return revisionRepository.countUpcoming(LocalDate.now());
    }

    /**
     * Returns the total count of active/pending scheduled reviews.
     */
    public int getTotalScheduledCount() {
        return revisionRepository.countTotalScheduled();
    }

    /**
     * Schedules all existing notes that do not currently have an active/pending revision.
     *
     * @return Number of newly scheduled notes.
     */
    public int scheduleAllUnscheduledNotes() {
        List<Note> allNotes = noteRepository.findAll();
        int scheduledCount = 0;

        for (Note note : allNotes) {
            Optional<Revision> active = revisionRepository.findActiveByNoteId(note.getId());
            if (active.isEmpty()) {
                scheduleInitialReview(note.getId());
                scheduledCount++;
            }
        }

        return scheduledCount;
    }
}
