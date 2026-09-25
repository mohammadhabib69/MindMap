import re

with open('src/main/java/com/mindmap/service/ImportExportService.java', 'r') as f:
    content = f.read()

old_json = """            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT * FROM notes")) {
                while (rs.next()) {"""
                
new_json = """            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT * FROM notes")) {
                while (rs.next()) {
                    try {
                        if (rs.getInt("is_private") == 1) {
                            continue; // Skip private notes to prevent data leaks
                        }
                    } catch (Exception ignored) {}"""

content = content.replace(old_json, new_json)

with open('src/main/java/com/mindmap/service/ImportExportService.java', 'w') as f:
    f.write(content)
