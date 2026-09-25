import re

with open('src/main/java/com/mindmap/controller/NoteEditorController.java', 'r') as f:
    content = f.read()

# Add fields
content = re.sub(r'(\s*@FXML\s*private ToggleButton btnFavorite;)',
                 r'\1\n    @FXML\n    private javafx.scene.control.CheckBox chkPrivate;\n    @FXML\n    private javafx.scene.control.PasswordField txtPin;', content)

# Init fields in initialize()
init_code = """
        if (chkPrivate != null && txtPin != null) {
            chkPrivate.selectedProperty().addListener((obs, oldVal, newVal) -> {
                txtPin.setVisible(newVal);
                txtPin.setManaged(newVal);
            });
        }
"""
content = re.sub(r'(\s*if \(cmbDifficulty != null\) \{)', r'\n' + init_code + r'\n\1', content)

# Populate on load
load_code = """
            if (chkPrivate != null) {
                chkPrivate.setSelected(note.isPrivate());
                if (txtPin != null && note.isPrivate()) {
                    txtPin.setText(note.getPin());
                }
            }
"""
content = re.sub(r'(\s*if \(btnFavorite != null\) \{\s*btnFavorite\.setSelected\(note\.isFavorite\(\)\);\s*updateFavoriteButtonUI\(\);\s*\})',
                 r'\1\n' + load_code, content)

# Save
save_code = """
        final boolean isPrivate = (chkPrivate != null) && chkPrivate.isSelected();
        final String pin = (txtPin != null) ? txtPin.getText().trim() : "";
        if (isPrivate && pin.isEmpty()) {
            showError("A PIN is required for private notes.");
            if (txtPin != null) txtPin.requestFocus();
            if (btnSave != null) btnSave.setDisable(false);
            return CompletableFuture.completedFuture(null);
        }
"""
content = re.sub(r'(\s*final boolean isFavorite = \(btnFavorite != null\) && btnFavorite\.isSelected\(\);)',
                 r'\1\n' + save_code, content)

content = content.replace('newNote.setFavorite(isFavorite);', 'newNote.setFavorite(isFavorite);\n                        newNote.setPrivate(isPrivate);\n                        newNote.setPin(pin);')
content = content.replace('currentNote.setFavorite(isFavorite);', 'currentNote.setFavorite(isFavorite);\n                        currentNote.setPrivate(isPrivate);\n                        currentNote.setPin(pin);')


with open('src/main/java/com/mindmap/controller/NoteEditorController.java', 'w') as f:
    f.write(content)
