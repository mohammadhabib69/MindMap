import re

with open('src/main/java/com/mindmap/controller/SearchController.java', 'r') as f:
    content = f.read()

# Imports
if 'com.mindmap.util.UiUtils' not in content:
    content = content.replace('import java.util.logging.Logger;', 'import java.util.logging.Logger;\nimport com.mindmap.util.UiUtils;')

content = re.sub(r'''showErrorAlert\("Error", "Could not open note viewer: " \+ e\.getMessage\(\)\);''',
                 r'UiUtils.showError("Error", "Could not open note viewer: " + e.getMessage());', content)

content = re.sub(r'''showErrorAlert\("Error", "Could not open note editor: " \+ e\.getMessage\(\)\);''',
                 r'UiUtils.showError("Error", "Could not open note editor: " + e.getMessage());', content)

# Remove showErrorAlert
content = re.sub(r'''\s*private void showErrorAlert\(String title, String message\) \{\s*Alert alert = new Alert\(Alert\.AlertType\.ERROR\);\s*alert\.setTitle\(title\);\s*alert\.setHeaderText\(null\);\s*alert\.setContentText\(message\);\s*alert\.showAndWait\(\);\s*\}''', '', content)

with open('src/main/java/com/mindmap/controller/SearchController.java', 'w') as f:
    f.write(content)
