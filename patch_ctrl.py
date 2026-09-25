import re

with open('src/main/java/com/mindmap/controller/NoteEditorController.java', 'r') as f:
    content = f.read()

# Add new fields
content = content.replace('    private javafx.scene.control.PasswordField txtPin;', 
                          '    private javafx.scene.control.PasswordField txtPin;\n    @FXML\n    private javafx.scene.control.PasswordField txtConfirmPin;\n    @FXML\n    private javafx.scene.layout.VBox boxPinFields;')

# Fix initialization
old_init = """        if (chkPrivate != null && txtPin != null) {
            chkPrivate.selectedProperty().addListener((obs, oldVal, newVal) -> {
                txtPin.setVisible(newVal);
                txtPin.setManaged(newVal);
            });
        }"""
new_init = """        if (chkPrivate != null && boxPinFields != null) {
            chkPrivate.selectedProperty().addListener((obs, oldVal, newVal) -> {
                boxPinFields.setVisible(newVal);
                boxPinFields.setManaged(newVal);
                if (!newVal) {
                    if (txtPin != null) txtPin.clear();
                    if (txtConfirmPin != null) txtConfirmPin.clear();
                }
            });
        }"""
content = content.replace(old_init, new_init)

# Fix population logic (we shouldn't put raw PIN back in the text field if we hash it, but wait, the instructions said: "Never store plaintext PIN." and "Allow changing the PIN only after successful authentication.")
# So when editing, we shouldn't populate txtPin. The user has to enter a new PIN if they want to change it? Or leave it blank to keep the old one?
# "Allow changing the PIN only after successful authentication... If Private Note is disabled, require confirmation..." 
# We already challenged them with the PIN *before* opening the editor! So they are authenticated.
# So if it's already a private note, the checkbox is checked. The PIN fields are empty. If they leave them empty, we keep the old PIN. If they type a new one, we update it.

old_load = """            if (chkPrivate != null) {
                chkPrivate.setSelected(note.isPrivate());
                if (txtPin != null && note.isPrivate()) {
                    txtPin.setText(note.getPin());
                }
            }"""
new_load = """            if (chkPrivate != null) {
                chkPrivate.setSelected(note.isPrivate());
                if (txtPin != null && note.isPrivate()) {
                    txtPin.setPromptText("Leave blank to keep existing PIN");
                }
            }"""
content = content.replace(old_load, new_load)

# Fix save validation
old_save = """        final boolean isPrivate = (chkPrivate != null) && chkPrivate.isSelected();
        final String pin = (txtPin != null) ? txtPin.getText().trim() : "";
        if (isPrivate && pin.isEmpty()) {
            showError("A PIN is required for private notes.");
            if (txtPin != null) txtPin.requestFocus();
            if (btnSave != null) btnSave.setDisable(false);
            return CompletableFuture.completedFuture(null);
        }"""

new_save = """        final boolean isPrivate = (chkPrivate != null) && chkPrivate.isSelected();
        String pin = (txtPin != null) ? txtPin.getText().trim() : "";
        String confirmPin = (txtConfirmPin != null) ? txtConfirmPin.getText().trim() : "";
        
        if (isPrivate) {
            if (mode == NoteEditorMode.CREATE || (mode == NoteEditorMode.EDIT && !pin.isEmpty())) {
                if (pin.isEmpty()) {
                    showError("A PIN is required for private notes.");
                    if (txtPin != null) txtPin.requestFocus();
                    if (btnSave != null) btnSave.setDisable(false);
                    return CompletableFuture.completedFuture(null);
                }
                if (!pin.equals(confirmPin)) {
                    showError("PINs do not match.");
                    if (txtConfirmPin != null) txtConfirmPin.requestFocus();
                    if (btnSave != null) btnSave.setDisable(false);
                    return CompletableFuture.completedFuture(null);
                }
            }
        }
        
        String finalPinToSave = null;
        if (isPrivate) {
            if (mode == NoteEditorMode.CREATE || (mode == NoteEditorMode.EDIT && !pin.isEmpty())) {
                finalPinToSave = com.mindmap.util.SecurityHelper.hashPin(pin);
            } else if (mode == NoteEditorMode.EDIT && note != null) {
                finalPinToSave = note.getPin(); // Keep old hash
            }
        }"""
        
content = content.replace(old_save, new_save)

content = content.replace('newNote.setPin(pin);', 'newNote.setPin(finalPinToSave);')
content = content.replace('currentNote.setPin(pin);', 'currentNote.setPin(finalPinToSave);')

with open('src/main/java/com/mindmap/controller/NoteEditorController.java', 'w') as f:
    f.write(content)
