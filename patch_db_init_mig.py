with open('src/main/java/com/mindmap/database/DatabaseInitializer.java', 'r') as f:
    content = f.read()

mig_code = """            // Safe Migrations for Phase 18
            try {
                stmt.execute("ALTER TABLE notes ADD COLUMN is_favorite INTEGER DEFAULT 0;");
                LOGGER.info("Added is_favorite column to notes table.");
            } catch (SQLException ignored) {}
            try {
                stmt.execute("ALTER TABLE notes ADD COLUMN last_viewed_at TEXT;");
                LOGGER.info("Added last_viewed_at column to notes table.");
            } catch (SQLException ignored) {}
            try {
                stmt.execute("ALTER TABLE notes ADD COLUMN is_private INTEGER DEFAULT 0;");
            } catch (SQLException ignored) {}
            try {
                stmt.execute("ALTER TABLE notes ADD COLUMN pin TEXT;");
            } catch (SQLException ignored) {}"""

content = content.replace('            // Safe Migrations for Phase 18\n            try {\n                stmt.execute("ALTER TABLE notes ADD COLUMN is_favorite INTEGER DEFAULT 0;");\n                LOGGER.info("Added is_favorite column to notes table.");\n            } catch (SQLException ignored) {}\n            try {\n                stmt.execute("ALTER TABLE notes ADD COLUMN last_viewed_at TEXT;");\n                LOGGER.info("Added last_viewed_at column to notes table.");\n            } catch (SQLException ignored) {}', mig_code)

with open('src/main/java/com/mindmap/database/DatabaseInitializer.java', 'w') as f:
    f.write(content)
