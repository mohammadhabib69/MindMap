import re

with open('src/main/java/com/mindmap/controller/RevisionController.java', 'r') as f:
    content = f.read()

# Imports
if 'com.mindmap.util.UiUtils' not in content:
    content = content.replace('import java.util.logging.Logger;', 'import java.util.logging.Logger;\nimport com.mindmap.util.UiUtils;')

content = re.sub(r'''LOGGER\.log\(Level\.SEVERE, "Failed to open Review Dialog: " \+ e\.getMessage\(\), e\);''',
                 r'LOGGER.log(Level.SEVERE, "Failed to open Review Dialog: " + e.getMessage(), e);\n            UiUtils.showError("Error", "Failed to open Review Dialog: " + e.getMessage());', content)

content = re.sub(r'''LOGGER\.log\(Level\.SEVERE, "Failed to open single Review Dialog: " \+ e\.getMessage\(\), e\);''',
                 r'LOGGER.log(Level.SEVERE, "Failed to open single Review Dialog: " + e.getMessage(), e);\n            UiUtils.showError("Error", "Failed to open single Review Dialog: " + e.getMessage());', content)

content = re.sub(r'''LOGGER\.log\(Level\.SEVERE, "Could not open note viewer: " \+ e\.getMessage\(\), e\);''',
                 r'LOGGER.log(Level.SEVERE, "Could not open note viewer: " + e.getMessage(), e);\n            UiUtils.showError("Error", "Could not open note viewer: " + e.getMessage());', content)

content = re.sub(r'''LOGGER\.log\(Level\.SEVERE, "Failed to open Schedule Note dialog: " \+ e\.getMessage\(\), e\);''',
                 r'LOGGER.log(Level.SEVERE, "Failed to open Schedule Note dialog: " + e.getMessage(), e);\n            UiUtils.showError("Error", "Failed to open Schedule Note dialog: " + e.getMessage());', content)


schedule_all_success = r'''Alert alert = new Alert\(Alert\.AlertType\.INFORMATION\);\s*alert\.setTitle\("Schedule Notes"\);\s*alert\.setHeaderText\(null\);\s*if \(scheduledCount > 0\) \{\s*alert\.setContentText\("Successfully added " \+ scheduledCount \+ " " \+\s*\(scheduledCount == 1 \? "note" : "notes"\) \+ " to the Spaced Repetition queue!"\);\s*\} else \{\s*alert\.setContentText\("All notes are already scheduled in the Spaced Repetition system\."\);\s*\}\s*alert\.showAndWait\(\);'''
schedule_all_replacement = r'''if (scheduledCount > 0) {
                        UiUtils.showInfo("Schedule Notes", "Successfully added " + scheduledCount + " " +
                                (scheduledCount == 1 ? "note" : "notes") + " to the Spaced Repetition queue!");
                    } else {
                        UiUtils.showInfo("Schedule Notes", "All notes are already scheduled in the Spaced Repetition system.");
                    }'''
content = re.sub(schedule_all_success, schedule_all_replacement, content)

content = re.sub(r'''LOGGER\.log\(Level\.SEVERE, "Failed to schedule all notes: " \+ throwable\.getMessage\(\), throwable\);''',
                 r'LOGGER.log(Level.SEVERE, "Failed to schedule all notes: " + throwable.getMessage(), throwable);\n                    UiUtils.showError("Error", "Failed to schedule all notes: " + throwable.getMessage());', content)

with open('src/main/java/com/mindmap/controller/RevisionController.java', 'w') as f:
    f.write(content)
