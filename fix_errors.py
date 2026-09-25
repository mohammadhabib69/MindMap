import re

# Fix DashboardController
with open('src/main/java/com/mindmap/controller/DashboardController.java', 'r') as f:
    content = f.read()
# In handleNewNote we injected verifyPin(note) but note is not defined. We just remove it.
# Wait, handleNewNote doesn't have `note`.
content = content.replace("""        if (!com.mindmap.util.SecurityHelper.verifyPin(note)) return;\n        try {\n            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/note_editor.fxml"));\n            Parent root = loader.load();\n\n            NoteEditorController controller = loader.getController();\n            controller.setNoteService(noteService);\n\n            controller.setMode(NoteEditorMode.CREATE);""", """        try {\n            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/note_editor.fxml"));\n            Parent root = loader.load();\n\n            NoteEditorController controller = loader.getController();\n            controller.setNoteService(noteService);\n\n            controller.setMode(NoteEditorMode.CREATE);""")
# Let's just strip verifyPin out of handleNewNote
content = re.sub(r'public void handleNewNote\(\)\s*\{\s*if \(\!com\.mindmap\.util\.SecurityHelper\.verifyPin\(note\)\) return;', r'public void handleNewNote() {\n', content)

with open('src/main/java/com/mindmap/controller/DashboardController.java', 'w') as f:
    f.write(content)


# Fix NoteEditorController
with open('src/main/java/com/mindmap/controller/NoteEditorController.java', 'r') as f:
    content = f.read()

# Make sure we actually inject the fields.
if 'chkPrivate' not in content.split('public void initialize')[0]:
    content = content.replace('    @FXML\n    private ToggleButton btnFavorite;', '    @FXML\n    private ToggleButton btnFavorite;\n    @FXML\n    private javafx.scene.control.CheckBox chkPrivate;\n    @FXML\n    private javafx.scene.control.PasswordField txtPin;')

with open('src/main/java/com/mindmap/controller/NoteEditorController.java', 'w') as f:
    f.write(content)
