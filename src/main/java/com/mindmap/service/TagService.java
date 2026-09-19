package com.mindmap.service;

import com.mindmap.model.Tag;
import com.mindmap.repository.TagRepository;
import com.mindmap.util.ValidationException;

import java.util.List;
import java.util.Optional;

/**
 * Service managing business logic, validation, and tag associations.
 */
public class TagService {

    private final TagRepository tagRepository;

    public TagService() {
        this(new TagRepository());
    }

    public TagService(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    /**
     * Validates and creates a new Tag, or returns the existing tag if one with the same name exists.
     *
     * @param name The tag name.
     * @return The persisted Tag instance.
     */
    public Tag getOrCreateTag(String name) {
        String cleanName = validateAndCleanTagName(name);
        return tagRepository.create(new Tag(cleanName));
    }

    /**
     * Creates a new Tag explicitly.
     */
    public Tag createTag(String name) {
        return getOrCreateTag(name);
    }

    /**
     * Retrieves a tag by its ID.
     */
    public Optional<Tag> getTag(int id) {
        if (id <= 0) {
            return Optional.empty();
        }
        return tagRepository.findById(id);
    }

    /**
     * Retrieves a tag by its name.
     */
    public Optional<Tag> getTagByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return Optional.empty();
        }
        return tagRepository.findByName(name.trim());
    }

    /**
     * Retrieves all tags.
     */
    public List<Tag> getAllTags() {
        return tagRepository.findAll();
    }

    /**
     * Deletes a tag by its ID.
     */
    public boolean deleteTag(int id) {
        if (id <= 0) {
            return false;
        }
        return tagRepository.delete(id);
    }

    /**
     * Associates a tag with a note.
     */
    public void addTagToNote(int noteId, int tagId) {
        if (noteId <= 0 || tagId <= 0) {
            throw new ValidationException("Valid positive IDs required to link tag to note.");
        }
        tagRepository.addTagToNote(noteId, tagId);
    }

    /**
     * Removes a tag association from a note.
     */
    public void removeTagFromNote(int noteId, int tagId) {
        if (noteId <= 0 || tagId <= 0) {
            return;
        }
        tagRepository.removeTagFromNote(noteId, tagId);
    }

    /**
     * Retrieves all tags associated with a note.
     */
    public List<Tag> getTagsForNote(int noteId) {
        if (noteId <= 0) {
            return List.of();
        }
        return tagRepository.findTagsByNoteId(noteId);
    }

    private String validateAndCleanTagName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new ValidationException("Tag name cannot be null or empty.");
        }
        return name.trim();
    }
}
