import sqlite3
import datetime

conn = sqlite3.connect('test_review_flow.sqlite')
c = conn.cursor()

c.execute("INSERT INTO notes (title, content, subject, difficulty, created_at, updated_at) VALUES ('Test', 'content', 'test', 'EASY', '2026-09-28T00:00:00', '2026-09-28T00:00:00')")
note_id = c.lastrowid

c.execute("INSERT INTO revisions (note_id, review_date, status, interval_days, created_at) VALUES (?, '2026-09-28', 'PENDING', 1, '2026-09-28T00:00:00')", (note_id,))
rev_id = c.lastrowid

print("Created note_id=", note_id, "rev_id=", rev_id)

c.execute("UPDATE revisions SET review_date = '2026-10-01', interval_days = 3 WHERE id = ?", (rev_id,))
conn.commit()

c.execute("SELECT id, review_date, interval_days FROM revisions WHERE note_id = ?", (note_id,))
print("After update:", c.fetchall())

