import re

with open('src/main/java/com/mindmap/controller/NoteEditorController.java', 'r') as f:
    content = f.read()

replacement = '''public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
        this.dialogStage.setOnCloseRequest(event -> {
            if (hasUnsavedChanges()) {
                boolean discard = UiUtils.showConfirmation("Unsaved Changes", "You have unsaved changes. Leave without saving?");
                if (!discard) {
                    event.consume(); // Cancel the close request
                }
            }
        });
    }'''

content = re.sub(r'public void setDialogStage\(Stage dialogStage\) \{\s*this\.dialogStage = dialogStage;\s*\}', replacement, content)

with open('src/main/java/com/mindmap/controller/NoteEditorController.java', 'w') as f:
    f.write(content)
