import re

with open('src/main/java/com/mindmap/controller/DashboardController.java', 'r') as f:
    content = f.read()

# Add lblPrivateNotes and cardPrivateNotes
content = re.sub(r'(\s*@FXML\s*private Label lblTotalConnections;)',
                 r'\1\n    @FXML\n    private Label lblPrivateNotes;\n    @FXML\n    private javafx.scene.layout.VBox cardPrivateNotes;', content)

# Populate it
load_code = """
            long privateCount = allNotes.stream().filter(com.mindmap.model.Note::isPrivate).count();
            javafx.application.Platform.runLater(() -> {
                if (lblPrivateNotes != null) lblPrivateNotes.setText(String.valueOf(privateCount));
            });
"""
content = re.sub(r'(\s*if \(lblTotalConnections != null\) lblTotalConnections\.setText\(String\.valueOf\(connectionCount\)\);\s*\})',
                 r'\1' + load_code, content)

# Add click handler
init_code = """
        if (cardPrivateNotes != null) {
            com.mindmap.util.AnimationUtil.addHoverEffect(cardPrivateNotes);
            cardPrivateNotes.setOnMouseClicked(e -> {
                // Not strictly navigating to Notes with filter because we don't have a direct routing mechanism built-in for that yet,
                // but we could just switch to Notes view.
                com.mindmap.controller.MainController.getInstance().showNotes();
            });
        }
"""
content = re.sub(r'(\s*if \(cardTotalConnections != null\) \{\s*com\.mindmap\.util\.AnimationUtil\.addHoverEffect\(cardTotalConnections\);\s*cardTotalConnections\.setOnMouseClicked\(e -> handleOpenMindMap\(\)\);\s*\})',
                 r'\1' + init_code, content)

with open('src/main/java/com/mindmap/controller/DashboardController.java', 'w') as f:
    f.write(content)
