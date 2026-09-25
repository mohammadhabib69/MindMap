import re

with open('src/main/java/com/mindmap/controller/DashboardController.java', 'r') as f:
    text = f.read()

text = re.sub(r'    @FXML\s*private Label lblPrivateNotes;\s*', '', text)
text = re.sub(r'    @FXML\s*private javafx\.scene\.layout\.VBox cardPrivateNotes;\s*', '', text)

text = re.sub(r'\s*long privateCount = allNotes\.stream\(\)\.filter\(com\.mindmap\.model\.Note::isPrivate\)\.count\(\);\s*javafx\.application\.Platform\.runLater\(\(\) -> \{\s*if \(lblPrivateNotes != null\) lblPrivateNotes\.setText\(String\.valueOf\(privateCount\)\);\s*\}\);\s*', '\n', text)

text = re.sub(r'\s*if \(cardPrivateNotes != null\) \{\s*com\.mindmap\.util\.AnimationUtil\.addHoverEffect\(cardPrivateNotes\);\s*cardPrivateNotes\.setOnMouseClicked\(e -> \{\s*// Not strictly navigating.*\s*// but we could.*\s*com\.mindmap\.controller\.MainController\.getInstance\(\)\.showNotes\(\);\s*\}\);\s*\}\s*', '\n', text)

with open('src/main/java/com/mindmap/controller/DashboardController.java', 'w') as f:
    f.write(text)
