with open('src/test/java/com/mindmap/ReviewFlowTest.java', 'r') as f:
    text = f.read()

test_logic = """
        // Create note
        Note note = new Note("ReviewFlow Test Note", "content", "Test", "EASY");
        note = noteRepo.create(note);
        
        System.out.println("Just created note. Let's see revisions:");
        for (Revision rev : revRepo.findByNoteId(note.getId())) {
            System.out.println("ID=" + rev.getId() + " date=" + rev.getReviewDate());
        }
        
        // Schedule it
        Revision rev = service.scheduleInitialReview(note.getId());
        
        System.out.println("After scheduleInitialReview:");
        for (Revision r : revRepo.findByNoteId(note.getId())) {
            System.out.println("ID=" + r.getId() + " date=" + r.getReviewDate());
        }
        
        System.exit(1);
"""

import re
text = re.sub(r'// Create note.*?System\.exit\(1\);', test_logic, text, flags=re.DOTALL)

with open('src/test/java/com/mindmap/ReviewFlowTest.java', 'w') as f:
    f.write(text)
