import re

with open('src/main/java/com/mindmap/controller/NoteEditorController.java', 'r') as f:
    text = f.read()

text = re.sub(r'                        currentNote\.setPrivate\(isPrivate\);\n', '', text)
text = re.sub(r'                        currentNote\.setPin\(finalPinToSave\);\n', '', text)

# Just in case for newNote
text = re.sub(r'                        newNote\.setPrivate\(isPrivate\);\n', '', text)
text = re.sub(r'                        newNote\.setPin\(finalPinToSave\);\n', '', text)

with open('src/main/java/com/mindmap/controller/NoteEditorController.java', 'w') as f:
    f.write(text)
