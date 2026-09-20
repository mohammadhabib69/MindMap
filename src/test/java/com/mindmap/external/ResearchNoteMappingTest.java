package com.mindmap.external;

import com.mindmap.controller.ResearchController;
import com.mindmap.external.dto.WikipediaPageSummary;
import com.mindmap.model.Note;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ResearchNoteMappingTest {

    // Simple subclass to expose the package-private method for testing
    // without needing to initialize JavaFX Toolkit
    private static class TestableResearchController extends ResearchController {
        @Override
        public Note createDraftNote(WikipediaPageSummary summary) {
            return super.createDraftNote(summary);
        }
    }

    @Test
    void testCreateNoteFromApiResult() {
        TestableResearchController controller = new TestableResearchController();
        WikipediaPageSummary summary = new WikipediaPageSummary();
        summary.setTitle("Machine Learning");
        summary.setExtract("ML is AI.");
        summary.setPageUrl("https://en.wikipedia.org/wiki/Machine_Learning");

        Note note = controller.createDraftNote(summary);

        assertEquals("Machine Learning", note.getTitle());
        assertTrue(note.getContent().contains("ML is AI."));
        assertTrue(note.getContent().contains("https://en.wikipedia.org/wiki/Machine_Learning"));
        assertEquals("Research", note.getSubject());
        assertEquals("Medium", note.getDifficulty());
    }

    @Test
    void testCreateNoteWithMissingOptionalValues() {
        TestableResearchController controller = new TestableResearchController();
        WikipediaPageSummary summary = new WikipediaPageSummary();
        // Title is null
        // Extract is null
        // URL is null

        Note note = controller.createDraftNote(summary);

        assertEquals("Untitled", note.getTitle());
        assertEquals("", note.getContent());
        assertEquals("Research", note.getSubject());
        assertEquals("Medium", note.getDifficulty());
    }
}
