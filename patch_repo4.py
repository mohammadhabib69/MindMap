with open('src/main/java/com/mindmap/repository/NoteRepository.java', 'r') as f:
    content = f.read()

bad_bindings = """            stmt.setInt(7, note.isFavorite() ? 1 : 0);
            stmt.setInt(8, note.isPrivate() ? 1 : 0);
            stmt.setString(9, note.getPin());
            stmt.setString(8, note.getLastViewedAt() != null ? DateUtil.formatDateTime(note.getLastViewedAt()) : null);"""

good_bindings = """            stmt.setInt(7, note.isFavorite() ? 1 : 0);
            stmt.setString(8, note.getLastViewedAt() != null ? DateUtil.formatDateTime(note.getLastViewedAt()) : null);
            stmt.setInt(9, note.isPrivate() ? 1 : 0);
            stmt.setString(10, note.getPin());"""

content = content.replace(bad_bindings, good_bindings)

with open('src/main/java/com/mindmap/repository/NoteRepository.java', 'w') as f:
    f.write(content)
