import sqlite3
import datetime

conn = sqlite3.connect('mindmap.sqlite')
c = conn.cursor()

c.execute("INSERT INTO notes (title, content, subject, difficulty) VALUES ('Test Note', 'content', 'test', 'EASY')")
note_id = c.lastrowid

today_str = datetime.date.today().isoformat()
now_str = datetime.datetime.now().isoformat()
c.execute("INSERT INTO revisions (note_id, review_date, status, interval_days, created_at) VALUES (?, ?, 'PENDING', 1, ?)", (note_id, today_str, now_str))
conn.commit()

c.execute("SELECT id, review_date, status, interval_days FROM revisions WHERE note_id = ?", (note_id,))
print("Before review:", c.fetchall())
conn.close()
