package com.mindmap.service;

import com.mindmap.model.DashboardStats;
import com.mindmap.model.Note;
import com.mindmap.model.TimelineEvent;
import com.mindmap.repository.ConnectionRepository;
import com.mindmap.repository.LearningEventRepository;
import com.mindmap.repository.NoteRepository;
import com.mindmap.repository.RevisionRepository;
import com.mindmap.repository.TagRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Service orchestrating dashboard data aggregation from underlying repositories.
 * Gathers note counts, connection statistics, review due/completed metrics,
 * subject/difficulty distributions, recent notes, and recent learning events.
 */
public class DashboardService {

    private final NoteRepository noteRepository;
    private final ConnectionRepository connectionRepository;
    private final RevisionRepository revisionRepository;
    private final TagRepository tagRepository;
    private final LearningEventRepository learningEventRepository;

    public DashboardService() {
        this(new NoteRepository(),
             new ConnectionRepository(),
             new RevisionRepository(),
             new TagRepository(),
             new LearningEventRepository());
    }

    public DashboardService(NoteRepository noteRepository,
                          ConnectionRepository connectionRepository,
                          RevisionRepository revisionRepository,
                          TagRepository tagRepository,
                          LearningEventRepository learningEventRepository) {
        this.noteRepository = Objects.requireNonNull(noteRepository, "noteRepository cannot be null");
        this.connectionRepository = Objects.requireNonNull(connectionRepository, "connectionRepository cannot be null");
        this.revisionRepository = Objects.requireNonNull(revisionRepository, "revisionRepository cannot be null");
        this.tagRepository = Objects.requireNonNull(tagRepository, "tagRepository cannot be null");
        this.learningEventRepository = Objects.requireNonNull(learningEventRepository, "learningEventRepository cannot be null");
    }

    /**
     * Gathers a complete snapshot of all dashboard statistics and recent records.
     * Uses optimized aggregate SQL queries without N+1 patterns.
     */
    public DashboardStats getDashboardStats() {
        LocalDate today = LocalDate.now();

        int totalNotes = noteRepository.count();
        int totalConnections = connectionRepository.count();
        int totalTags = tagRepository.count();
        int dueToday = revisionRepository.countDue(today);
        int upcoming = revisionRepository.countUpcoming(today);
        int totalScheduled = revisionRepository.countTotalScheduled();
        int completedToday = learningEventRepository.countCompletedReviewsForDate(today);

        int connectedNotes = connectionRepository.countConnectedNotes();
        int isolatedNotes = Math.max(0, totalNotes - connectedNotes);

        Map<String, Integer> bySubject = noteRepository.countNotesBySubject();
        Map<String, Integer> byDifficulty = noteRepository.countNotesByDifficulty();

        List<Note> recentNotes = noteRepository.findRecentNotes(5);
        List<TimelineEvent> recentActivity = learningEventRepository.findRecentTimelineEvents(6);

        return new DashboardStats(
                totalNotes,
                totalConnections,
                totalTags,
                dueToday,
                upcoming,
                totalScheduled,
                completedToday,
                connectedNotes,
                isolatedNotes,
                bySubject,
                byDifficulty,
                recentNotes,
                recentActivity
        );
    }

    public int getTotalNotes() {
        return noteRepository.count();
    }

    public int getTotalConnections() {
        return connectionRepository.count();
    }

    public int getConnectedNotes() {
        return connectionRepository.countConnectedNotes();
    }

    public int getIsolatedNotes() {
        return Math.max(0, noteRepository.count() - connectionRepository.countConnectedNotes());
    }

    public int getDueToday() {
        return revisionRepository.countDue(LocalDate.now());
    }

    public int getUpcomingReviews() {
        return revisionRepository.countUpcoming(LocalDate.now());
    }

    public int getCompletedToday() {
        return learningEventRepository.countCompletedReviewsForDate(LocalDate.now());
    }

    public Map<String, Integer> getNotesBySubject() {
        return noteRepository.countNotesBySubject();
    }

    public Map<String, Integer> getNotesByDifficulty() {
        return noteRepository.countNotesByDifficulty();
    }

    public List<Note> getRecentNotes(int limit) {
        return noteRepository.findRecentNotes(limit);
    }

    public List<TimelineEvent> getRecentActivity(int limit) {
        return learningEventRepository.findRecentTimelineEvents(limit);
    }
}
