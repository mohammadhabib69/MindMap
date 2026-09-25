import re

with open('src/main/java/com/mindmap/controller/NotesController.java', 'r') as f:
    content = f.read()

view_code = """
        if (!com.mindmap.util.SecurityHelper.verifyPin(noteToView)) {
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/note_view.fxml"));"""
content = content.replace("""        try {\n            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/note_view.fxml"));""", view_code)


editor_code = """
        if (mode == NoteEditorMode.EDIT && note != null && !com.mindmap.util.SecurityHelper.verifyPin(note)) {
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/note_editor.fxml"));"""
content = content.replace("""        try {\n            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/note_editor.fxml"));""", editor_code)

with open('src/main/java/com/mindmap/controller/NotesController.java', 'w') as f:
    f.write(content)
