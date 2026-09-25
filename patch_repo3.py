with open('src/main/java/com/mindmap/repository/NoteRepository.java', 'r') as f:
    content = f.read()

# Fix the first update (around line 156)
old_sql_1 = """        String sql = \"\"\"
                UPDATE notes
                SET title = ?, content = ?, subject = ?, difficulty = ?, updated_at = ?, is_favorite = ?, last_viewed_at = ?
                WHERE id = ?;
                \"\"\";"""
new_sql_1 = """        String sql = \"\"\"
                UPDATE notes
                SET title = ?, content = ?, subject = ?, difficulty = ?, updated_at = ?, is_favorite = ?, last_viewed_at = ?, is_private = ?, pin = ?
                WHERE id = ?;
                \"\"\";"""
content = content.replace(old_sql_1, new_sql_1)

# Fix the bindings for the first update
old_bind_1 = """            stmt.setString(8, note.getLastViewedAt() != null ? note.getLastViewedAt().format(formatter) : null);
            stmt.setInt(9, note.getId());"""
new_bind_1 = """            stmt.setString(8, note.getLastViewedAt() != null ? note.getLastViewedAt().format(formatter) : null);
            stmt.setInt(9, note.isPrivate() ? 1 : 0);
            stmt.setString(10, note.getPin());
            stmt.setInt(11, note.getId());"""
content = content.replace(old_bind_1, new_bind_1)

# Fix the second update (around line 455)
old_sql_2 = """        String updateNoteSql = \"\"\"
                UPDATE notes
                SET title = ?, content = ?, subject = ?, difficulty = ?, updated_at = ?, is_favorite = ?, last_viewed_at = ?
                WHERE id = ?;
                \"\"\";"""
new_sql_2 = """        String updateNoteSql = \"\"\"
                UPDATE notes
                SET title = ?, content = ?, subject = ?, difficulty = ?, updated_at = ?, is_favorite = ?, last_viewed_at = ?, is_private = ?, pin = ?
                WHERE id = ?;
                \"\"\";"""
content = content.replace(old_sql_2, new_sql_2)

# Fix the bindings for the second update
old_bind_2 = """                    stmt.setString(8, note.getLastViewedAt() != null ? note.getLastViewedAt().format(formatter) : null);
                    stmt.setInt(9, note.isPrivate() ? 1 : 0);
                    stmt.setString(10, note.getPin());"""
# Wait, I already added `stmt.setInt(9, ...)` in patch_repo2! Let's check how it looks currently!
