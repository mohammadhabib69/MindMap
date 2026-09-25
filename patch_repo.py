import re

with open('src/main/java/com/mindmap/repository/NoteRepository.java', 'r') as f:
    content = f.read()

# Update createNote
content = content.replace('INSERT INTO notes (title, content, subject, difficulty, created_at, updated_at, is_favorite) VALUES (?, ?, ?, ?, ?, ?, ?)',
                          'INSERT INTO notes (title, content, subject, difficulty, created_at, updated_at, is_favorite, is_private, pin) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)')

content = content.replace('stmt.setInt(7, note.isFavorite() ? 1 : 0);',
                          'stmt.setInt(7, note.isFavorite() ? 1 : 0);\n            stmt.setInt(8, note.isPrivate() ? 1 : 0);\n            stmt.setString(9, note.getPin());')

# Update updateNote
content = content.replace('UPDATE notes SET title = ?, content = ?, subject = ?, difficulty = ?, updated_at = ?, is_favorite = ? WHERE id = ?',
                          'UPDATE notes SET title = ?, content = ?, subject = ?, difficulty = ?, updated_at = ?, is_favorite = ?, is_private = ?, pin = ? WHERE id = ?')

content = content.replace('stmt.setInt(7, note.getId());',
                          'stmt.setInt(7, note.isPrivate() ? 1 : 0);\n            stmt.setString(8, note.getPin());\n            stmt.setInt(9, note.getId());')

# Read queries
content = content.replace('is_favorite, last_viewed_at FROM notes', 'is_favorite, last_viewed_at, is_private, pin FROM notes')
content = content.replace('n.is_favorite, n.last_viewed_at,', 'n.is_favorite, n.last_viewed_at, n.is_private, n.pin,')

# Map result set
map_code = """        note.setFavorite(rs.getInt("is_favorite") == 1);
        String lastViewed = rs.getString("last_viewed_at");
        if (lastViewed != null && !lastViewed.isEmpty()) {
            note.setLastViewedAt(LocalDateTime.parse(lastViewed, formatter));
        }
        note.setPrivate(rs.getInt("is_private") == 1);
        note.setPin(rs.getString("pin"));"""

content = content.replace('        note.setFavorite(rs.getInt("is_favorite") == 1);\n        String lastViewed = rs.getString("last_viewed_at");\n        if (lastViewed != null && !lastViewed.isEmpty()) {\n            note.setLastViewedAt(LocalDateTime.parse(lastViewed, formatter));\n        }', map_code)

with open('src/main/java/com/mindmap/repository/NoteRepository.java', 'w') as f:
    f.write(content)
