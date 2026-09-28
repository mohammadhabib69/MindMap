with open('src/main/java/com/mindmap/controller/MainController.java', 'r') as f:
    text = f.read()

handler = """
    @FXML
    public void handleNavQuiz() {
        navigateTo("/fxml/quiz.fxml", btnQuiz);
    }
"""

import re
text = re.sub(r'(@FXML\s+public\s+void\s+showRevision\(\)\s*\{\s*navigateTo\("/fxml/revision\.fxml",\s*btnRevision\);\s*\})', r'\1\n' + handler, text)

with open('src/main/java/com/mindmap/controller/MainController.java', 'w') as f:
    f.write(text)
