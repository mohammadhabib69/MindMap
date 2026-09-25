import re

# MainController
with open('src/main/java/com/mindmap/controller/MainController.java', 'r') as f:
    content = f.read()

view_code = """                    if (!com.mindmap.util.SecurityHelper.verifyPin(selected)) return;
                    try {
                        javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/note_view.fxml"));"""
content = content.replace("""                    try {\n                        javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/note_view.fxml"));""", view_code)

with open('src/main/java/com/mindmap/controller/MainController.java', 'w') as f:
    f.write(content)

# TimelineController
with open('src/main/java/com/mindmap/controller/TimelineController.java', 'r') as f:
    content = f.read()

view_code2 = """
        if (!com.mindmap.util.SecurityHelper.verifyPin(note)) return;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/note_view.fxml"));"""
content = content.replace("""        try {\n            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/note_view.fxml"));""", view_code2)

with open('src/main/java/com/mindmap/controller/TimelineController.java', 'w') as f:
    f.write(content)

