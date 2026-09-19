package com.mindmap.service;

import com.mindmap.model.Difficulty;
import com.mindmap.model.LearningEvent;
import com.mindmap.model.LearningEventType;
import com.mindmap.model.Note;
import com.mindmap.repository.LearningEventRepository;
import com.mindmap.repository.NoteRepository;
import com.mindmap.util.ValidationException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service managing business logic, validation, and CRUD operations for Note entities.
 */
public class NoteService {

    private static final Logger LOGGER = Logger.getLogger(NoteService.class.getName());

    private final NoteRepository noteRepository;
    private final LearningEventRepository learningEventRepository;

    public NoteService() {
        this(new NoteRepository(), new LearningEventRepository());
    }

    public NoteService(NoteRepository noteRepository) {
        this(noteRepository, new LearningEventRepository());
    }

    public NoteService(NoteRepository noteRepository, LearningEventRepository learningEventRepository) {
        this.noteRepository = noteRepository;
        this.learningEventRepository = learningEventRepository != null ? learningEventRepository : new LearningEventRepository();
    }

    /**
     * Validates and persists a new Note.
     *
     * @param note The note to create.
     * @return The created note with generated ID.
     * @throws ValidationException If note validation fails.
     */
    public Note createNote(Note note) {
        validateNote(note);

        LocalDateTime now = LocalDateTime.now();
        if (note.getCreatedAt() == null) {
            note.setCreatedAt(now);
        }
        note.setUpdatedAt(now);

        Note created = noteRepository.create(note);
        logEvent(created.getId(), LearningEventType.NOTE_CREATED.name(), "Created note: " + (created.getTitle() != null ? created.getTitle() : "Untitled"));
        return created;
    }

    /**
     * Validates and updates an existing Note.
     *
     * @param note The note containing updated fields.
     * @return The updated Note.
     * @throws ValidationException If validation fails or note does not exist.
     */
    public Note updateNote(Note note) {
        if (note.getId() <= 0) {
            throw new ValidationException("Note ID must be a positive integer to update.");
        }
        validateNote(note);

        note.setUpdatedAt(LocalDateTime.now());
        boolean success = noteRepository.update(note);
        if (!success) {
            throw new ValidationException("Note with ID " + note.getId() + " does not exist.");
        }
        logEvent(note.getId(), LearningEventType.NOTE_UPDATED.name(), "Updated note: " + (note.getTitle() != null ? note.getTitle() : "Untitled"));
        return note;
    }

    private void logEvent(int noteId, String eventType, String description) {
        try {
            if (learningEventRepository != null && noteId > 0) {
                learningEventRepository.create(new LearningEvent(noteId, eventType, description));
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to log learning event (" + eventType + ") for note " + noteId + ": " + e.getMessage());
        }
    }

    /**
     * Deletes a note by its ID.
     *
     * @param id The note ID.
     * @return True if deleted, false otherwise.
     */
    public boolean deleteNote(int id) {
        if (id <= 0) {
            return false;
        }
        return noteRepository.delete(id);
    }

    /**
     * Retrieves a note by its ID.
     *
     * @param id The note ID.
     * @return Optional containing the note if present.
     */
    public Optional<Note> getNote(int id) {
        if (id <= 0) {
            return Optional.empty();
        }
        return noteRepository.findById(id);
    }

    /**
     * Retrieves all notes in the database.
     *
     * @return List of all notes.
     */
    public List<Note> getAllNotes() {
        return noteRepository.findAll();
    }

    /**
     * Validates and persists a new Note along with its associated tags in a single transaction.
     *
     * @param note The note to persist.
     * @param tagNames The tag names to link to this note.
     * @return The persisted Note with its generated ID and tags.
     */
    public Note createNoteWithTags(Note note, List<String> tagNames) {
        validateNote(note);

        LocalDateTime now = LocalDateTime.now();
        if (note.getCreatedAt() == null) {
            note.setCreatedAt(now);
        }
        note.setUpdatedAt(now);

        List<String> cleanTagNames = sanitizeTagNames(tagNames);
        Note created = noteRepository.createWithTags(note, cleanTagNames);
        logEvent(created.getId(), LearningEventType.NOTE_CREATED.name(), "Created note: " + (created.getTitle() != null ? created.getTitle() : "Untitled"));
        return created;
    }

    /**
     * Validates and updates an existing Note and its tags in a single transaction.
     *
     * @param note The note to update.
     * @param tagNames The updated list of tag names.
     * @return The updated Note.
     */
    public Note updateNoteWithTags(Note note, List<String> tagNames) {
        if (note.getId() <= 0) {
            throw new ValidationException("Note ID must be a positive integer to update.");
        }
        validateNote(note);

        note.setUpdatedAt(LocalDateTime.now());
        List<String> cleanTagNames = sanitizeTagNames(tagNames);
        boolean success = noteRepository.updateWithTags(note, cleanTagNames);
        if (!success) {
            throw new ValidationException("Note with ID " + note.getId() + " does not exist.");
        }
        logEvent(note.getId(), LearningEventType.NOTE_UPDATED.name(), "Updated note: " + (note.getTitle() != null ? note.getTitle() : "Untitled"));
        return note;
    }

    /**
     * Retrieves all notes with their tags populated.
     */
    public List<Note> getAllNotesWithTags() {
        return noteRepository.findAllWithTags();
    }

    /**
     * Retrieves a note by ID with its tags populated.
     */
    public Optional<Note> getNoteWithTags(int id) {
        if (id <= 0) {
            return Optional.empty();
        }
        return noteRepository.findByIdWithTags(id);
    }

    /**
     * Searches notes by text in title, content, or subject (case-insensitive) with tags populated.
     */
    public List<Note> searchNotes(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllNotesWithTags();
        }
        return noteRepository.searchWithTags(query.trim());
    }

