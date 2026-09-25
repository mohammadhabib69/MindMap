with open('src/main/java/com/mindmap/repository/NoteRepository.java', 'r') as f:
    text = f.read()

text = text.replace('WHERE (n.title LIKE ? OR n.subject LIKE ? OR (n.is_private = 0 AND n.content LIKE ?))', 'WHERE (n.title LIKE ? OR n.content LIKE ? OR n.subject LIKE ?)')

with open('src/main/java/com/mindmap/repository/NoteRepository.java', 'w') as f:
    f.write(text)
