import re

with open('src/main/java/com/mindmap/controller/NoteEditorController.java', 'r') as f:
    text = f.read()

# Remove @FXML fields
text = re.sub(r'    @FXML\s*private javafx\.scene\.control\.CheckBox chkPrivate;\s*', '', text)
text = re.sub(r'    @FXML\s*private javafx\.scene\.control\.PasswordField txtPin;\s*', '', text)
text = re.sub(r'    @FXML\s*private javafx\.scene\.control\.PasswordField txtConfirmPin;\s*', '', text)
text = re.sub(r'    @FXML\s*private javafx\.scene\.layout\.VBox boxPinFields;\s*', '', text)

# Remove listener in initialize
init_listener = r'        if \(chkPrivate != null && boxPinFields != null\) \{\s*chkPrivate\.selectedProperty\(\)\.addListener\(\(obs, oldVal, newVal\) -> \{\s*boxPinFields\.setVisible\(newVal\);\s*boxPinFields\.setManaged\(newVal\);\s*if \(!newVal\) \{\s*if \(txtPin != null\) txtPin\.clear\(\);\s*if \(txtConfirmPin != null\) txtConfirmPin\.clear\(\);\s*\}\s*\}\);\s*// trigger once manually to setup initial state\s*boxPinFields\.setVisible\(chkPrivate\.isSelected\(\)\);\s*boxPinFields\.setManaged\(chkPrivate\.isSelected\(\)\);\s*\}\s*'
text = re.sub(init_listener, '', text)

# Remove populate logic in loadNoteData
pop_logic = r'            if \(chkPrivate != null\) \{\s*chkPrivate\.setSelected\(note\.isPrivate\(\)\);\s*if \(txtPin != null && note\.isPrivate\(\)\) \{\s*txtPin\.setPromptText\("Leave blank to keep existing PIN"\);\s*\}\s*\}\s*'
text = re.sub(pop_logic, '', text)

# Remove save validation logic entirely
save_validation = r'\s*final boolean isPrivate = \(chkPrivate != null\) && chkPrivate\.isSelected\(\);\s*String pin = \(txtPin != null\) \? txtPin\.getText\(\)\.trim\(\) : "";\s*String confirmPin = \(txtConfirmPin != null\) \? txtConfirmPin\.getText\(\)\.trim\(\) : "";\s*if \(isPrivate\) \{\s*if \(mode == NoteEditorMode\.CREATE \|\| \(mode == NoteEditorMode\.EDIT && !pin\.isEmpty\(\)\)\) \{\s*if \(pin\.isEmpty\(\)\) \{\s*showError\("Please enter a PIN\."\);\s*if \(txtPin != null\) txtPin\.requestFocus\(\);\s*if \(btnSave != null\) \{ btnSave\.setDisable\(false\); btnSave\.setText\(mode == NoteEditorMode\.CREATE \? "Create Note" : "Save"\); \}\s*return CompletableFuture\.completedFuture\(null\);\s*\}\s*if \(confirmPin\.isEmpty\(\)\) \{\s*showError\("Please confirm your PIN\."\);\s*if \(txtConfirmPin != null\) txtConfirmPin\.requestFocus\(\);\s*if \(btnSave != null\) \{ btnSave\.setDisable\(false\); btnSave\.setText\(mode == NoteEditorMode\.CREATE \? "Create Note" : "Save"\); \}\s*return CompletableFuture\.completedFuture\(null\);\s*\}\s*if \(!pin\.equals\(confirmPin\)\) \{\s*showError\("PINs do not match\."\);\s*if \(txtConfirmPin != null\) txtConfirmPin\.requestFocus\(\);\s*if \(btnSave != null\) \{ btnSave\.setDisable\(false\); btnSave\.setText\(mode == NoteEditorMode\.CREATE \? "Create Note" : "Save"\); \}\s*return CompletableFuture\.completedFuture\(null\);\s*\}\s*\}\s*\}\s*String tempPinToSave = null;\s*if \(isPrivate\) \{\s*if \(mode == NoteEditorMode\.CREATE \|\| \(mode == NoteEditorMode\.EDIT && !pin\.isEmpty\(\)\)\) \{\s*tempPinToSave = com\.mindmap\.util\.SecurityHelper\.hashPin\(pin\);\s*\} else if \(mode == NoteEditorMode\.EDIT && note != null\) \{\s*tempPinToSave = note\.getPin\(\); // Keep old hash\s*\}\s*\}\s*final String finalPinToSave = tempPinToSave;\s*'

text = re.sub(save_validation, '', text)

# Also remove newNote.setPrivate(...) and setPin(...)
text = re.sub(r'            newNote\.setPrivate\(isPrivate\);\n', '', text)
text = re.sub(r'            newNote\.setPin\(finalPinToSave\);\n', '', text)


with open('src/main/java/com/mindmap/controller/NoteEditorController.java', 'w') as f:
    f.write(text)
