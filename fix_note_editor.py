import re

with open('src/main/java/com/mindmap/controller/NoteEditorController.java', 'r') as f:
    text = f.read()

# 1. Inject the listener into initialize()
init_code = """
        if (chkPrivate != null && boxPinFields != null) {
            chkPrivate.selectedProperty().addListener((obs, oldVal, newVal) -> {
                boxPinFields.setVisible(newVal);
                boxPinFields.setManaged(newVal);
                if (!newVal) {
                    if (txtPin != null) txtPin.clear();
                    if (txtConfirmPin != null) txtConfirmPin.clear();
                }
            });
            // trigger once manually to setup initial state
            boxPinFields.setVisible(chkPrivate.isSelected());
            boxPinFields.setManaged(chkPrivate.isSelected());
        }
"""
text = re.sub(r'(public void initialize\(\) \{)', r'\1' + init_code, text)


# 2. Fix save validation to restore button text
text = text.replace('                    if (btnSave != null) btnSave.setDisable(false);', '                    if (btnSave != null) { btnSave.setDisable(false); btnSave.setText(mode == NoteEditorMode.CREATE ? "Create Note" : "Save"); }')
text = text.replace('                    if (btnSave != null) btnSave.setDisable(false);', '                    if (btnSave != null) { btnSave.setDisable(false); btnSave.setText(mode == NoteEditorMode.CREATE ? "Create Note" : "Save"); }')

# Wait, `Note title is required` validation also needs this!
text = text.replace("""            showError("Note title is required.");
            txtTitle.requestFocus();
            return CompletableFuture.completedFuture(null);""", """            showError("Note title is required.");
            txtTitle.requestFocus();
            if (btnSave != null) { btnSave.setDisable(false); btnSave.setText(mode == NoteEditorMode.CREATE ? "Create Note" : "Save"); }
            return CompletableFuture.completedFuture(null);""")

with open('src/main/java/com/mindmap/controller/NoteEditorController.java', 'w') as f:
    f.write(text)
