import re

with open('src/main/java/com/mindmap/controller/NoteViewController.java', 'r') as f:
    text = f.read()

text = text.replace('editHandler.accept(note);', 'javafx.application.Platform.runLater(() -> editHandler.accept(note));')

with open('src/main/java/com/mindmap/controller/NoteViewController.java', 'w') as f:
    f.write(text)
