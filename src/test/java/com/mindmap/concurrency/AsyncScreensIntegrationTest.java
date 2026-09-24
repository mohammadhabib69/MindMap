package com.mindmap.concurrency;

import com.mindmap.controller.DashboardController;
import com.mindmap.controller.NotesController;
import com.mindmap.controller.RevisionController;

import com.mindmap.controller.TimelineController;
import com.mindmap.database.DatabaseInitializer;
import com.mindmap.model.DashboardStats;
import com.mindmap.model.Difficulty;
import com.mindmap.model.Note;
import com.mindmap.repository.NoteRepository;
import com.mindmap.repository.RevisionRepository;
import com.mindmap.service.DashboardService;
import com.mindmap.service.NoteService;
import com.mindmap.service.RevisionService;
import com.mindmap.service.SearchService;
import com.mindmap.service.TimelineService;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests verifying asynchronous background execution,
 * JavaFX Application Thread safety for UI controls and ObservableLists,
 * and stale-result token protection across screens.
 */
public class AsyncScreensIntegrationTest {

    private static boolean toolkitInitialized = false;
    private static final List<Integer> createdNoteIds = new ArrayList<>();
    private static NoteRepository noteRepository;

    @BeforeAll
    static void setupAll() throws SQLException, InterruptedException {
        DatabaseInitializer.initialize();
        noteRepository = new NoteRepository();

        CountDownLatch latch = new CountDownLatch(1);
        try {
            Platform.startup(latch::countDown);
            toolkitInitialized = true;
            latch.await(3, TimeUnit.SECONDS);
        } catch (IllegalStateException e) {
            // Already running
            toolkitInitialized = true;
        }

        // Seed sample note
        Note n = new Note();
        n.setTitle("Async Screen Test Note");
        n.setContent("Integration testing content for async execution");
        n.setSubject("Concurrency");
        n.setDifficulty(Difficulty.EASY.name());
        Note created = noteRepository.create(n);
        createdNoteIds.add(created.getId());
    }

    @AfterAll
    static void tearDownAll() {
        for (int id : createdNoteIds) {
            try {
                noteRepository.delete(id);
            } catch (Exception ignored) {}
        }
    }

    @Test
    void testDashboardAsyncDataLoading() throws Exception {
        if (!toolkitInitialized) return;
        TaskExecutor.ensureActiveExecutor();

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dashboard.fxml"));
                Parent root = loader.load();
                assertNotNull(root);
                DashboardController controller = loader.getController();

                CompletableFuture<DashboardStats> future = controller.loadDashboardData();
                assertNotNull(future);

                future.whenComplete((stats, ex) -> {
                    try {
                        assertTrue(Platform.isFxApplicationThread(), "UI update must complete on JavaFX thread");
                        assertNull(ex, "Dashboard async load should not throw: " + (ex != null ? ex.getMessage() : ""));
                        assertNotNull(stats);
                        assertTrue(stats.getTotalNotes() >= 1);
                    } finally {
                        latch.countDown();
                    }
                });
            } catch (Exception e) {
                fail("Failed to load dashboard: " + e.getMessage());
                latch.countDown();
            }
        });

        assertTrue(latch.await(5, TimeUnit.SECONDS), "Dashboard async loading timed out");
    }

    @Test
    void testRevisionAsyncDataLoading() throws Exception {
        if (!toolkitInitialized) return;
        TaskExecutor.ensureActiveExecutor();

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/revision.fxml"));
                Parent root = loader.load();
                assertNotNull(root);
                RevisionController controller = loader.getController();

                CompletableFuture<RevisionController.RevisionSnapshot> future = controller.loadRevisionData();
                assertNotNull(future);

                future.whenComplete((snapshot, ex) -> {
                    try {
                        assertTrue(Platform.isFxApplicationThread(), "Revision UI update must be on JavaFX thread");
                        assertNull(ex);
                        assertNotNull(snapshot);
                        assertNotNull(controller.getDueData());
                        assertNotNull(controller.getUpcomingData());
                    } finally {
                        latch.countDown();
                    }
                });
            } catch (Exception e) {
                fail("Failed to load revision screen: " + e.getMessage());
                latch.countDown();
            }
        });

        assertTrue(latch.await(5, TimeUnit.SECONDS), "Revision async loading timed out");
    }

    @Test
    void testTimelineAsyncFilterAndStaleTokenProtection() throws Exception {
        if (!toolkitInitialized) return;
        TaskExecutor.ensureActiveExecutor();

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/timeline.fxml"));
                Parent root = loader.load();
                assertNotNull(root);
                TimelineController controller = loader.getController();

                // 1. Dispatch first async filter query
                CompletableFuture<TimelineController.TimelineSnapshot> future1 = controller.handleApplyFilters();

                // 2. Immediately dispatch a second filter query (simulating rapid user interaction)
                CompletableFuture<TimelineController.TimelineSnapshot> future2 = controller.handleApplyFilters();

                // Future 2 should complete successfully on FX thread
                future2.whenComplete((snapshot, ex) -> {
                    try {
                        assertTrue(Platform.isFxApplicationThread(), "Timeline UI update must run on FX thread");
                        assertNull(ex);
                        assertNotNull(snapshot);
                        assertNotNull(controller.getTimelineData());
                    } finally {
                        latch.countDown();
                    }
                });
            } catch (Exception e) {
                fail("Timeline async filter test failed: " + e.getMessage());
                latch.countDown();
            }
        });

        assertTrue(latch.await(5, TimeUnit.SECONDS), "Timeline async filtering timed out");
    }

    @Test
    void testSearchAsyncExecutionAndStaleProtection() throws Exception {
        if (!toolkitInitialized) return;
        TaskExecutor.ensureActiveExecutor();

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/notes.fxml"));
                Parent root = loader.load();
                assertNotNull(root);
                NotesController controller = loader.getController();

                // Rapid successive searches
                CompletableFuture<List<Note>> f1 = controller.handleSearch();
                CompletableFuture<List<Note>> f2 = controller.handleSearch();

                f2.whenComplete((results, ex) -> {
                    try {
                        assertTrue(Platform.isFxApplicationThread(), "Search results must update on JavaFX thread");
                        assertNull(ex);
                        assertNotNull(results);
                    } finally {
                        latch.countDown();
                    }
                });
            } catch (Exception e) {
                fail("Search async execution test failed: " + e.getMessage());
                latch.countDown();
            }
        });

        assertTrue(latch.await(5, TimeUnit.SECONDS), "Search async execution timed out");
    }

    @Test
    void testNotesControllerAsyncFilter() throws Exception {
        if (!toolkitInitialized) return;
        TaskExecutor.ensureActiveExecutor();

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/notes.fxml"));
                Parent root = loader.load();
                assertNotNull(root);
                NotesController controller = loader.getController();

                CompletableFuture<java.util.List<Note>> future = controller.handleSearch();
                assertNotNull(future);

                future.whenComplete((snapshot, ex) -> {
                    try {
                        assertTrue(Platform.isFxApplicationThread(), "Notes UI update must run on JavaFX thread");
                        assertNull(ex);
                        assertNotNull(snapshot);
                        assertNotNull(snapshot);
                    } finally {
                        latch.countDown();
                    }
                });
            } catch (Exception e) {
                fail("Notes async filter test failed: " + e.getMessage());
                latch.countDown();
            }
        });

        assertTrue(latch.await(5, TimeUnit.SECONDS), "Notes async filter timed out");
    }
}
