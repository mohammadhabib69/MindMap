package com.mindmap;

import com.mindmap.database.DatabaseInitializer;
import com.mindmap.model.Connection;
import com.mindmap.model.Note;
import com.mindmap.service.ConnectionService;
import com.mindmap.service.NoteService;
import com.mindmap.util.ValidationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class ConnectionsAndGraphTest {

    private ConnectionService connectionService;
    private NoteService noteService;
    private final List<Integer> createdNoteIds = new ArrayList<>();
    private final List<Integer> createdConnectionIds = new ArrayList<>();

    @BeforeAll
    static void setUpAll() throws java.sql.SQLException {
        DatabaseInitializer.initialize();
    }

    @BeforeEach
    void setUp() {
        connectionService = new ConnectionService();
        noteService = new NoteService();
    }

    @AfterEach
    void tearDown() {
        // Clean up connections first (if any remain)
        for (int connId : createdConnectionIds) {
            connectionService.deleteConnection(connId);
        }
        createdConnectionIds.clear();

        // Clean up test notes
        for (int noteId : createdNoteIds) {
            noteService.deleteNote(noteId);
        }
        createdNoteIds.clear();
    }

    private Note createTestNote(String title, String subject) {
        Note note = new Note();
        note.setTitle(title);
        note.setContent("Test content for " + title);
        note.setSubject(subject);
        note.setDifficulty("MEDIUM");
        Note created = noteService.createNote(note);
        createdNoteIds.add(created.getId());
        return created;
    }

    @Test
    void testCreateConnectionSuccess() {
        Note noteA = createTestNote("Test Graph Note A", "Computer Science");
        Note noteB = createTestNote("Test Graph Note B", "Computer Science");

        Connection connection = connectionService.createConnection(noteA.getId(), noteB.getId(), "Prerequisite");
        createdConnectionIds.add(connection.getId());

        assertTrue(connection.getId() > 0, "Connection should have generated ID");
        assertEquals(noteA.getId(), connection.getFromNoteId());
        assertEquals(noteB.getId(), connection.getToNoteId());
        assertEquals("Prerequisite", connection.getRelation());

        // Verify retrieval by ID
        Optional<Connection> retrieved = connectionService.getConnection(connection.getId());
        assertTrue(retrieved.isPresent());
        assertEquals("Prerequisite", retrieved.get().getRelation());
    }

    @Test
    void testCreateConnectionDefaultRelation() {
        Note noteA = createTestNote("Test Note X", "Math");
        Note noteB = createTestNote("Test Note Y", "Math");

        // Pass null relation; should default to "Related"
        Connection conn1 = connectionService.createConnection(noteA.getId(), noteB.getId(), null);
        createdConnectionIds.add(conn1.getId());
        assertEquals(ConnectionService.DEFAULT_RELATION, conn1.getRelation());
    }

    @Test
    void testCannotConnectNoteToSelf() {
        Note noteA = createTestNote("Self Loop Note", "Philosophy");

        ValidationException ex = assertThrows(ValidationException.class, () ->
                connectionService.createConnection(noteA.getId(), noteA.getId(), "Related"));

        assertTrue(ex.getMessage().contains("cannot be connected to itself"));
    }

    @Test
    void testCannotConnectNonExistentNotes() {
        Note noteA = createTestNote("Existing Note", "Physics");

        // Target note does not exist
        assertThrows(ValidationException.class, () ->
                connectionService.createConnection(noteA.getId(), 9999999, "Related"));

        // Source note does not exist
        assertThrows(ValidationException.class, () ->
                connectionService.createConnection(9999999, noteA.getId(), "Related"));

        // Invalid IDs
        assertThrows(ValidationException.class, () ->
                connectionService.createConnection(-1, noteA.getId(), "Related"));
        assertThrows(ValidationException.class, () ->
                connectionService.createConnection(noteA.getId(), 0, "Related"));
    }

    @Test
    void testPreventDuplicateConnections() {
        Note noteA = createTestNote("Duplicate Note Source", "Biology");
        Note noteB = createTestNote("Duplicate Note Target", "Biology");

        Connection conn = connectionService.createConnection(noteA.getId(), noteB.getId(), "Example");
        createdConnectionIds.add(conn.getId());

        // Attempt to create the exact same connection again
        ValidationException ex = assertThrows(ValidationException.class, () ->
                connectionService.createConnection(noteA.getId(), noteB.getId(), "Another Relation"));

        assertTrue(ex.getMessage().contains("already exists"));
    }

    @Test
    void testDeleteConnectionPreservesNotes() {
        Note noteA = createTestNote("Preserve Note 1", "Chemistry");
        Note noteB = createTestNote("Preserve Note 2", "Chemistry");

        Connection conn = connectionService.createConnection(noteA.getId(), noteB.getId(), "Similar");

        boolean deleted = connectionService.deleteConnection(conn.getId());
        assertTrue(deleted, "Connection deletion should succeed");

        // Connection should no longer exist
        assertTrue(connectionService.getConnection(conn.getId()).isEmpty());
        assertFalse(connectionService.connectionExists(noteA.getId(), noteB.getId()));

        // Crucial: Notes must still exist in SQLite!
        assertTrue(noteService.getNote(noteA.getId()).isPresent(), "Source note must still exist");
        assertTrue(noteService.getNote(noteB.getId()).isPresent(), "Target note must still exist");
    }

    @Test
    void testGetConnectionsForNote() {
        Note noteA = createTestNote("Center Node", "AI");
        Note noteB = createTestNote("Leaf Node 1", "AI");
        Note noteC = createTestNote("Leaf Node 2", "AI");

        Connection conn1 = connectionService.createConnection(noteA.getId(), noteB.getId(), "Extension");
        Connection conn2 = connectionService.createConnection(noteC.getId(), noteA.getId(), "Prerequisite");
        createdConnectionIds.add(conn1.getId());
        createdConnectionIds.add(conn2.getId());

        List<Connection> connections = connectionService.getConnectionsForNote(noteA.getId());
        assertEquals(2, connections.size(), "Center node should have 2 connections (1 outgoing, 1 incoming)");

        List<Connection> connectionsB = connectionService.getConnectionsForNote(noteB.getId());
        assertEquals(1, connectionsB.size(), "Leaf node 1 should have 1 incoming connection");
    }

    @Test
    void testCascadeDeleteNoteRemovesConnections() {
        Note noteA = createTestNote("Cascade Source Note", "Databases");
        Note noteB = createTestNote("Cascade Target Note", "Databases");

        Connection conn = connectionService.createConnection(noteA.getId(), noteB.getId(), "Depends On");
        int connId = conn.getId();

        // Verify connection exists
        assertTrue(connectionService.getConnection(connId).isPresent());

        // Delete Note A
        boolean noteDeleted = noteService.deleteNote(noteA.getId());
        assertTrue(noteDeleted, "Note A deletion should succeed");
        createdNoteIds.remove(Integer.valueOf(noteA.getId())); // already deleted

        // Due to SQLite FOREIGN KEY ... ON DELETE CASCADE, the connection row must be gone automatically!
        assertTrue(connectionService.getConnection(connId).isEmpty(), "Connection must be cascaded and deleted");
        assertFalse(connectionService.connectionExists(noteA.getId(), noteB.getId()));

        // Note B still exists
        assertTrue(noteService.getNote(noteB.getId()).isPresent(), "Note B must still exist");
    }

    @Test
    void testDeleteConnectionBetweenNotes() {
        Note noteA = createTestNote("Between Note 1", "History");
        Note noteB = createTestNote("Between Note 2", "History");

        Connection conn = connectionService.createConnection(noteA.getId(), noteB.getId(), "Related");
        assertTrue(connectionService.connectionExists(noteA.getId(), noteB.getId()));

        boolean deleted = connectionService.deleteConnectionBetween(noteA.getId(), noteB.getId());
        assertTrue(deleted, "deleteConnectionBetween should return true");
        assertFalse(connectionService.connectionExists(noteA.getId(), noteB.getId()));
    }

    @Test
    void testBidirectionalConnectionsAllowed() {
        Note noteA = createTestNote("Bidirectional Node 1", "Linguistics");
        Note noteB = createTestNote("Bidirectional Node 2", "Linguistics");

        // Directed edge A -> B
        Connection connAB = connectionService.createConnection(noteA.getId(), noteB.getId(), "Influences");
        createdConnectionIds.add(connAB.getId());

        // Reverse directed edge B -> A is valid in a directed graph
        Connection connBA = connectionService.createConnection(noteB.getId(), noteA.getId(), "Influenced By");
        createdConnectionIds.add(connBA.getId());

        assertTrue(connectionService.connectionExists(noteA.getId(), noteB.getId()));
        assertTrue(connectionService.connectionExists(noteB.getId(), noteA.getId()));

        assertEquals(2, connectionService.getConnectionsForNote(noteA.getId()).size());
        assertEquals(2, connectionService.getConnectionsForNote(noteB.getId()).size());
    }

    @Test
    void testFindBetweenAndConnectionCount() {
        int initialCount = connectionService.getConnectionCount();

        Note noteA = createTestNote("Count Note 1", "Economics");
        Note noteB = createTestNote("Count Note 2", "Economics");

        Connection conn = connectionService.createConnection(noteA.getId(), noteB.getId(), "Prerequisite");
        createdConnectionIds.add(conn.getId());

        assertEquals(initialCount + 1, connectionService.getConnectionCount());

        Optional<Connection> found = new com.mindmap.repository.ConnectionRepository().findBetween(noteA.getId(), noteB.getId());
        assertTrue(found.isPresent());
        assertEquals("Prerequisite", found.get().getRelation());
    }
}
