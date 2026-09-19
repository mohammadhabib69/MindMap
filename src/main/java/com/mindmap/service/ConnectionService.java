package com.mindmap.service;

import com.mindmap.model.Connection;
import com.mindmap.model.Note;
import com.mindmap.repository.ConnectionRepository;
import com.mindmap.repository.NoteRepository;
import com.mindmap.util.ValidationException;

import java.util.List;
import java.util.Optional;

/**
 * Service managing business logic, validation, and operations for note-to-note connections (graph edges).
 */
public class ConnectionService {

    public static final String DEFAULT_RELATION = "Related";
    public static final List<String> STANDARD_RELATIONS = List.of(
            "Related",
            "Prerequisite",
            "Example",
            "Extension",
            "Depends On",
            "Similar"
    );

    private final ConnectionRepository connectionRepository;
    private final NoteRepository noteRepository;

    public ConnectionService() {
        this(new ConnectionRepository(), new NoteRepository());
    }

    public ConnectionService(ConnectionRepository connectionRepository, NoteRepository noteRepository) {
        this.connectionRepository = connectionRepository;
        this.noteRepository = noteRepository;
    }

    /**
     * Validates and persists a directed connection between two notes.
     *
     * @param fromNoteId The ID of the source note.
     * @param toNoteId   The ID of the target note.
     * @param relation   The relationship label (e.g. "Related", "Prerequisite"). Defaults to "Related" if blank.
     * @return The created Connection with its generated ID.
     * @throws ValidationException If validation fails (e.g. self-connection, missing notes, duplicate connection).
     */
    public Connection createConnection(int fromNoteId, int toNoteId, String relation) {
        validateConnectionParameters(fromNoteId, toNoteId);

        String cleanRelation = normalizeRelation(relation);
        Connection connection = new Connection(fromNoteId, toNoteId, cleanRelation);
        return connectionRepository.create(connection);
    }

    /**
     * Validates and persists a Connection entity.
     *
     * @param connection The connection to persist.
     * @return The persisted Connection with generated ID.
     * @throws ValidationException If validation fails.
     */
    public Connection createConnection(Connection connection) {
        if (connection == null) {
            throw new ValidationException("Connection cannot be null.");
        }
        return createConnection(connection.getFromNoteId(), connection.getToNoteId(), connection.getRelation());
    }

    /**
     * Deletes a connection by its ID.
     *
     * @param id The connection ID.
     * @return True if deleted, false otherwise.
     */
    public boolean deleteConnection(int id) {
        if (id <= 0) {
            return false;
        }
        return connectionRepository.delete(id);
    }

    /**
     * Deletes a directed relationship between two notes.
     *
     * @param fromNoteId Source note ID.
     * @param toNoteId   Target note ID.
     * @return True if deleted, false otherwise.
     */
    public boolean deleteConnectionBetween(int fromNoteId, int toNoteId) {
        if (fromNoteId <= 0 || toNoteId <= 0) {
            return false;
        }
        return connectionRepository.deleteBetween(fromNoteId, toNoteId);
    }

    /**
     * Retrieves a connection by ID.
     *
     * @param id The connection ID.
     * @return Optional containing the Connection if found.
     */
    public Optional<Connection> getConnection(int id) {
        if (id <= 0) {
            return Optional.empty();
        }
        return connectionRepository.findById(id);
    }

    /**
     * Retrieves all connections in the knowledge graph.
     *
     * @return List of all connections.
     */
    public List<Connection> getAllConnections() {
        return connectionRepository.findAll();
    }

    /**
     * Retrieves all connections where the specified note is either the source or the target.
     *
     * @param noteId The note ID.
     * @return List of connections involving the note.
     */
    public List<Connection> getConnectionsForNote(int noteId) {
        if (noteId <= 0) {
            return List.of();
        }
        return connectionRepository.findByNoteId(noteId);
    }

    /**
     * Checks if a directed connection exists between two notes.
     *
     * @param fromNoteId Source note ID.
     * @param toNoteId   Target note ID.
     * @return True if a connection exists, false otherwise.
     */
    public boolean connectionExists(int fromNoteId, int toNoteId) {
        if (fromNoteId <= 0 || toNoteId <= 0) {
            return false;
        }
        return connectionRepository.exists(fromNoteId, toNoteId);
    }

    /**
     * Returns the total count of connections in the graph.
     *
     * @return Total count.
     */
    public int getConnectionCount() {
        return connectionRepository.count();
    }

    private void validateConnectionParameters(int fromNoteId, int toNoteId) {
        if (fromNoteId <= 0) {
            throw new ValidationException("Source note must be selected.");
        }
        if (toNoteId <= 0) {
            throw new ValidationException("Target note must be selected.");
        }
        if (fromNoteId == toNoteId) {
            throw new ValidationException("A note cannot be connected to itself.");
        }

        // Verify that both notes exist in the database
        Optional<Note> fromNote = noteRepository.findById(fromNoteId);
        if (fromNote.isEmpty()) {
            throw new ValidationException("Source note with ID " + fromNoteId + " does not exist.");
        }

        Optional<Note> toNote = noteRepository.findById(toNoteId);
        if (toNote.isEmpty()) {
            throw new ValidationException("Target note with ID " + toNoteId + " does not exist.");
        }

        // Prevent duplicate connections
        if (connectionRepository.exists(fromNoteId, toNoteId)) {
            throw new ValidationException("A connection from '" + fromNote.get().getTitle()
                    + "' to '" + toNote.get().getTitle() + "' already exists.");
        }
    }

    private String normalizeRelation(String relation) {
        if (relation == null || relation.trim().isEmpty()) {
            return DEFAULT_RELATION;
        }
        return relation.trim();
    }
}
