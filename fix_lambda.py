import re

with open('src/main/java/com/mindmap/controller/NoteEditorController.java', 'r') as f:
    content = f.read()

old_code = """        String finalPinToSave = null;
        if (isPrivate) {
            if (mode == NoteEditorMode.CREATE || (mode == NoteEditorMode.EDIT && !pin.isEmpty())) {
                finalPinToSave = com.mindmap.util.SecurityHelper.hashPin(pin);
            } else if (mode == NoteEditorMode.EDIT && note != null) {
                finalPinToSave = note.getPin(); // Keep old hash
            }
        }"""

new_code = """        String tempPinToSave = null;
        if (isPrivate) {
            if (mode == NoteEditorMode.CREATE || (mode == NoteEditorMode.EDIT && !pin.isEmpty())) {
                tempPinToSave = com.mindmap.util.SecurityHelper.hashPin(pin);
            } else if (mode == NoteEditorMode.EDIT && note != null) {
                tempPinToSave = note.getPin(); // Keep old hash
            }
        }
        final String finalPinToSave = tempPinToSave;"""

content = content.replace(old_code, new_code)

with open('src/main/java/com/mindmap/controller/NoteEditorController.java', 'w') as f:
    f.write(content)
