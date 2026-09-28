with open('src/main/resources/fxml/main.fxml', 'r') as f:
    text = f.read()

target = '<Button fx:id="btnRevision" text="Revision"'
insert = '<Button fx:id="btnQuiz" text="Quiz" onAction="#handleNavQuiz" styleClass="nav-button"/>\n            '

import re
text = re.sub(r'(<Button fx:id="btnRevision"[^>]*>)\s*', r'\1\n            ' + insert, text)

with open('src/main/resources/fxml/main.fxml', 'w') as f:
    f.write(text)

with open('src/main/java/com/mindmap/controller/MainController.java', 'r') as f:
    text = f.read()

import re
text = re.sub(r'(@FXML\s+private\s+Button\s+btnRevision;)', r'\1\n    @FXML\n    private Button btnQuiz;', text)

handler = """
    @FXML
    private void handleNavQuiz() {
        navigateTo("/fxml/quiz.fxml", btnQuiz);
    }
"""
text = re.sub(r'(@FXML\s+private\s+void\s+handleNavRevision\(\)\s*\{[^}]+\})', r'\1\n' + handler, text)

# For passing context later if needed: Quiz needs to navigate inside itself (to session, then result).
# Since `ViewManager` loads into `contentArea`?
# In Quiz, when we click "Start", we need to tell `MainController` to load `/fxml/quiz_session.fxml`.
# Actually `MainController` can have a public method `loadViewInContentArea(String fxmlPath)`.
method = """
    public void loadViewInContentArea(String fxmlPath) {
        Parent view = ViewManager.loadView(fxmlPath);
        if (contentArea != null && view != null) {
            contentArea.getChildren().setAll(view);
        }
    }
"""
# insert before last }
text = re.sub(r'\}\s*$', method + '\n}', text)

with open('src/main/java/com/mindmap/controller/MainController.java', 'w') as f:
    f.write(text)
