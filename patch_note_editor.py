import re

with open('src/main/java/com/mindmap/controller/NoteEditorController.java', 'r') as f:
    content = f.read()

# Imports
if 'com.mindmap.util.UiUtils' not in content:
    content = content.replace('import java.util.logging.Logger;', 'import java.util.logging.Logger;\nimport com.mindmap.util.UiUtils;\nimport java.util.Objects;')

# Add fields for tracking unsaved changes
if 'private String originalTitle;' not in content:
    content = content.replace('private boolean saved = false;', '''private boolean saved = false;
    private String originalTitle = "";
    private String originalSubject = "";
    private String originalTags = "";
    private String originalContent = "";
    private String originalDifficulty = "";''')

# Store original state in setNote
content = re.sub(r'txtTitle\.clear\(\);', r'txtTitle.clear();\n                originalTitle = "";\n                originalSubject = "";\n                originalTags = "";\n                originalContent = "";\n                originalDifficulty = Difficulty.MEDIUM.name();', content)

content = re.sub(r'cmbDifficulty\.setValue\(note\.getDifficulty\(\) != null \? note\.getDifficulty\(\) : Difficulty\.MEDIUM\.name\(\)\);',
                 r'''cmbDifficulty.setValue(note.getDifficulty() != null ? note.getDifficulty() : Difficulty.MEDIUM.name());
                originalTitle = txtTitle.getText();
                originalSubject = txtSubject.getText();
                originalTags = txtTags.getText();
                originalContent = txtContent.getText();
                originalDifficulty = cmbDifficulty.getValue();''', content)

content = re.sub(r'txtContent\.setText\(note\.getContent\(\) != null \? note\.getContent\(\) : ""\);',
                 r'''txtContent.setText(note.getContent() != null ? note.getContent() : "");
            originalTitle = txtTitle.getText();
            originalSubject = txtSubject.getText();
            originalTags = txtTags.getText();
            originalContent = txtContent.getText();
            originalDifficulty = cmbDifficulty.getValue();''', content)

# Check unsaved changes in handleCancel
handle_cancel = r'''    @FXML
    private void handleCancel() {
        if (hasUnsavedChanges()) {
            boolean discard = UiUtils.showConfirmation("Unsaved Changes", "You have unsaved changes. Leave without saving?");
            if (!discard) {
                return;
            }
        }
        saved = false;
        if (dialogStage != null) {
            dialogStage.close();
        }
    }

    private boolean hasUnsavedChanges() {
        if (!Objects.equals(originalTitle, txtTitle.getText())) return true;
        if (!Objects.equals(originalSubject, txtSubject.getText())) return true;
        if (!Objects.equals(originalTags, txtTags.getText())) return true;
        if (!Objects.equals(originalContent, txtContent.getText())) return true;
        if (!Objects.equals(originalDifficulty, cmbDifficulty.getValue())) return true;
        return false;
    }'''
content = re.sub(r'''\s*@FXML\s*private void handleCancel\(\) \{\s*saved = false;\s*if \(dialogStage != null\) \{\s*dialogStage\.close\(\);\s*\}\s*\}''', '\n' + handle_cancel, content)

# Success feedback
content = re.sub(r'''this\.saved = true;\s*if \(btnSave != null\) \{''',
                 r'''this.saved = true;
                    UiUtils.showInfo(currentMode == NoteEditorMode.CREATE ? "Note Created" : "Note Updated",
                                     currentMode == NoteEditorMode.CREATE ? "Note created successfully." : "Note updated successfully.");
                    if (btnSave != null) {''', content)


with open('src/main/java/com/mindmap/controller/NoteEditorController.java', 'w') as f:
    f.write(content)
