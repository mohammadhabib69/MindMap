package com.mindmap;

import com.mindmap.database.DatabaseInitializer;
import com.mindmap.database.DatabaseManager;
import com.mindmap.model.Difficulty;
import com.mindmap.model.Note;
import com.mindmap.model.Tag;
import com.mindmap.repository.NoteRepository;
import com.mindmap.repository.TagRepository;
import com.mindmap.service.NoteService;
import com.mindmap.service.TagService;
import com.mindmap.util.ValidationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite verifying Phase 4: Notes Management, Tag System, and SQLite CRUD.
 * Uses isolated test identifiers and cleans up created test records after each test run.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class NotesCrudTest {

    private static NoteRepository noteRepository;
    private static TagRepository tagRepository;
    private static NoteService noteService;
    private static TagService tagService;

    private List<Integer> createdNoteIds;
    private String testSuffix;

    @BeforeAll
    public static void setUpAll() throws SQLException {
        DatabaseInitializer.initialize();
        tagRepository = new TagRepository();
        noteRepository = new NoteRepository(tagRepository);
        tagService = new TagService(tagRepository);
        noteService = new NoteService(noteRepository);
    }

    @BeforeEach
    public void setUp() {
        createdNoteIds = new ArrayList<>();
        testSuffix = "_" + System.nanoTime();
    }

    @AfterEach
    public void tearDown() {
        if (createdNoteIds != null) {
            for (int id : createdNoteIds) {
                try {
                    noteService.deleteNote(id);
                } catch (Exception ignored) {
                }
            }
            createdNoteIds.clear();
        }
    }

    private Note track(Note note) {
        if (note != null && note.getId() > 0) {
            createdNoteIds.add(note.getId());
        }
        return note;
    }

    @Test
    @Order(1)
    public void testCreateNoteWithMultipleTags() {
        String tag1 = "BST" + testSuffix;
        String tag2 = "Tree" + testSuffix;
        String tag3 = "Algorithms" + testSuffix;

        Note note = new Note("Binary Search Tree Invariants " + testSuffix, "Left child is smaller, right child is greater.", "CS", Difficulty.MEDIUM.name());
        Note created = track(noteService.createNoteWithTags(note, List.of(tag1, tag2, tag3)));

        assertTrue(created.getId() > 0, "Created note should receive positive ID");
        assertEquals("Binary Search Tree Invariants " + testSuffix, created.getTitle());
        assertEquals("CS", created.getSubject());
        assertEquals("MEDIUM", created.getDifficulty());

        List<Tag> noteTags = created.getTags();
        assertEquals(3, noteTags.size(), "Should have exactly 3 tags associated");
        assertTrue(created.getTagsString().contains(tag1));
        assertTrue(created.getTagsString().contains(tag2));
        assertTrue(created.getTagsString().contains(tag3));
    }

    @Test
    @Order(2)
    public void testCaseInsensitiveTagUniqueness() {
        String baseName = "CasEtAg" + testSuffix;
        Tag tag1 = tagService.getOrCreateTag(baseName.toUpperCase());
        Tag tag2 = tagService.getOrCreateTag(baseName.toLowerCase());

        assertEquals(tag1.getId(), tag2.getId(), "Tag variations in casing must reuse the existing ID");

        // De-duplicate tags in same list
        Note note = new Note("AVL Tree Balancing " + testSuffix, "Self balancing BST", "CS", Difficulty.HARD.name());
        Note created = track(noteService.createNoteWithTags(note, List.of(baseName.toUpperCase(), baseName.toLowerCase())));

        assertEquals(1, created.getTags().size(), "Case variations in note tag list should de-duplicate to 1 tag");
    }

    @Test
    @Order(3)
    public void testFindNoteWithTagsById() {
        String tag1 = "DP" + testSuffix;
        String tag2 = "Recursion" + testSuffix;

        Note note = new Note("Dynamic Programming " + testSuffix, "Optimal substructure", "Algorithms", Difficulty.HARD.name());
        Note created = track(noteService.createNoteWithTags(note, List.of(tag1, tag2)));

        Optional<Note> fetched = noteService.getNoteWithTags(created.getId());
        assertTrue(fetched.isPresent(), "Should find note with tags by ID");
        Note found = fetched.get();
        assertEquals("Dynamic Programming " + testSuffix, found.getTitle());
        assertEquals(2, found.getTags().size());
        assertTrue(found.getTagsString().contains(tag1));
        assertTrue(found.getTagsString().contains(tag2));
    }

    @Test
    @Order(4)
    public void testFindAllNotesWithTags() {
        String tag = "FindAllTag" + testSuffix;
        track(noteService.createNoteWithTags(new Note("FindAll Note " + testSuffix, "Content", "CS", Difficulty.EASY.name()), List.of(tag)));

        List<Note> allNotes = noteService.getAllNotesWithTags();
        assertNotNull(allNotes);
        assertFalse(allNotes.isEmpty());

        boolean foundOurNote = allNotes.stream().anyMatch(n -> n.getTagsString().contains(tag));
        assertTrue(foundOurNote, "Should find created note with populated tag in findAllWithTags");
    }

    @Test
    @Order(5)
    public void testUpdateNoteAndTags() {
        String tagInitial = "InitialTag" + testSuffix;
        String tagKept = "KeptTag" + testSuffix;
        String tagNew = "NewTag" + testSuffix;

        Note note = track(noteService.createNoteWithTags(new Note("Original Title " + testSuffix, "Original Content", "Math", Difficulty.EASY.name()), List.of(tagInitial, tagKept)));

        // Update note fields and tags
        note.setTitle("Updated Title " + testSuffix);
        note.setDifficulty(Difficulty.HARD.name());
        note.setContent("Updated Content");

        Note updated = noteService.updateNoteWithTags(note, List.of(tagKept, tagNew));
        assertEquals("Updated Title " + testSuffix, updated.getTitle());
        assertEquals("HARD", updated.getDifficulty());

        Optional<Note> fetched = noteService.getNoteWithTags(updated.getId());
        assertTrue(fetched.isPresent());
        Note fetchedNote = fetched.get();
        assertEquals(2, fetchedNote.getTags().size());
        assertTrue(fetchedNote.getTagsString().contains(tagKept));
        assertTrue(fetchedNote.getTagsString().contains(tagNew));
        assertFalse(fetchedNote.getTagsString().contains(tagInitial), "Removed tag must no longer be associated");
    }

    @Test
    @Order(6)
    public void testDeleteNoteCascadesToNoteTags() throws SQLException {
        String tag1 = "DelTag1" + testSuffix;
        String tag2 = "DelTag2" + testSuffix;

        Note note = noteService.createNoteWithTags(new Note("Temp Note " + testSuffix, "To be deleted", "Testing", Difficulty.EASY.name()), List.of(tag1, tag2));
        int noteId = note.getId();

        // Verify note_tags has entries
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT count(*) FROM note_tags WHERE note_id = ?")) {
            stmt.setInt(1, noteId);
            try (ResultSet rs = stmt.executeQuery()) {
                assertTrue(rs.next());
                assertEquals(2, rs.getInt(1));
            }
        }

        // Delete note
        boolean deleted = noteService.deleteNote(noteId);
        assertTrue(deleted);

        // Verify note is gone
        Optional<Note> fetched = noteService.getNoteWithTags(noteId);
        assertTrue(fetched.isEmpty());

        // Verify note_tags records were cascade-deleted
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT count(*) FROM note_tags WHERE note_id = ?")) {
            stmt.setInt(1, noteId);
            try (ResultSet rs = stmt.executeQuery()) {
                assertTrue(rs.next());
                assertEquals(0, rs.getInt(1), "note_tags must be cascade-deleted");
            }
        }
    }

    @Test
    @Order(7)
    public void testSearchNotes() {
        String uniqueKeyword = "SearchKey" + testSuffix;
        track(noteService.createNoteWithTags(new Note("Title With " + uniqueKeyword, "Regular content", "CS", Difficulty.MEDIUM.name()), List.of()));
        track(noteService.createNoteWithTags(new Note("Another Note", "Content containing " + uniqueKeyword + " inside", "CS", Difficulty.MEDIUM.name()), List.of()));
        track(noteService.createNoteWithTags(new Note("Subject Note", "Regular content", uniqueKeyword, Difficulty.MEDIUM.name()), List.of()));

        // Search by keyword matching title, content, and subject
        List<Note> results = noteService.searchNotes(uniqueKeyword);
        assertEquals(3, results.size(), "Search should find notes matching across title, content, or subject");

        // Case-insensitive search
        List<Note> lowerResults = noteService.searchNotes(uniqueKeyword.toLowerCase());
        assertEquals(3, lowerResults.size(), "Search should be case-insensitive");
    }

    @Test
    @Order(8)
    public void testFilterNotesByTag() {
        String targetTag = "FilterTag" + testSuffix;
        String otherTag = "OtherTag" + testSuffix;

        track(noteService.createNoteWithTags(new Note("Note A " + testSuffix, "Content A", "CS", Difficulty.EASY.name()), List.of(targetTag, otherTag)));
        track(noteService.createNoteWithTags(new Note("Note B " + testSuffix, "Content B", "CS", Difficulty.EASY.name()), List.of(targetTag)));
        track(noteService.createNoteWithTags(new Note("Note C " + testSuffix, "Content C", "CS", Difficulty.EASY.name()), List.of(otherTag)));

        List<Note> filtered = noteService.filterNotesByTag(targetTag);
        assertEquals(2, filtered.size(), "Should find exactly the 2 notes tagged with targetTag");
        assertTrue(filtered.stream().allMatch(n -> n.getTagsString().contains(targetTag)));

        // Case-insensitive tag filter
        List<Note> lowerFiltered = noteService.filterNotesByTag(targetTag.toLowerCase());
        assertEquals(2, lowerFiltered.size(), "Tag filter should be case-insensitive");
    }

    @Test
    @Order(9)
    public void testCombinedSearchAndFilter() {
        String tag = "CombineTag" + testSuffix;
        track(noteService.createNoteWithTags(new Note("Merge Sort Algorithm " + testSuffix, "Divide and conquer", "Algorithms", Difficulty.MEDIUM.name()), List.of(tag)));
        track(noteService.createNoteWithTags(new Note("Quick Sort Algorithm " + testSuffix, "Partitioning", "Algorithms", Difficulty.MEDIUM.name()), List.of(tag)));
        track(noteService.createNoteWithTags(new Note("Bubble Sort " + testSuffix, "Quadratic sorting", "Algorithms", Difficulty.EASY.name()), List.of()));

        // Search "Quick" with tag
        List<Note> results = noteService.searchAndFilterNotes("Quick", tag);
        assertEquals(1, results.size());
        assertTrue(results.get(0).getTitle().contains("Quick Sort"));

        // Search "Sort" with tag (matches Merge and Quick, but NOT Bubble because Bubble does not have the tag)
        List<Note> sortResults = noteService.searchAndFilterNotes("Sort", tag);
        assertEquals(2, sortResults.size());
        assertTrue(sortResults.stream().noneMatch(n -> n.getTitle().contains("Bubble Sort")));
    }

    @Test
    @Order(10)
    public void testEmptySearchResults() {
        List<Note> results = noteService.searchNotes("NonExistentString_99999" + testSuffix);
        assertNotNull(results);
        assertTrue(results.isEmpty());

        List<Note> tagResults = noteService.filterNotesByTag("NonExistentTag_99999" + testSuffix);
        assertNotNull(tagResults);
        assertTrue(tagResults.isEmpty());
    }

    @Test
    @Order(11)
    public void testNoteValidation() {
        assertThrows(ValidationException.class, () -> {
            noteService.createNoteWithTags(new Note("", "Body", "Subject", "EASY"), List.of());
        });

        assertThrows(ValidationException.class, () -> {
            noteService.createNoteWithTags(new Note("   ", "Body", "Subject", "EASY"), List.of());
        });

        assertThrows(ValidationException.class, () -> {
            noteService.createNoteWithTags(new Note("Valid Title", "Body", "Subject", "INVALID_DIFFICULTY"), List.of());
        });
    }

    @Test
    @Order(12)
    public void testNoteCount() {
        int initialCount = noteService.getNoteCount();
        Note note = track(noteService.createNoteWithTags(new Note("Count Test " + testSuffix, "Testing count", "Count", "EASY"), List.of()));
        assertEquals(initialCount + 1, noteService.getNoteCount());

        noteService.deleteNote(note.getId());
        createdNoteIds.remove(Integer.valueOf(note.getId()));
        assertEquals(initialCount, noteService.getNoteCount());
    }

    @Test
    @Order(13)
    public void testTransactionRollbackOnError() {
        int initialNoteCount = noteService.getNoteCount();

        assertThrows(ValidationException.class, () -> {
            noteService.createNoteWithTags(new Note(null, "Rollback Content", "Subject", "EASY"), List.of("Tag1"));
        });
        assertEquals(initialNoteCount, noteService.getNoteCount(), "Note count must remain unchanged after validation error");
    }
}
