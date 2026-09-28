with open('src/test/java/com/mindmap/ReviewFlowTest.java', 'r') as f:
    text = f.read()

test_logic = """
        Note note = new Note("ReviewFlow Test Note", "content", "Test", "EASY");
        note = noteRepo.create(note);
        
        Revision rev = service.scheduleInitialReview(note.getId());
        rev.setReviewDate(LocalDate.now());
        revRepo.update(rev);
        
        System.out.println("CREATED Note ID: " + note.getId());
        System.out.println("CREATED Rev ID: " + rev.getId());
        
        List<ScheduledReview> due = service.getDueReviews();
        ScheduledReview current = null;
        for (ScheduledReview r : due) {
            if (r.getNote().getTitle().equals("ReviewFlow Test Note")) {
                current = r;
                break;
            }
        }
        
        System.out.println("FETCHED Rev ID: " + current.getRevision().getId());
        
        System.exit(1);
"""

import re
text = re.sub(r'Note note = new Note.*?System\.exit\(1\);', test_logic, text, flags=re.DOTALL)

with open('src/test/java/com/mindmap/ReviewFlowTest.java', 'w') as f:
    f.write(text)
