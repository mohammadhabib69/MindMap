package com.mindmap;

import com.mindmap.database.DatabaseInitializer;
import com.mindmap.database.DatabaseManager;
import com.mindmap.model.Connection;
import com.mindmap.model.Difficulty;
import com.mindmap.model.LearningEvent;
import com.mindmap.model.LearningEventType;
import com.mindmap.model.Note;
import com.mindmap.model.Revision;
import com.mindmap.model.RevisionStatus;
import com.mindmap.model.Tag;
import com.mindmap.repository.ConnectionRepository;
import com.mindmap.repository.LearningEventRepository;
import com.mindmap.repository.NoteRepository;
import com.mindmap.repository.RevisionRepository;
import com.mindmap.repository.TagRepository;
import com.mindmap.service.NoteService;
import com.mindmap.service.TagService;
import com.mindmap.util.ValidationException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration and unit test suite verifying database tables, repositories, models, and services.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RepositoryAndServiceTest {

    private static NoteRepository noteRepository;
    private static TagRepository tagRepository;
    private static ConnectionRepository connectionRepository;
    private static RevisionRepository revisionRepository;
    private static LearningEventRepository learningEventRepository;
    private static NoteService noteService;
    private static TagService tagService;

    @BeforeAll
    public static void setUp() throws SQLException {
        DatabaseInitializer.initialize();

        noteRepository = new NoteRepository();
        tagRepository = new TagRepository();
        connectionRepository = new ConnectionRepository();
        revisionRepository = new RevisionRepository();
        learningEventRepository = new LearningEventRepository();

        noteService = new NoteService(noteRepository);
        tagService = new TagService(tagRepository);
    }

    @Test
    @Order(1)
    public void testDatabaseConnection() throws SQLException {
        try (java.sql.Connection conn = DatabaseManager.getConnection()) {
            assertNotNull(conn, "Database connection should not be null");
            assertFalse(conn.isClosed(), "Database connection should be open");
        }
    }

    @Test
    @Order(2)
    public void testDatabaseTableCreation() throws SQLException {
        Set<String> expectedTables = Set.of("notes", "tags", "note_tags", "connections", "revisions", "learning_events");
        Set<String> actualTables = new HashSet<>();

        try (java.sql.Connection conn = DatabaseManager.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            try (ResultSet rs = metaData.getTables(null, null, "%", new String[]{"TABLE"})) {
                while (rs.next()) {
                    actualTables.add(rs.getString("TABLE_NAME").toLowerCase());
                }
            }
        }

        assertTrue(actualTables.containsAll(expectedTables),
                "Database should contain all required tables. Missing: " + expectedTables);
    }

    @Test
    @Order(3)
    public void testCreateNote() {
        Note note = new Note("Discrete Mathematics Basics", "Study sets and logic proofs", "Math", Difficulty.EASY.name());
        Note created = noteService.createNote(note);

        assertTrue(created.getId() > 0, "Created note must have a positive generated ID");
        assertEquals("Discrete Mathematics Basics", created.getTitle());
        assertEquals("Math", created.getSubject());
        assertEquals("EASY", created.getDifficulty());
        assertNotNull(created.getCreatedAt());
        assertNotNull(created.getUpdatedAt());
    }

    @Test
    @Order(4)
    public void testFindNote() {
        Note note = new Note("Algorithms & Complexity", "Big-O notation and trees", "CS", Difficulty.MEDIUM.name());
        Note created = noteService.createNote(note);

        Optional<Note> found = noteService.getNote(created.getId());
        assertTrue(found.isPresent(), "Should find created note by ID");
        assertEquals("Algorithms & Complexity", found.get().getTitle());
        assertEquals("CS", found.get().getSubject());
    }

    @Test
    @Order(5)
    public void testFindAllNotes() {
        List<Note> allNotes = noteService.getAllNotes();
        assertNotNull(allNotes);
        assertFalse(allNotes.isEmpty(), "Should retrieve all notes from database");
    }

    @Test
    @Order(6)
    public void testUpdateNote() {
        Note note = new Note("Operating Systems", "Process scheduling algorithms", "CS", Difficulty.HARD.name());
        Note created = noteService.createNote(note);

        created.setContent("Updated: Virtual memory and page replacement");
        created.setDifficulty(Difficulty.MEDIUM.name());
        Note updated = noteService.updateNote(created);

        Optional<Note> fetched = noteService.getNote(updated.getId());
        assertTrue(fetched.isPresent());
        assertEquals("Updated: Virtual memory and page replacement", fetched.get().getContent());
        assertEquals("MEDIUM", fetched.get().getDifficulty());
    }

    @Test
    @Order(7)
    public void testDeleteNote() {
        Note note = new Note("Temporary Note", "Will be deleted", "Testing", Difficulty.EASY.name());
        Note created = noteService.createNote(note);
        int id = created.getId();

        boolean deleted = noteService.deleteNote(id);
        assertTrue(deleted, "Should successfully delete note");

        Optional<Note> fetched = noteService.getNote(id);
        assertTrue(fetched.isEmpty(), "Deleted note should no longer be found");
    }

    @Test
    @Order(8)
    public void testCreateAndFindTag() {
        Tag tag = tagService.createTag("Computer Science");
        assertTrue(tag.getId() > 0, "Tag should have positive ID");
        assertEquals("Computer Science", tag.getName());

        Optional<Tag> foundById = tagService.getTag(tag.getId());
        assertTrue(foundById.isPresent());
        assertEquals("Computer Science", foundById.get().getName());

        Optional<Tag> foundByName = tagService.getTagByName("Computer Science");
        assertTrue(foundByName.isPresent());
        assertEquals(tag.getId(), foundByName.get().getId());
    }

    @Test
    @Order(9)
    public void testTagUniquenessAndAssociation() {
        Tag tag1 = tagService.getOrCreateTag("Algorithms");
        Tag tag2 = tagService.getOrCreateTag("Algorithms");

        assertEquals(tag1.getId(), tag2.getId(), "Tags with identical names should return same ID");

        Note note = noteService.createNote(new Note("Sorting Analysis", "Merge sort vs Quick sort", "CS", Difficulty.MEDIUM.name()));
        tagService.addTagToNote(note.getId(), tag1.getId());

        List<Tag> noteTags = tagService.getTagsForNote(note.getId());
        assertEquals(1, noteTags.size());
        assertEquals("Algorithms", noteTags.get(0).getName());

        tagService.removeTagFromNote(note.getId(), tag1.getId());
        assertTrue(tagService.getTagsForNote(note.getId()).isEmpty());
    }

    @Test
    @Order(10)
    public void testCreateAndFindConnection() {
        Note noteA = noteService.createNote(new Note("Data Structures", "Lists, stacks, graphs", "CS", Difficulty.EASY.name()));
        Note noteB = noteService.createNote(new Note("Graph Algorithms", "Dijkstra and BFS", "CS", Difficulty.HARD.name()));

        Connection conn = new Connection(noteA.getId(), noteB.getId(), "prerequisite_for");
        Connection saved = connectionRepository.create(conn);

        assertTrue(saved.getId() > 0, "Connection should have generated ID");

        Optional<Connection> found = connectionRepository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("prerequisite_for", found.get().getRelation());

        List<Connection> connectionsForA = connectionRepository.findByNoteId(noteA.getId());
        assertFalse(connectionsForA.isEmpty());

        boolean deleted = connectionRepository.deleteBetween(noteA.getId(), noteB.getId());
        assertTrue(deleted, "Should delete connection between two notes");
    }

    @Test
    @Order(11)
    public void testRevisionRepository() {
        Note note = noteService.createNote(new Note("Spaced Repetition Note", "Testing review schedules", "Study", Difficulty.MEDIUM.name()));

        LocalDate today = LocalDate.now();
        Revision revision = new Revision(note.getId(), today, RevisionStatus.PENDING.name(), 1);
        Revision saved = revisionRepository.create(revision);

        assertTrue(saved.getId() > 0);
        assertEquals(RevisionStatus.PENDING.name(), saved.getStatus());

        List<Revision> dueRevisions = revisionRepository.findDue(today);
        assertFalse(dueRevisions.isEmpty(), "Should find revision due today");

        saved.setStatus(RevisionStatus.COMPLETED.name());
        boolean updated = revisionRepository.update(saved);
        assertTrue(updated);
    }

    @Test
    @Order(12)
    public void testLearningEventRepository() {
        Note note = noteService.createNote(new Note("Timeline Note", "Event tracking", "History", Difficulty.EASY.name()));

        LocalDateTime start = LocalDateTime.now().minusMinutes(5);
        LearningEvent event = new LearningEvent(note.getId(), LearningEventType.NOTE_CREATED.name(), "Created initial note");
        LearningEvent saved = learningEventRepository.create(event);

        assertTrue(saved.getId() > 0);

        List<LearningEvent> events = learningEventRepository.findBetween(start, LocalDateTime.now().plusMinutes(5));
        assertFalse(events.isEmpty(), "Should find recorded learning event in interval");
    }

    @Test
    @Order(13)
    public void testNoteValidationRules() {
        assertThrows(ValidationException.class, () -> {
            noteService.createNote(new Note("", "Content", "Subject", Difficulty.EASY.name()));
        }, "Empty title must throw ValidationException");

        assertThrows(ValidationException.class, () -> {
            noteService.createNote(new Note("Valid Title", "Content", "Subject", "INVALID_DIFFICULTY"));
        }, "Invalid difficulty string must throw ValidationException");
    }

    @Test
    @Order(14)
    public void testCountDistinctSubjects() {
        int initial = noteService.getDistinctSubjectCount();

        Note n1 = noteService.createNote(new Note("Subject Test 1", "Body", "QuantumPhysics", Difficulty.EASY.name()));
        assertEquals(initial + 1, noteService.getDistinctSubjectCount(), "Count should increase after adding a new distinct subject");

        Note n2 = noteService.createNote(new Note("Subject Test 2", "Body", "QuantumPhysics", Difficulty.MEDIUM.name()));
        assertEquals(initial + 1, noteService.getDistinctSubjectCount(), "Duplicate subject must not increase distinct subject count");

        Note n3 = noteService.createNote(new Note("Subject Test 3", "Body", "   ", Difficulty.HARD.name()));
        assertEquals(initial + 1, noteService.getDistinctSubjectCount(), "Empty/whitespace subject must not increase distinct subject count");

        noteService.deleteNote(n1.getId());
        noteService.deleteNote(n2.getId());
        noteService.deleteNote(n3.getId());

        assertEquals(initial, noteService.getDistinctSubjectCount(), "Count should return to initial after deleting the unique subject notes");
    }
}
