with open('src/test/java/com/mindmap/ReviewFlowTest.java', 'r') as f:
    text = f.read()

test_logic = """
        // Re-check Due Today
        List<ScheduledReview> dueAfter = service.getDueReviews();
        for (ScheduledReview r : dueAfter) {
            if (r.getNote().getTitle().equals("ReviewFlow Test Note")) {
                System.out.println("FOUND IN DUE AGAIN! ID in list: " + r.getRevision().getId() + " Date in list: " + r.getRevision().getReviewDate());
            }
        }
        
        System.out.println("Number of due reviews: " + dueAfter.size());
        
        // Print all revisions for this note
        List<Revision> allRevs = revRepo.findByNoteId(current.getNote().getId());
        for (Revision rev : allRevs) {
            System.out.println("Revision in DB: ID=" + rev.getId() + " date=" + rev.getReviewDate() + " status=" + rev.getStatus());
        }
        
        System.exit(1);
"""
import re
text = re.sub(r'// Re-check Due Today.*?System\.exit\(1\);', test_logic, text, flags=re.DOTALL)

with open('src/test/java/com/mindmap/ReviewFlowTest.java', 'w') as f:
    f.write(text)
