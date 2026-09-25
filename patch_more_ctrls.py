import re

with open('src/main/java/com/mindmap/controller/MindMapController.java', 'r') as f:
    content = f.read()

view_code = """
        if (!com.mindmap.util.SecurityHelper.verifyPin(fresh)) return;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/note_view.fxml"));"""
content = content.replace("""        try {\n            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/note_view.fxml"));""", view_code)

with open('src/main/java/com/mindmap/controller/MindMapController.java', 'w') as f:
    f.write(content)

with open('src/main/java/com/mindmap/controller/RevisionController.java', 'r') as f:
    content = f.read()

view_code2 = """
        if (!com.mindmap.util.SecurityHelper.verifyPin(note)) return;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/note_view.fxml"));"""
content = content.replace("""        try {\n            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/note_view.fxml"));""", view_code2)

with open('src/main/java/com/mindmap/controller/RevisionController.java', 'w') as f:
    f.write(content)

