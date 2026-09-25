import re

with open('src/main/java/com/mindmap/repository/NoteRepository.java', 'r') as f:
    content = f.read()

old_where = '            sql.append("WHERE (n.title LIKE ? OR n.content LIKE ? OR n.subject LIKE ?) ");'
new_where = '            sql.append("WHERE (n.title LIKE ? OR n.subject LIKE ? OR (n.is_private = 0 AND n.content LIKE ?)) ");'
content = content.replace(old_where, new_where)

with open('src/main/java/com/mindmap/repository/NoteRepository.java', 'w') as f:
    f.write(content)
