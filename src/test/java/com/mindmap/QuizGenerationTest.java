package com.mindmap;

import com.mindmap.model.Note;
import com.mindmap.model.BankQuestion;
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
        
        List<BankQuestion> qs = service.generateQuestions(notes, notes, 5, "Mixed");
        
        assertEquals(5, qs.size(), "Should generate 5 questions");
        
        for (BankQuestion q : qs) {
            assertNotNull(q.getQuestionText());
            assertNotNull(q.getOptionsJson());
            assertNotNull(q.getCorrectAnswerJson());
            assertNotNull(q.getQuestionType());
            assertNotNull(q.getExplanation());
            assertTrue(q.getSourceNoteId() > 0);
        }
    }
}
