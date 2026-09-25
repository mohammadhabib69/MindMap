import re

with open('src/main/java/com/mindmap/controller/DashboardController.java', 'r') as f:
    content = f.read()

view_code = """
        if (!com.mindmap.util.SecurityHelper.verifyPin(note)) return;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/note_view.fxml"));"""
content = content.replace("""        try {\n            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/note_view.fxml"));""", view_code)

edit_code = """
        if (!com.mindmap.util.SecurityHelper.verifyPin(note)) return;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/note_editor.fxml"));"""
content = content.replace("""        try {\n            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/note_editor.fxml"));""", edit_code)

with open('src/main/java/com/mindmap/controller/DashboardController.java', 'w') as f:
    f.write(content)
