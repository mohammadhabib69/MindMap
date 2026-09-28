import sqlite3

conn = sqlite3.connect('mindmap.sqlite')
c = conn.cursor()

c.execute("INSERT INTO notes (title, content, subject, difficulty, created_at, updated_at) VALUES ('Fake Note', 'x', 'x', 'EASY', '2026-01-01', '2026-01-01')")
note_id = c.lastrowid

c.execute("INSERT INTO revisions (note_id, review_date, status, interval_days, created_at) VALUES (?, '2026-01-01', 'PENDING', 1, '2026-01-01')", (note_id,))
rev_id = c.lastrowid

print("True Note ID:", note_id)
print("True Rev ID:", rev_id)

c.execute("""
    SELECT r.id, r.note_id, r.review_date, r.status, r.interval_days, r.created_at,
           n.id AS n_id, n.title, n.content, n.subject, n.difficulty,
           n.created_at AS n_created_at, n.updated_at AS n_updated_at
    FROM revisions r
    INNER JOIN notes n ON r.note_id = n.id
    WHERE r.id = ?
""", (rev_id,))

cols = [desc[0] for desc in c.description]
print("Columns:", cols)
row = c.fetchone()
print("Row:", row)

conn.close()
