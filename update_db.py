with open('src/main/java/com/mindmap/database/DatabaseInitializer.java', 'r') as f:
    text = f.read()

quiz_tables = """            CREATE TABLE IF NOT EXISTS quiz_sessions (
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
            \",
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
"""

import re
text = re.sub(r'(\s*)};\s*for\s*\(String\s+sql\s*:\s*tableSql\)', r',\n' + quiz_tables + r'\1};\n\1for (String sql : tableSql)', text)

with open('src/main/java/com/mindmap/database/DatabaseInitializer.java', 'w') as f:
    f.write(text)
