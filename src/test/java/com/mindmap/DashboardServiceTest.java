package com.mindmap;

import com.mindmap.database.DatabaseInitializer;
import com.mindmap.model.Connection;
import com.mindmap.model.DashboardStats;
import com.mindmap.model.Difficulty;
import com.mindmap.model.LearningEvent;
import com.mindmap.model.LearningEventType;
import com.mindmap.model.Note;
import com.mindmap.model.Revision;
import com.mindmap.model.RevisionStatus;
import com.mindmap.model.TimelineEvent;
import com.mindmap.repository.ConnectionRepository;
import com.mindmap.repository.LearningEventRepository;
import com.mindmap.repository.NoteRepository;
import com.mindmap.repository.RevisionRepository;
import com.mindmap.repository.TagRepository;
import com.mindmap.service.DashboardService;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
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
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit and integration tests for DashboardService and DashboardController:
 * - Aggregates real SQLite data for notes, connections, tags, revisions, and events
 * - Verifies connected vs isolated note calculations
 * - Tests subject and difficulty distributions
 * - Confirms recent notes and recent learning events
 * - Validates dashboard FXML loading and control binding
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DashboardServiceTest {

    private static NoteRepository noteRepository;
    private static ConnectionRepository connectionRepository;
    private static RevisionRepository revisionRepository;
    private static TagRepository tagRepository;
    private static LearningEventRepository learningEventRepository;
    private static DashboardService dashboardService;

    private static final List<Integer> createdNoteIds = new ArrayList<>();
    private static final List<Integer> createdConnectionIds = new ArrayList<>();
    private static final List<Integer> createdRevisionIds = new ArrayList<>();
    private static final List<Integer> createdEventIds = new ArrayList<>();

    private static Note noteA;
    private static Note noteB;
    private static Note noteIsolated;

    @BeforeAll
    static void setUpAll() throws SQLException {
        DatabaseInitializer.initialize();
        noteRepository = new NoteRepository();
        connectionRepository = new ConnectionRepository();
        revisionRepository = new RevisionRepository();
        tagRepository = new TagRepository();
        learningEventRepository = new LearningEventRepository();
        dashboardService = new DashboardService(
                noteRepository, connectionRepository, revisionRepository, tagRepository, learningEventRepository
        );

        // 1. Seed test notes
        noteA = new Note("Dashboard Note A", "Content A", "Machine Learning", Difficulty.HARD.name());
        noteA = noteRepository.create(noteA);
        createdNoteIds.add(noteA.getId());

        noteB = new Note("Dashboard Note B", "Content B", "Machine Learning", Difficulty.MEDIUM.name());
        noteB = noteRepository.create(noteB);
        createdNoteIds.add(noteB.getId());

        noteIsolated = new Note("Isolated Note C", "Content C", "Philosophy", Difficulty.EASY.name());
        noteIsolated = noteRepository.create(noteIsolated);
        createdNoteIds.add(noteIsolated.getId());

        // 2. Connect noteA <-> noteB
        Connection conn = new Connection(noteA.getId(), noteB.getId(), "depends on");
        conn = connectionRepository.create(conn);
        createdConnectionIds.add(conn.getId());

        // 3. Seed revisions: 1 due today, 1 upcoming
        Revision revDue = new Revision(noteA.getId(), LocalDate.now(), RevisionStatus.PENDING.name(), 1);
        revDue = revisionRepository.create(revDue);
        createdRevisionIds.add(revDue.getId());

        Revision revUpcoming = new Revision(noteB.getId(), LocalDate.now().plusDays(4), RevisionStatus.PENDING.name(), 5);
        revUpcoming = revisionRepository.create(revUpcoming);
        createdRevisionIds.add(revUpcoming.getId());

        // 4. Seed learning event for today (review completed)
        LearningEvent eventToday = new LearningEvent(noteA.getId(), LearningEventType.NOTE_REVIEWED.name(), "Review completed: GOOD");
        eventToday.setEventDate(LocalDateTime.now());
        eventToday = learningEventRepository.create(eventToday);
        createdEventIds.add(eventToday.getId());
    }

    @AfterAll
    static void tearDownAll() {
        for (int connId : createdConnectionIds) {
            try { connectionRepository.delete(connId); } catch (Exception ignored) {}
        }
        for (int revId : createdRevisionIds) {
            try { revisionRepository.delete(revId); } catch (Exception ignored) {}
        }
        for (int evId : createdEventIds) {
            try { learningEventRepository.delete(evId); } catch (Exception ignored) {}
        }
        for (int noteId : createdNoteIds) {
            try { noteRepository.delete(noteId); } catch (Exception ignored) {}
        }
    }

    @Test
    @Order(1)
    void testTotalNotesAndDistinctSubjects() {
        int totalNotes = dashboardService.getTotalNotes();
        assertTrue(totalNotes >= 3, "Total notes should include our seeded notes");

        Map<String, Integer> bySubject = dashboardService.getNotesBySubject();
        assertNotNull(bySubject);
        assertTrue(bySubject.containsKey("Machine Learning"));
        assertTrue(bySubject.get("Machine Learning") >= 2);
        assertTrue(bySubject.containsKey("Philosophy"));
    }

    @Test
    @Order(2)
    void testConnectionsAndGraphStructure() {
        int totalConnections = dashboardService.getTotalConnections();
        assertTrue(totalConnections >= 1, "Should have at least our 1 connection");

        int connectedNotes = dashboardService.getConnectedNotes();
        assertTrue(connectedNotes >= 2, "noteA and noteB are connected, so at least 2 connected notes");

        int isolatedNotes = dashboardService.getIsolatedNotes();
        assertTrue(isolatedNotes >= 1, "noteIsolated is not connected, so at least 1 isolated note");

        assertEquals(dashboardService.getTotalNotes(), connectedNotes + isolatedNotes,
                "Total notes must equal connected notes + isolated notes");
    }

    @Test
    @Order(3)
    void testRevisionDueAndUpcomingCounts() {
        int due = dashboardService.getDueToday();
        assertTrue(due >= 1, "Should have at least 1 due review");

        int upcoming = dashboardService.getUpcomingReviews();
        assertTrue(upcoming >= 1, "Should have at least 1 upcoming review");

        int completedToday = dashboardService.getCompletedToday();
        assertTrue(completedToday >= 1, "Should have at least 1 completed review today");
    }

    @Test
    @Order(4)
    void testNotesByDifficultyDistribution() {
        Map<String, Integer> byDifficulty = dashboardService.getNotesByDifficulty();
        assertNotNull(byDifficulty);
        assertTrue(byDifficulty.containsKey("EASY"));
        assertTrue(byDifficulty.containsKey("MEDIUM"));
        assertTrue(byDifficulty.containsKey("HARD"));

        assertTrue(byDifficulty.get("HARD") >= 1, "noteA is HARD");
        assertTrue(byDifficulty.get("MEDIUM") >= 1, "noteB is MEDIUM");
        assertTrue(byDifficulty.get("EASY") >= 1, "noteIsolated is EASY");
    }

    @Test
    @Order(5)
    void testRecentNotesOrdering() {
        List<Note> recentNotes = dashboardService.getRecentNotes(5);
        assertNotNull(recentNotes);
        assertFalse(recentNotes.isEmpty());
        assertTrue(recentNotes.size() <= 5);

        // Verify descending updated_at ordering
        for (int i = 0; i < recentNotes.size() - 1; i++) {
            LocalDateTime current = recentNotes.get(i).getUpdatedAt();
            LocalDateTime next = recentNotes.get(i + 1).getUpdatedAt();
            if (current != null && next != null) {
                assertTrue(current.isAfter(next) || current.isEqual(next),
                        "Recent notes must be ordered descending by updated_at");
            }
        }
    }

    @Test
    @Order(6)
    void testRecentLearningActivity() {
        List<TimelineEvent> recentActivity = dashboardService.getRecentActivity(6);
        assertNotNull(recentActivity);
        assertFalse(recentActivity.isEmpty());
        assertTrue(recentActivity.size() <= 6);

        // Verify latest activity contains our seeded review event
        boolean foundOurReview = recentActivity.stream()
                .anyMatch(e -> e.getId() == createdEventIds.get(0));
        assertTrue(foundOurReview, "Recent activity should include today's completed review");
    }

    @Test
    @Order(7)
    void testDashboardStatsSnapshotConsistency() {
        DashboardStats stats = dashboardService.getDashboardStats();
        assertNotNull(stats);

        assertTrue(stats.getTotalNotes() >= 3);
        assertTrue(stats.getTotalConnections() >= 1);
        assertTrue(stats.getConnectedNotes() >= 2);
        assertTrue(stats.getDueToday() >= 1);
        assertTrue(stats.getUpcomingReviews() >= 1);
        assertTrue(stats.getCompletedToday() >= 1);

        // Progress calculations
        double progressRatio = stats.getReviewProgressRatio();
        assertTrue(progressRatio > 0.0 && progressRatio <= 1.0);
        int progressPercent = stats.getReviewProgressPercent();
        assertTrue(progressPercent > 0 && progressPercent <= 100);

        // Connected ratio
        double connRatio = stats.getConnectedRatio();
        assertTrue(connRatio > 0.0 && connRatio <= 1.0);
        int connPercent = stats.getConnectedPercent();
        assertTrue(connPercent > 0 && connPercent <= 100);
    }

    @Test
    @Order(8)
    void testDashboardFxmlLoadingAndInitialization() throws Exception {
        CountDownLatch startupLatch = new CountDownLatch(1);
        try {
            Platform.startup(startupLatch::countDown);
            startupLatch.await(2, TimeUnit.SECONDS);
        } catch (IllegalStateException ignored) {
            // Platform already initialized
        }

        CountDownLatch uiLatch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dashboard.fxml"));
                Parent root = loader.load();
                assertNotNull(root, "dashboard.fxml should load successfully");

                com.mindmap.controller.DashboardController controller = loader.getController();
                assertNotNull(controller, "DashboardController should be instantiated");
                assertNotNull(controller.getDashboardService());
            } catch (Exception e) {
                fail("Failed to load dashboard.fxml: " + e.getMessage());
            } finally {
                uiLatch.countDown();
            }
        });

        assertTrue(uiLatch.await(5, TimeUnit.SECONDS), "Dashboard UI initialization timed out");
    }
}
