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
        
        // Complete Review
        service.completeReview(current.getRevision(), ReviewOutcome.GOOD);
        
        // Re-check Due Today
        List<ScheduledReview> dueAfter = service.getDueReviews();
        boolean foundInDue = false;
        for (ScheduledReview r : dueAfter) {
            if (r.getNote().getTitle().equals("ReviewFlow Test Note")) {
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
