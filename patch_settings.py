import re

with open('src/main/java/com/mindmap/controller/SettingsController.java', 'r') as f:
    content = f.read()

# Replace import statements
if 'com.mindmap.util.UiUtils' not in content:
    content = content.replace('import java.util.logging.Logger;', 'import java.util.logging.Logger;\nimport com.mindmap.util.UiUtils;')

content = re.sub(r'''Alert alert = new Alert\(Alert\.AlertType\.INFORMATION\);\s*alert\.setTitle\("Export Successful"\);\s*alert\.setHeaderText\(null\);\s*alert\.setContentText\("MindMap data exported successfully\."\);\s*alert\.showAndWait\(\);''',
                 r'UiUtils.showInfo("Export Successful", "MindMap data exported successfully.");', content)

content = re.sub(r'''Alert alert = new Alert\(Alert\.AlertType\.ERROR\);\s*alert\.setTitle\("Export Failed"\);\s*alert\.setHeaderText\("Failed to export data"\);\s*alert\.setContentText\(e\.getMessage\(\)\);\s*alert\.showAndWait\(\);''',
                 r'UiUtils.showError("Export Failed", "Failed to export data: " + e.getMessage());', content)

content = re.sub(r'''Alert alert = new Alert\(Alert\.AlertType\.ERROR\);\s*alert\.setTitle\("Import Failed"\);\s*alert\.setHeaderText\("Failed to validate JSON file"\);\s*alert\.setContentText\("No existing data was modified\\.\\n" \+ e\.getMessage\(\)\);\s*alert\.showAndWait\(\);''',
                 r'UiUtils.showError("Import Failed", "Failed to validate JSON file.\\nNo existing data was modified.\\n" + e.getMessage());', content)

content = re.sub(r'''Alert alert = new Alert\(Alert\.AlertType\.INFORMATION\);\s*alert\.setTitle\("Import Successful"\);\s*alert\.setHeaderText\(null\);\s*alert\.setContentText\("Import completed successfully\\.\\nPlease navigate to Dashboard or Notes to see the changes\."\);\s*alert\.showAndWait\(\);''',
                 r'UiUtils.showInfo("Import Successful", "Import completed successfully.\\nPlease navigate to Dashboard or Notes to see the changes.");', content)

content = re.sub(r'''Alert alert = new Alert\(Alert\.AlertType\.ERROR\);\s*alert\.setTitle\("Import Failed"\);\s*alert\.setHeaderText\("An error occurred during import"\);\s*alert\.setContentText\("No existing data was modified\\.\\n" \+ e\.getMessage\(\)\);\s*alert\.showAndWait\(\);''',
                 r'UiUtils.showError("Import Failed", "An error occurred during import.\\nNo existing data was modified.\\n" + e.getMessage());', content)

content = re.sub(r'''Alert confirm = new Alert\(Alert\.AlertType\.CONFIRMATION\);\s*confirm\.setTitle\("Import Preview"\);\s*confirm\.setHeaderText\("Ready to import from " \+ file\.getName\(\)\);.*?Optional<ButtonType> result = confirm\.showAndWait\(\);\s*if \(result\.isPresent\(\) && result\.get\(\) == ButtonType\.OK\) \{\s*executeImport\(data\);\s*\}''',
                 r'''boolean proceed = UiUtils.showConfirmation("Import Preview", "Ready to import from " + file.getName() + "\\n\\n" + previewText);
                        if (proceed) {
                            executeImport(data);
                        }''', content, flags=re.DOTALL)


content = re.sub(r'''Alert alert = new Alert\(Alert\.AlertType\.INFORMATION\);\s*alert\.setTitle\("Export Successful"\);\s*alert\.setHeaderText\(null\);\s*alert\.setContentText\("All notes exported to PDF successfully\."\);\s*alert\.showAndWait\(\);''',
                 r'UiUtils.showInfo("Export Successful", "All notes exported to PDF successfully.");', content)

content = re.sub(r'''Alert alert = new Alert\(Alert\.AlertType\.ERROR\);\s*alert\.setTitle\("Export Failed"\);\s*alert\.setHeaderText\("An error occurred while generating the PDF"\);\s*alert\.setContentText\(error\.getMessage\(\)\);\s*alert\.showAndWait\(\);''',
                 r'UiUtils.showError("Export Failed", "An error occurred while generating the PDF.\\n" + error.getMessage());', content)

with open('src/main/java/com/mindmap/controller/SettingsController.java', 'w') as f:
    f.write(content)
