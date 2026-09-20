import re

with open('src/main/java/com/mindmap/controller/MindMapController.java', 'r') as f:
    content = f.read()

# Imports
if 'com.mindmap.util.UiUtils' not in content:
    content = content.replace('import java.util.logging.Logger;', 'import java.util.logging.Logger;\nimport com.mindmap.util.UiUtils;')

content = re.sub(r'''showErrorAlert\("Error", "Could not open connection dialog: " \+ e\.getMessage\(\)\);''',
                 r'UiUtils.showError("Error", "Could not open connection dialog: " + e.getMessage());', content)

delete_confirm = r'''Alert alert = new Alert\(Alert\.AlertType\.CONFIRMATION\);\s*alert\.setTitle\("Delete Connection"\);\s*alert\.setHeaderText\(null\);\s*alert\.setContentText\("Are you sure you want to delete the connection '" \+ conn\.getRelation\(\) \+\s*"' between '" \+ fromTitle \+ "' and '" \+ toTitle \+ "'\?\\n\\nThis will only delete the connection\. Both notes will remain intact\."\);\s*Optional<ButtonType> result = alert\.showAndWait\(\);\s*if \(result\.isPresent\(\) && result\.get\(\) == ButtonType\.OK\) \{'''
delete_replacement = r'''boolean proceed = UiUtils.showConfirmation("Delete Connection", "Are you sure you want to delete the connection '" + conn.getRelation() +
                    "' between '" + fromTitle + "' and '" + toTitle + "'?\\n\\nThis will only delete the connection. Both notes will remain intact.");
        if (proceed) {'''
content = re.sub(delete_confirm, delete_replacement, content)

content = re.sub(r'''showErrorAlert\("Delete Failed", "Could not delete connection from database\."\);''',
                 r'UiUtils.showError("Delete Failed", "Could not delete connection from database.");', content)

# Remove showErrorAlert
content = re.sub(r'''\s*private void showErrorAlert\(String title, String message\) \{\s*Alert alert = new Alert\(Alert\.AlertType\.ERROR\);\s*alert\.setTitle\(title\);\s*alert\.setHeaderText\(null\);\s*alert\.setContentText\(message\);\s*alert\.showAndWait\(\);\s*\}''', '', content)

with open('src/main/java/com/mindmap/controller/MindMapController.java', 'w') as f:
    f.write(content)
