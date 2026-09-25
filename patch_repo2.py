with open('src/main/java/com/mindmap/repository/NoteRepository.java', 'r') as f:
    content = f.read()

old_insert = """                INSERT INTO notes (title, content, subject, difficulty, created_at, updated_at, is_favorite, last_viewed_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?);"""
new_insert = """                INSERT INTO notes (title, content, subject, difficulty, created_at, updated_at, is_favorite, last_viewed_at, is_private, pin)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"""

content = content.replace(old_insert, new_insert)

content = content.replace('stmt.setString(8, note.getLastViewedAt() != null ? note.getLastViewedAt().format(formatter) : null);',
                          'stmt.setString(8, note.getLastViewedAt() != null ? note.getLastViewedAt().format(formatter) : null);\n                    stmt.setInt(9, note.isPrivate() ? 1 : 0);\n                    stmt.setString(10, note.getPin());')

old_update = """                UPDATE notes SET title = ?, content = ?, subject = ?, difficulty = ?, updated_at = ?, is_favorite = ?, last_viewed_at = ?
                WHERE id = ?;"""
new_update = """                UPDATE notes SET title = ?, content = ?, subject = ?, difficulty = ?, updated_at = ?, is_favorite = ?, last_viewed_at = ?, is_private = ?, pin = ?
                WHERE id = ?;"""
                
content = content.replace(old_update, new_update)

content = content.replace('stmt.setInt(8, note.getId());',
                          'stmt.setInt(8, note.isPrivate() ? 1 : 0);\n                    stmt.setString(9, note.getPin());\n                    stmt.setInt(10, note.getId());')


with open('src/main/java/com/mindmap/repository/NoteRepository.java', 'w') as f:
    f.write(content)
