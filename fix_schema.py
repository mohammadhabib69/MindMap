with open('src/main/java/com/mindmap/database/DatabaseInitializer.java', 'r') as f:
    text = f.read()

import re

# Remove the previously added tables
# We added them just before public static void initialize()
# Let's completely rewrite the SCHEMA_STATEMENTS to include the new tables properly.

target = "private static final String[] SCHEMA_STATEMENTS = {"
end_target = "};\n\n    public static void initialize()"

start = text.find(target)
end = text.find(end_target)
if start != -1 and end != -1:
    schema = """private static final String[] SCHEMA_STATEMENTS = {
            \"\"\"
            CREATE TABLE IF NOT EXISTS notes (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                title TEXT NOT NULL,
                content TEXT,
                subject TEXT,
                difficulty TEXT,
                created_at TEXT NOT NULL,
                updated_at TEXT NOT NULL,
                is_private INTEGER DEFAULT 0,
                pin TEXT
            );
            \"\"\",
            \"\"\"
            CREATE TABLE IF NOT EXISTS tags (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL UNIQUE
            );
            \"\"\",
            \"\"\"
            CREATE TABLE IF NOT EXISTS note_tags (
                note_id INTEGER NOT NULL,
                tag_id INTEGER NOT NULL,
                PRIMARY KEY (note_id, tag_id),
                FOREIGN KEY (note_id) REFERENCES notes(id) ON DELETE CASCADE,
                FOREIGN KEY (tag_id) REFERENCES tags(id) ON DELETE CASCADE
            );
            \"\"\",
            \"\"\"
            CREATE TABLE IF NOT EXISTS connections (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                source_note_id INTEGER NOT NULL,
                target_note_id INTEGER NOT NULL,
                connection_type TEXT,
                FOREIGN KEY (source_note_id) REFERENCES notes(id) ON DELETE CASCADE,
                FOREIGN KEY (target_note_id) REFERENCES notes(id) ON DELETE CASCADE
            );
            \"\"\",
            \"\"\"
            CREATE TABLE IF NOT EXISTS revisions (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                note_id INTEGER NOT NULL UNIQUE,
                next_review_date TEXT NOT NULL,
                interval_days INTEGER DEFAULT 0,
                ease_factor REAL DEFAULT 2.5,
                review_count INTEGER DEFAULT 0,
                FOREIGN KEY (note_id) REFERENCES notes(id) ON DELETE CASCADE
            );
            \"\"\",
            \"\"\"
            CREATE TABLE IF NOT EXISTS learning_events (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                note_id INTEGER NOT NULL,
                event_date TEXT NOT NULL,
                outcome TEXT NOT NULL,
                FOREIGN KEY (note_id) REFERENCES notes(id) ON DELETE CASCADE
            );
            \"\"\",
            \"\"\"
            CREATE TABLE IF NOT EXISTS question_bank (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                source_note_id INTEGER NOT NULL,
                question_text TEXT UNIQUE,
                question_type TEXT,
                options_json TEXT,
                correct_answer_json TEXT,
                explanation TEXT,
                subject TEXT,
                topic TEXT,
                difficulty TEXT,
                created_at TEXT NOT NULL,
                times_studied INTEGER DEFAULT 0,
                times_asked INTEGER DEFAULT 0,
                FOREIGN KEY (source_note_id) REFERENCES notes(id) ON DELETE CASCADE
            );
            \"\"\",
            \"\"\"
            CREATE TABLE IF NOT EXISTS quiz_attempts (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                mode TEXT,
                question_count INTEGER,
                time_limit_seconds INTEGER,
                started_at TEXT,
                completed_at TEXT,
                score INTEGER,
                correct_count INTEGER,
                incorrect_count INTEGER,
                unanswered_count INTEGER
            );
            \"\"\",
            \"\"\"
            CREATE TABLE IF NOT EXISTS quiz_attempt_questions (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                attempt_id INTEGER NOT NULL,
                question_id INTEGER NOT NULL,
                question_order INTEGER,
                user_answer_json TEXT,
                is_correct INTEGER DEFAULT 0,
                FOREIGN KEY (attempt_id) REFERENCES quiz_attempts(id) ON DELETE CASCADE,
                FOREIGN KEY (question_id) REFERENCES question_bank(id) ON DELETE CASCADE
            );
            \"\"\"
    """
    new_text = text[:start] + schema + text[end:]
    with open('src/main/java/com/mindmap/database/DatabaseInitializer.java', 'w') as f:
        f.write(new_text)
    print("Schema updated.")
else:
    print("Could not find start or end target.")

