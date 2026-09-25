import re

with open('src/main/java/com/mindmap/repository/NoteRepository.java', 'r') as f:
    content = f.read()

# Replace the SQL
old_sql_pattern = r'UPDATE notes\s*SET title = \?, content = \?, subject = \?, difficulty = \?, updated_at = \?, is_favorite = \?, last_viewed_at = \?\s*WHERE id = \?;'
new_sql = 'UPDATE notes\n                SET title = ?, content = ?, subject = ?, difficulty = ?, updated_at = ?, is_favorite = ?, last_viewed_at = ?, is_private = ?, pin = ?\n                WHERE id = ?;'

content = re.sub(old_sql_pattern, new_sql, content)

# Check update bindings. They should be:
# stmt.setString(7, note.getLastViewedAt() != null ? DateUtil.formatDateTime(note.getLastViewedAt()) : null);
# stmt.setInt(8, note.isPrivate() ? 1 : 0);
# stmt.setString(9, note.getPin());
# stmt.setInt(10, note.getId());

with open('src/main/java/com/mindmap/repository/NoteRepository.java', 'w') as f:
    f.write(content)
