with open('src/main/java/com/mindmap/controller/NoteEditorController.java', 'r') as f:
    content = f.read()

content = content.replace('private javafx.scene.control.ToggleButton btnFavorite;', 
                          'private javafx.scene.control.ToggleButton btnFavorite;\n    @FXML\n    private javafx.scene.control.CheckBox chkPrivate;\n    @FXML\n    private javafx.scene.control.PasswordField txtPin;')

with open('src/main/java/com/mindmap/controller/NoteEditorController.java', 'w') as f:
    f.write(content)
