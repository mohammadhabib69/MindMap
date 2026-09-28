package com.mindmap;

import com.mindmap.model.Note;
import com.mindmap.model.QuizConfiguration;
import com.mindmap.model.QuizQuestion;
import com.mindmap.service.QuizGenerationService;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class QuizGenerationTest {

    @Test
    public void testGenerateQuiz() {
        QuizGenerationService service = new QuizGenerationService();
        List<Note> notes = new ArrayList<>();
        
        for (int i = 0; i < 5; i++) {
            Note n = new Note("Title " + i, "This is the first sentence of note " + i + ". This is the second sentence. This is the third sentence.", "SubjectA", "EASY");
            n.setId(i + 1);
            notes.add(n);
        }
        
        QuizConfiguration config = new QuizConfiguration("SubjectA", "All Topics", "EASY", 5, "Mixed");
        
        List<QuizQuestion> qs = service.generateQuiz(notes, config);
        
        assertEquals(5, qs.size(), "Should generate 5 questions");
        
        for (QuizQuestion q : qs) {
            assertNotNull(q.getQuestionText());
            assertNotNull(q.getOptionsJson());
            assertNotNull(q.getCorrectAnswerJson());
            assertNotNull(q.getQuestionType());
            assertEquals(0, q.getQuizSessionId(), "We haven't set session ID yet, so it defaults to 0 but wait we didn't check that.");
            // Actually it defaults to 0 before DB.
        }
    }
}
