package com.mindmap.service;

import com.mindmap.model.ConnectionFilterPreset;
import com.mindmap.model.DateFilterPreset;
import com.mindmap.model.Difficulty;
import com.mindmap.model.Note;
import com.mindmap.model.SearchCriteria;
import com.mindmap.model.Tag;
import com.mindmap.repository.NoteRepository;
import com.mindmap.repository.TagRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Service managing advanced search, multi-criteria filtering, and filter metadata discovery.
 */
public class SearchService {

    private final NoteRepository noteRepository;
    private final TagRepository tagRepository;

    public SearchService() {
        this(new NoteRepository(), new TagRepository());
    }

    public SearchService(NoteRepository noteRepository, TagRepository tagRepository) {
        this.noteRepository = noteRepository;
        this.tagRepository = tagRepository;
    }

    /**
     * Executes an advanced search using the provided search and filter criteria.
     *
     * @param criteria The criteria to apply (query, subject, difficulty, tag, date, connection).
     * @return List of matching Note entities.
     */
    public List<Note> searchNotes(SearchCriteria criteria) {
        if (criteria == null) {
            criteria = new SearchCriteria();
        }
        return noteRepository.searchByCriteria(criteria);
    }

    /**
     * Retrieves all available subjects dynamically from existing notes.
     * Always prepends "All Subjects".
     *
     * @return List of available subjects including "All Subjects".
     */
    public List<String> getAvailableSubjects() {
        List<String> list = new ArrayList<>();
        list.add(SearchCriteria.ALL_SUBJECTS);
        list.addAll(noteRepository.findAllDistinctSubjects());
        return list;
    }

    /**
     * Retrieves all available difficulties according to the Difficulty enum.
     * Always prepends "All Difficulties".
     *
     * @return List of difficulty options including "All Difficulties".
     */
    public List<String> getAvailableDifficulties() {
        List<String> list = new ArrayList<>();
        list.add(SearchCriteria.ALL_DIFFICULTIES);
        for (Difficulty d : Difficulty.values()) {
            list.add(d.name());
        }
        return list;
    }

    /**
     * Retrieves all available tags from the database.
     * Always prepends "All Tags".
     *
     * @return List of tag names including "All Tags".
     */
    public List<String> getAvailableTags() {
        List<String> list = new ArrayList<>();
        list.add(SearchCriteria.ALL_TAGS);
        List<Tag> tags = tagRepository.findAll();
        for (Tag tag : tags) {
            if (tag.getName() != null && !tag.getName().trim().isEmpty()) {
                list.add(tag.getName());
            }
        }
        return list;
    }

    /**
     * Retrieves all date filter preset options.
     *
     * @return List of DateFilterPreset values.
     */
    public List<DateFilterPreset> getDateFilterPresets() {
        return Arrays.asList(DateFilterPreset.values());
    }

    /**
     * Retrieves all connection filter preset options.
     *
     * @return List of ConnectionFilterPreset values.
     */
    public List<ConnectionFilterPreset> getConnectionFilterPresets() {
        return Arrays.asList(ConnectionFilterPreset.values());
    }
}
