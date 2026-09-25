import re

with open('src/main/java/com/mindmap/database/DatabaseInitializer.java', 'r') as f:
    content = f.read()

# Add is_private and pin to the create table statement
create_table_regex = r'(updated_at TEXT NOT NULL)'
content = re.sub(create_table_regex, r'\1,\n    is_private INTEGER DEFAULT 0,\n    pin TEXT', content)

# Check if migrations are applied, but DatabaseInitializer usually handles simple table creation.
# Let's see if it has an alter table block.

with open('src/main/java/com/mindmap/database/DatabaseInitializer.java', 'w') as f:
    f.write(content)
