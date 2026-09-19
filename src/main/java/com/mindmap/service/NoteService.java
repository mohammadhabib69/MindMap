package com.mindmap.service;

import com.mindmap.model.Difficulty;
import com.mindmap.model.Note;
import com.mindmap.repository.NoteRepository;
import com.mindmap.util.ValidationException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service managing business logic, validation, and CRUD operations for Note entities.
 */
public class NoteService {

    private final NoteRepository noteRepository;

    public NoteService() {
        this(new NoteRepository());
    }

    public NoteService(NoteRepository noteRepository) {
        this.noteRepository = noteRepository;
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

        return noteRepository.create(note);
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
        return note;
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
     * Returns the total count of notes.
     *
     * @return Total notes count.
     */
    public int getNoteCount() {
        return noteRepository.count();
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
