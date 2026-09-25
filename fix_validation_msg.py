import re

with open('src/main/java/com/mindmap/controller/NoteEditorController.java', 'r') as f:
    text = f.read()

old_val = """                if (pin.isEmpty()) {
                    showError("A PIN is required for private notes.");
                    if (txtPin != null) txtPin.requestFocus();
                    if (btnSave != null) { btnSave.setDisable(false); btnSave.setText(mode == NoteEditorMode.CREATE ? "Create Note" : "Save"); }
                    return CompletableFuture.completedFuture(null);
                }
                if (!pin.equals(confirmPin)) {
                    showError("PINs do not match.");"""

new_val = """                if (pin.isEmpty()) {
                    showError("Please enter a PIN.");
                    if (txtPin != null) txtPin.requestFocus();
                    if (btnSave != null) { btnSave.setDisable(false); btnSave.setText(mode == NoteEditorMode.CREATE ? "Create Note" : "Save"); }
                    return CompletableFuture.completedFuture(null);
                }
                if (confirmPin.isEmpty()) {
                    showError("Please confirm your PIN.");
                    if (txtConfirmPin != null) txtConfirmPin.requestFocus();
                    if (btnSave != null) { btnSave.setDisable(false); btnSave.setText(mode == NoteEditorMode.CREATE ? "Create Note" : "Save"); }
                    return CompletableFuture.completedFuture(null);
                }
                if (!pin.equals(confirmPin)) {
                    showError("PINs do not match.");"""

text = text.replace(old_val, new_val)

with open('src/main/java/com/mindmap/controller/NoteEditorController.java', 'w') as f:
    f.write(text)
