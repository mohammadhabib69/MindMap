import re

with open('src/main/java/com/mindmap/controller/ReviewDialogController.java', 'r') as f:
    content = f.read()

# Imports
if 'com.mindmap.util.UiUtils' not in content:
    content = content.replace('import java.util.logging.Logger;', 'import java.util.logging.Logger;\nimport com.mindmap.util.UiUtils;')

content = re.sub(r'''LOGGER\.log\(Level\.SEVERE, "Failed to complete review: " \+ throwable\.getMessage\(\), throwable\);''',
                 r'LOGGER.log(Level.SEVERE, "Failed to complete review: " + throwable.getMessage(), throwable);\n                    UiUtils.showError("Review Error", "Failed to save review outcome: " + throwable.getMessage());', content)

content = re.sub(r'''LOGGER\.log\(Level\.SEVERE, "Could not open note viewer: " \+ e\.getMessage\(\), e\);''',
                 r'LOGGER.log(Level.SEVERE, "Could not open note viewer: " + e.getMessage(), e);\n            UiUtils.showError("Error", "Could not open note viewer: " + e.getMessage());', content)


with open('src/main/java/com/mindmap/controller/ReviewDialogController.java', 'w') as f:
    f.write(content)
