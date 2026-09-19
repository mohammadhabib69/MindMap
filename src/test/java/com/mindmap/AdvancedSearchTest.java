package com.mindmap;

import com.mindmap.database.DatabaseInitializer;
import com.mindmap.model.Connection;
import com.mindmap.model.ConnectionFilterPreset;
import com.mindmap.model.DateFilterPreset;
import com.mindmap.model.Difficulty;
import com.mindmap.model.Note;
import com.mindmap.model.SearchCriteria;
import com.mindmap.repository.ConnectionRepository;
import com.mindmap.repository.NoteRepository;
import com.mindmap.repository.TagRepository;
import com.mindmap.service.SearchService;
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
 * Test suite verifying Phase 7 Advanced Search & Multi-Criteria Filtering functionality.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AdvancedSearchTest {

    private static NoteRepository noteRepository;
    private static TagRepository tagRepository;
    private static ConnectionRepository connectionRepository;
    private static SearchService searchService;

    private static final List<Integer> createdNoteIds = new ArrayList<>();
    private static final List<Integer> createdConnIds = new ArrayList<>();

    private static Note noteAlgo;
    private static Note noteCalculus;
    private static Note noteHistory;
    private static Note notePhysics;

    @BeforeAll
    public static void setUp() throws SQLException {
        DatabaseInitializer.initialize();

        tagRepository = new TagRepository();
        noteRepository = new NoteRepository(tagRepository);
        connectionRepository = new ConnectionRepository();
        searchService = new SearchService(noteRepository, tagRepository);

        // Create specific test notes
        noteAlgo = new Note("Graph Dijkstra Algorithm", "Shortest path algorithm on weighted graphs", "Computer Science", "HARD");
        noteAlgo.setUpdatedAt(LocalDateTime.now());
        noteAlgo = noteRepository.createWithTags(noteAlgo, List.of("graphs", "dijkstra", "algorithms"));
        createdNoteIds.add(noteAlgo.getId());

        noteCalculus = new Note("Differential Calculus", "Derivatives and chain rule concepts", "Mathematics", "MEDIUM");
        noteCalculus.setUpdatedAt(LocalDateTime.now());
        noteCalculus = noteRepository.createWithTags(noteCalculus, List.of("calculus", "derivatives"));
        createdNoteIds.add(noteCalculus.getId());

        noteHistory = new Note("Industrial Revolution", "Steam engine and textile mechanization in Britain", "History", "EASY");
        noteHistory.setUpdatedAt(LocalDateTime.now().minusDays(3));
        noteHistory = noteRepository.createWithTags(noteHistory, List.of("history", "revolution"));
        createdNoteIds.add(noteHistory.getId());

        notePhysics = new Note("Quantum Mechanics Basics", "Wave-particle duality and Schrödinger equation", "Physics", "HARD");
        notePhysics.setUpdatedAt(LocalDateTime.now().minusDays(15));
        notePhysics = noteRepository.createWithTags(notePhysics, List.of("quantum", "physics"));
        createdNoteIds.add(notePhysics.getId());

        // Connect noteAlgo to noteCalculus
        Connection conn = new Connection(noteAlgo.getId(), noteCalculus.getId(), "applies to");
        conn = connectionRepository.create(conn);
        createdConnIds.add(conn.getId());
    }

    @AfterAll
    public static void tearDown() {
        for (int connId : createdConnIds) {
            try {
                connectionRepository.delete(connId);
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
    public void testSearchByTitleKeyword() {
        SearchCriteria criteria = new SearchCriteria().withQuery("Dijkstra");
        List<Note> results = searchService.searchNotes(criteria);

        assertFalse(results.isEmpty(), "Results should find Dijkstra note");
        assertTrue(results.stream().anyMatch(n -> n.getId() == noteAlgo.getId()), "Should include noteAlgo");
        assertFalse(results.stream().anyMatch(n -> n.getId() == noteCalculus.getId()), "Should not include noteCalculus");
    }

    @Test
    @Order(2)
    public void testSearchByContentKeyword() {
        SearchCriteria criteria = new SearchCriteria().withQuery("textile mechanization");
        List<Note> results = searchService.searchNotes(criteria);

        assertFalse(results.isEmpty(), "Results should find note by content keyword");
        assertTrue(results.stream().anyMatch(n -> n.getId() == noteHistory.getId()), "Should match noteHistory");
    }

    @Test
    @Order(3)
    public void testSearchBySubjectKeyword() {
        SearchCriteria criteria = new SearchCriteria().withQuery("Mathematics");
        List<Note> results = searchService.searchNotes(criteria);

        assertFalse(results.isEmpty(), "Results should find Mathematics note");
        assertTrue(results.stream().anyMatch(n -> n.getId() == noteCalculus.getId()), "Should include noteCalculus");
    }

    @Test
    @Order(4)
    public void testSearchByTagKeyword() {
        SearchCriteria criteria = new SearchCriteria().withQuery("quantum");
        List<Note> results = searchService.searchNotes(criteria);

        assertFalse(results.isEmpty(), "Results should match note tagged 'quantum'");
        assertTrue(results.stream().anyMatch(n -> n.getId() == notePhysics.getId()), "Should include notePhysics");
    }

    @Test
    @Order(5)
    public void testCaseInsensitiveAndWhitespaceSearch() {
        SearchCriteria criteria = new SearchCriteria().withQuery("   dIjKsTrA   ");
        List<Note> results = searchService.searchNotes(criteria);

        assertTrue(results.stream().anyMatch(n -> n.getId() == noteAlgo.getId()), "Case-insensitive search should match");
    }

    @Test
    @Order(6)
    public void testFilterBySubject() {
        SearchCriteria criteria = new SearchCriteria().withSubject("Computer Science");
        List<Note> results = searchService.searchNotes(criteria);

        assertFalse(results.isEmpty());
        assertTrue(results.stream().allMatch(n -> "Computer Science".equalsIgnoreCase(n.getSubject())));
    }

    @Test
    @Order(7)
    public void testFilterByDifficulty() {
        SearchCriteria criteria = new SearchCriteria().withDifficulty("EASY");
        List<Note> results = searchService.searchNotes(criteria);

        assertFalse(results.isEmpty());
        assertTrue(results.stream().allMatch(n -> "EASY".equalsIgnoreCase(n.getDifficulty())));
        assertTrue(results.stream().anyMatch(n -> n.getId() == noteHistory.getId()));
    }

    @Test
    @Order(8)
    public void testFilterByTag() {
        SearchCriteria criteria = new SearchCriteria().withTag("derivatives");
        List<Note> results = searchService.searchNotes(criteria);

        assertFalse(results.isEmpty());
        assertTrue(results.stream().anyMatch(n -> n.getId() == noteCalculus.getId()));
    }

    @Test
    @Order(9)
    public void testFilterByDatePresetToday() {
        SearchCriteria criteria = new SearchCriteria().withDateFilter(DateFilterPreset.TODAY);
        List<Note> results = searchService.searchNotes(criteria);

        assertFalse(results.isEmpty());
        assertTrue(results.stream().anyMatch(n -> n.getId() == noteAlgo.getId()), "Note updated today should match");
    }

    @Test
    @Order(10)
    public void testFilterByDatePresetLast7Days() {
        SearchCriteria criteria = new SearchCriteria().withDateFilter(DateFilterPreset.LAST_7_DAYS);
        List<Note> results = searchService.searchNotes(criteria);

        assertTrue(results.stream().anyMatch(n -> n.getId() == noteHistory.getId()), "Note updated 3 days ago should match LAST_7_DAYS");
    }

    @Test
    @Order(11)
    public void testFilterByCustomDateRange() {
        LocalDate start = LocalDate.now().minusDays(20);
        LocalDate end = LocalDate.now().minusDays(10);

        SearchCriteria criteria = new SearchCriteria()
                .withDateFilter(DateFilterPreset.CUSTOM)
                .withCustomStartDate(start)
                .withCustomEndDate(end);

        List<Note> results = searchService.searchNotes(criteria);
        assertTrue(results.stream().anyMatch(n -> n.getId() == notePhysics.getId()), "Note updated 15 days ago should match custom range");
        assertFalse(results.stream().anyMatch(n -> n.getId() == noteAlgo.getId()), "Note updated today should not match older range");
    }

    @Test
    @Order(12)
    public void testConnectionFilterHasConnections() {
        SearchCriteria criteria = new SearchCriteria().withConnectionFilter(ConnectionFilterPreset.HAS_CONNECTIONS);
        List<Note> results = searchService.searchNotes(criteria);

        assertTrue(results.stream().anyMatch(n -> n.getId() == noteAlgo.getId()), "Connected note should match HAS_CONNECTIONS");
        assertTrue(results.stream().anyMatch(n -> n.getId() == noteCalculus.getId()), "Connected note should match HAS_CONNECTIONS");
        assertFalse(results.stream().anyMatch(n -> n.getId() == noteHistory.getId()), "Isolated note should not match HAS_CONNECTIONS");

        // Verify connection count is populated
        Note retrievedAlgo = results.stream().filter(n -> n.getId() == noteAlgo.getId()).findFirst().orElseThrow();
        assertTrue(retrievedAlgo.getConnectionCount() >= 1, "Connection count should be at least 1");
    }

    @Test
    @Order(13)
    public void testConnectionFilterNoConnections() {
        SearchCriteria criteria = new SearchCriteria().withConnectionFilter(ConnectionFilterPreset.NO_CONNECTIONS);
        List<Note> results = searchService.searchNotes(criteria);

        assertTrue(results.stream().anyMatch(n -> n.getId() == noteHistory.getId()), "Isolated note should match NO_CONNECTIONS");
        assertFalse(results.stream().anyMatch(n -> n.getId() == noteAlgo.getId()), "Connected note should not match NO_CONNECTIONS");
    }

    @Test
    @Order(14)
    public void testCombinedMultiCriteriaFilters() {
        SearchCriteria criteria = new SearchCriteria()
                .withQuery("algorithm")
                .withSubject("Computer Science")
                .withDifficulty("HARD")
                .withTag("dijkstra")
                .withConnectionFilter(ConnectionFilterPreset.HAS_CONNECTIONS);

        List<Note> results = searchService.searchNotes(criteria);

        assertEquals(1, results.size(), "Only noteAlgo should match all simultaneous filters");
        assertEquals(noteAlgo.getId(), results.get(0).getId());
    }

    @Test
    @Order(15)
    public void testCombinedCriteriaNoMatch() {
        SearchCriteria criteria = new SearchCriteria()
                .withQuery("algorithm")
                .withSubject("Mathematics"); // Computer Science note does not have Mathematics subject

        List<Note> results = searchService.searchNotes(criteria);
        assertTrue(results.isEmpty(), "Conflicting filters should return empty list");
    }

    @Test
    @Order(16)
    public void testSqlInjectionSafety() {
        SearchCriteria criteria = new SearchCriteria()
                .withQuery("' OR 1=1; DROP TABLE notes; --")
                .withSubject("'; DELETE FROM notes; --")
                .withTag("'; --");

        assertDoesNotThrow(() -> {
            List<Note> results = searchService.searchNotes(criteria);
            assertNotNull(results);
        }, "Parameterized SQL should safely handle injection payload without errors");

        // Verify notes table still exists and has entries
        assertTrue(noteRepository.count() > 0, "Notes table must remain intact");
    }

    @Test
    @Order(17)
    public void testSearchServiceMetadataMethods() {
        List<String> subjects = searchService.getAvailableSubjects();
        assertTrue(subjects.contains(SearchCriteria.ALL_SUBJECTS));
        assertTrue(subjects.contains("Computer Science"));
        assertTrue(subjects.contains("Mathematics"));

        List<String> difficulties = searchService.getAvailableDifficulties();
        assertTrue(difficulties.contains(SearchCriteria.ALL_DIFFICULTIES));
        assertTrue(difficulties.contains(Difficulty.EASY.name()));
        assertTrue(difficulties.contains(Difficulty.MEDIUM.name()));
        assertTrue(difficulties.contains(Difficulty.HARD.name()));

        List<String> tags = searchService.getAvailableTags();
        assertTrue(tags.contains(SearchCriteria.ALL_TAGS));
        assertTrue(tags.contains("graphs") || tags.contains("calculus"));

        assertEquals(5, searchService.getDateFilterPresets().size());
        assertEquals(3, searchService.getConnectionFilterPresets().size());
    }
}
