with open('src/main/java/com/mindmap/database/DatabaseInitializer.java', 'r') as f:
    text = f.read()

target = "    private static final String[] SCHEMA_STATEMENTS = {"
end_target = "    };"

# Find the end of SCHEMA_STATEMENTS array
start_idx = text.find(target)
if start_idx != -1:
    end_idx = text.find(end_target, start_idx)
    if end_idx != -1:
        # insert before end_target
        insert_text = """,
            \"\"\"
            CREATE TABLE IF NOT EXISTS quiz_sessions (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                title TEXT,
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
            CREATE TABLE IF NOT EXISTS quiz_questions (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                quiz_session_id INTEGER NOT NULL,
                source_note_id INTEGER NOT NULL,
                question_text TEXT,
                question_type TEXT,
                options_json TEXT,
                correct_answer_json TEXT,
                user_answer_json TEXT,
                question_order INTEGER,
                is_correct INTEGER DEFAULT 0,
                FOREIGN KEY (quiz_session_id) REFERENCES quiz_sessions(id) ON DELETE CASCADE,
                FOREIGN KEY (source_note_id) REFERENCES notes(id) ON DELETE CASCADE
            );
            \"\"\"
"""
        new_text = text[:end_idx] + insert_text + text[end_idx:]
        with open('src/main/java/com/mindmap/database/DatabaseInitializer.java', 'w') as f:
            f.write(new_text)
        print("Success")
    else:
        print("End target not found")
else:
    print("Target not found")
