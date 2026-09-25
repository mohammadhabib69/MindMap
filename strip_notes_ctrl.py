import re

with open('src/main/java/com/mindmap/controller/NotesController.java', 'r') as f:
    text = f.read()

# Replace combobox items
text = text.replace('"All Notes", "Favorites", "Private Notes", "Recently Viewed"', '"All Notes", "Favorites", "Recently Viewed"')

# Replace search logic
text = re.sub(r' \} else if \("Private Notes"\.equals\(viewMode\)\) \{\s*filtered = filtered\.stream\(\)\.filter\(Note::isPrivate\)\.toList\(\);', '', text)

# Remove the lock icon logic
lock_icon_logic = r'\s*colTitle\.setCellFactory\(tc -> new javafx\.scene\.control\.TableCell<Note, String>\(\) \{\s*@Override\s*protected void updateItem\(String item, boolean empty\) \{\s*super\.updateItem\(item, empty\);\s*if \(empty \|\| item == null\) \{\s*setText\(null\);\s*setGraphic\(null\);\s*\} else \{\s*Note note = getTableView\(\)\.getItems\(\)\.get\(getIndex\(\)\);\s*if \(note\.isPrivate\(\)\) \{\s*setText\("🔒 " \+ item\);\s*\} else \{\s*setText\(item\);\s*\}\s*\}\s*\}\s*\}\);\s*'
text = re.sub(lock_icon_logic, '', text)

with open('src/main/java/com/mindmap/controller/NotesController.java', 'w') as f:
    f.write(text)