    /**
     * Filters notes associated with a specific tag name using relational tables.
     */
    public List<Note> filterNotesByTag(String tagName) {
        if (tagName == null || tagName.trim().isEmpty() || "All Tags".equalsIgnoreCase(tagName.trim())) {
            return getAllNotesWithTags();
        }
        return noteRepository.findByTagWithTags(tagName.trim());
    }

    /**
     * Performs combined text search and tag filtering.
     */
    public List<Note> searchAndFilterNotes(String query, String tagName) {
        boolean hasQuery = (query != null && !query.trim().isEmpty());
        boolean hasTag = (tagName != null && !tagName.trim().isEmpty() && !"All Tags".equalsIgnoreCase(tagName.trim()));

        if (!hasQuery && !hasTag) {
            return getAllNotesWithTags();
        }
        if (hasQuery && !hasTag) {
            return searchNotes(query);
        }
        if (!hasQuery && hasTag) {
            return filterNotesByTag(tagName);
        }
        return noteRepository.searchAndFilterWithTags(query.trim(), tagName.trim());
    }

    /**
     * Returns the total count of notes.
     *
     * @return Total notes count.
     */
    public int getNoteCount() {
        return noteRepository.count();
    }

    /**
     * Returns the count of distinct non-empty subjects across all notes.
     *
     * @return Distinct subjects count.
     */
    public int getDistinctSubjectCount() {
        return noteRepository.countDistinctSubjects();
    }

    private List<String> sanitizeTagNames(List<String> rawTagNames) {
        if (rawTagNames == null || rawTagNames.isEmpty()) {
            return List.of();
        }
        return rawTagNames.stream()
                .filter(name -> name != null && !name.trim().isEmpty())
                .map(String::trim)
                .toList();
    }

    /**
     * Validates note fields according to business rules.
     */
    private void validateNote(Note note) {
        if (note == null) {
            throw new ValidationException("Note cannot be null.");
        }

        if (note.getTitle() == null || note.getTitle().trim().isEmpty()) {
            throw new ValidationException("Note title cannot be null or empty.");
        }
        note.setTitle(note.getTitle().trim());

        if (note.getSubject() != null) {
            note.setSubject(note.getSubject().trim());
        }

        if (note.getDifficulty() != null && !note.getDifficulty().trim().isEmpty()) {
            Difficulty difficultyEnum = Difficulty.fromString(note.getDifficulty());
            if (difficultyEnum == null) {
                throw new ValidationException("Invalid difficulty: '" + note.getDifficulty() + "'. Allowed values are EASY, MEDIUM, HARD.");
            }
            note.setDifficulty(difficultyEnum.name());
        }
    }
}
