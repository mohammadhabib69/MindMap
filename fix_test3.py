with open('src/test/java/com/mindmap/ReviewFlowTest.java', 'r') as f:
    text = f.read()

test_logic = """
        // Check Due Today
        List<ScheduledReview> due = service.getDueReviews();
        ScheduledReview current = null;
        for (ScheduledReview r : due) {
            if (r.getNote().getTitle().equals("ReviewFlow Test Note")) {
                current = r;
                break;
            }
        }
        
        assertNotNull(current, "Test note should be in due list");
        
        System.out.println("Before update, ID: " + current.getRevision().getId() + ", date: " + current.getRevision().getReviewDate());
        
        // Complete Review
        service.completeReview(current.getRevision(), ReviewOutcome.GOOD);
        
        System.out.println("After completeReview call, ID: " + current.getRevision().getId() + ", date: " + current.getRevision().getReviewDate());
        
        // Verify from DB directly
        java.util.Optional<Revision> dbRev = revRepo.findById(current.getRevision().getId());
        System.out.println("In DB directly: date: " + dbRev.get().getReviewDate() + " interval: " + dbRev.get().getIntervalDays());

        // Re-check Due Today
        List<ScheduledReview> dueAfter = service.getDueReviews();
        boolean foundInDue = false;
        for (ScheduledReview r : dueAfter) {
            if (r.getNote().getTitle().equals("ReviewFlow Test Note")) {
                System.out.println("FOUND IN DUE AGAIN! Date in list: " + r.getRevision().getReviewDate());
                foundInDue = true;
                break;
            }
        }
        assertFalse(foundInDue, "Should not be in due list after completion");
        
        // Re-check Upcoming
        List<ScheduledReview> upcomingAfter = service.getUpcomingReviews();
        ScheduledReview currentUpcoming = null;
        for (ScheduledReview r : upcomingAfter) {
            if (r.getNote().getTitle().equals("ReviewFlow Test Note")) {
                currentUpcoming = r;
                break;
            }
        }
        assertNotNull(currentUpcoming, "Test note should be in upcoming list");
        
        // Check next interval logic
        assertEquals(3, currentUpcoming.getRevision().getIntervalDays(), "Next interval for GOOD from 1 should be 3");
        assertEquals(LocalDate.now().plusDays(3), currentUpcoming.getRevision().getReviewDate(), "Next review date should be 3 days away");
        
        // Delete test note
        noteRepo.delete(currentUpcoming.getNote().getId());
        
        System.out.println("TEST PASSED!");
"""

import re
text = re.sub(r'// Check Due Today.*?System\.out\.println\("TEST PASSED!"\);', test_logic, text, flags=re.DOTALL)

with open('src/test/java/com/mindmap/ReviewFlowTest.java', 'w') as f:
    f.write(text)
