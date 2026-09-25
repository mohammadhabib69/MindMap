import re
with open('src/main/java/com/mindmap/controller/MainController.java', 'r') as f:
    text = f.read()

# Remove the bad injection on NoteEditorController
text = re.sub(r'controller\.setNote\(null, com\.mindmap\.controller\.NoteEditorMode\.CREATE\);\s*controller\.setEditHandler\(this::handleEditNote\);', 
              'controller.setNote(null, com.mindmap.controller.NoteEditorMode.CREATE);', text)

# Add the correct injections
# It occurs after controller.setNote(selected);
text = re.sub(r'(controller\.setNote\(selected\);)(?!\s*controller\.setEditHandler)', r'\1\n                        controller.setEditHandler(this::handleEditNote);', text)

with open('src/main/java/com/mindmap/controller/MainController.java', 'w') as f:
    f.write(text)
