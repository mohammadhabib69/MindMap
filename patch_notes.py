import re

with open('src/main/java/com/mindmap/controller/NotesController.java', 'r') as f:
    content = f.read()

# Imports
if 'com.mindmap.util.UiUtils' not in content:
    content = content.replace('import java.util.logging.Logger;', 'import java.util.logging.Logger;\nimport com.mindmap.util.UiUtils;')

content = re.sub(r'''showErrorAlert\("Error", "Could not open note viewer: " \+ e\.getMessage\(\)\);''',
                 r'UiUtils.showError("Error", "Could not open note viewer: " + e.getMessage());', content)

content = re.sub(r'''showErrorAlert\("Error", "Could not open note editor: " \+ e\.getMessage\(\)\);''',
                 r'UiUtils.showError("Error", "Could not open note editor: " + e.getMessage());', content)

# Delete confirmation
content = re.sub(r'''Alert confirmAlert = new Alert\(Alert\.AlertType\.CONFIRMATION\);\s*confirmAlert\.setTitle\("Delete Note"\);\s*confirmAlert\.setHeaderText\("Delete \\"" \+ selected\.getTitle\(\) \+ "\\"\?"\);\s*confirmAlert\.setContentText\("Are you sure you want to permanently delete this note\? This action cannot be undone\."\);\s*Optional<ButtonType> result = confirmAlert\.showAndWait\(\);\s*if \(result\.isPresent\(\) && result\.get\(\) == ButtonType\.OK\) \{''',
                 r'''boolean proceed = UiUtils.showConfirmation("Delete Note", "Delete this note?\\n\\nDeleting this note may also remove its associated connections and related data.");
        if (proceed) {''', content)

content = re.sub(r'''showErrorAlert\("Delete Failed", "The note could not be deleted from the database\."\);''',
                 r'UiUtils.showError("Delete Failed", "The note could not be deleted from the database.");', content)

content = re.sub(r'''showErrorAlert\("Database Error", "An error occurred while deleting the note\."\);''',
                 r'UiUtils.showError("Database Error", "An error occurred while deleting the note.");', content)

# Remove showErrorAlert
content = re.sub(r'''\s*private void showErrorAlert\(String title, String message\) \{\s*Alert alert = new Alert\(Alert\.AlertType\.ERROR\);\s*alert\.setTitle\(title\);\s*alert\.setHeaderText\(null\);\s*alert\.setContentText\(message\);\s*alert\.showAndWait\(\);\s*\}''', '', content)


# PDF export
content = re.sub(r'''javafx\.scene\.control\.Alert alert = new javafx\.scene\.control\.Alert\(javafx\.scene\.control\.Alert\.AlertType\.INFORMATION\);\s*alert\.setTitle\("Export Successful"\);\s*alert\.setHeaderText\(null\);\s*alert\.setContentText\("PDF exported successfully\."\);\s*alert\.showAndWait\(\);''',
                 r'UiUtils.showInfo("Export Successful", "PDF exported successfully.");', content)

content = re.sub(r'''javafx\.scene\.control\.Alert alert = new javafx\.scene\.control\.Alert\(javafx\.scene\.control\.Alert\.AlertType\.ERROR\);\s*alert\.setTitle\("Export Failed"\);\s*alert\.setHeaderText\("An error occurred while generating the PDF"\);\s*alert\.setContentText\(error\.getMessage\(\)\);\s*alert\.showAndWait\(\);''',
                 r'UiUtils.showError("Export Failed", "Unable to export the PDF. Please choose another location.\\n" + error.getMessage());', content)


with open('src/main/java/com/mindmap/controller/NotesController.java', 'w') as f:
    f.write(content)
