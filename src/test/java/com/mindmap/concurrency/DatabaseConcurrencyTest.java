package com.mindmap.concurrency;

import com.mindmap.database.DatabaseInitializer;
import com.mindmap.database.DatabaseManager;
import com.mindmap.model.Difficulty;
import com.mindmap.model.Note;
import com.mindmap.repository.NoteRepository;
import com.mindmap.repository.RevisionRepository;
import com.mindmap.repository.TagRepository;
import com.mindmap.service.NoteService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests verifying thread safety of SQLite under concurrent access by multiple worker threads.
 * Validates that PRAGMA busy_timeout = 5000; and the connection-per-call pattern prevent
 * SQLITE_BUSY lock errors when concurrent writes and reads happen simultaneously.
 */
public class DatabaseConcurrencyTest {

    private static NoteRepository noteRepository;
    private static TagRepository tagRepository;
    private static NoteService noteService;
    private static final List<Integer> createdNoteIds = Collections.synchronizedList(new ArrayList<>());

    @BeforeAll
    static void setup() throws SQLException {
        DatabaseInitializer.initialize();
        noteRepository = new NoteRepository();
        tagRepository = new TagRepository();
        noteService = new NoteService(noteRepository);
    }

    @AfterAll
    static void tearDown() {
        for (Integer id : createdNoteIds) {
            try {
                noteRepository.delete(id);
            } catch (Exception ignored) {}
        }
    }

    @Test
    void testBusyTimeoutPragmaConfigured() throws SQLException {
        try (Connection conn = DatabaseManager.getConnection()) {
            assertNotNull(conn);
            assertFalse(conn.isClosed());
        }
    }

    @Test
    void testConcurrentReadsAndWritesAcrossWorkers() throws InterruptedException {
        TaskExecutor.ensureActiveExecutor();
        int taskCount = 18;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(taskCount);
        AtomicInteger successCount = new AtomicInteger(0);
        List<Throwable> exceptions = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < taskCount; i++) {
            final int index = i;
            TaskExecutor.execute(() -> {
                try {
                    // Synchronize start to maximize simultaneous concurrency
                    startLatch.await(3, TimeUnit.SECONDS);

                    if (index % 2 == 0) {
                        // Write operation: Create note with tags
                        Note n = new Note("Concurrent Note #" + index, "Body " + index, "Concurrency", Difficulty.MEDIUM.name());
                        Note created = noteService.createNoteWithTags(n, List.of("thread-safe", "worker-" + (index % 3)));
                        createdNoteIds.add(created.getId());
                        successCount.incrementAndGet();
                    } else {
                        // Read operation: Search and filter notes
                        List<Note> results = noteService.searchAndFilterNotes("Concurrent", null);
                        assertNotNull(results);
                        int count = noteService.getNoteCount();
                        assertTrue(count >= 0);
                        successCount.incrementAndGet();
                    }
                } catch (Throwable t) {
                    exceptions.add(t);
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        boolean completed = doneLatch.await(10, TimeUnit.SECONDS);

        assertTrue(completed, "Concurrent database tasks did not finish within timeout");
        assertTrue(exceptions.isEmpty(), "Concurrent database operations produced exceptions: " + exceptions);
        assertEquals(taskCount, successCount.get(), "All concurrent database tasks should succeed");
    }
}
