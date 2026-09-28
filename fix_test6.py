with open('src/test/java/com/mindmap/ReviewFlowTest.java', 'r') as f:
    text = f.read()

test_logic = """
        System.out.println("Updating ID: " + current.getRevision().getId());
        boolean success = revRepo.update(current.getRevision());
        System.out.println("Update success returned: " + success);
        
        System.out.println("Revisions for note from DB directly after manual update:");
        for (Revision r : revRepo.findByNoteId(note.getId())) {
            System.out.println("ID=" + r.getId() + " date=" + r.getReviewDate());
        }
        
        System.exit(1);
"""

import re
text = re.sub(r'service\.completeReview.*?System\.exit\(1\);', test_logic, text, flags=re.DOTALL)

with open('src/test/java/com/mindmap/ReviewFlowTest.java', 'w') as f:
    f.write(text)
