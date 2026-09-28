import sqlite3

conn = sqlite3.connect('mindmap.sqlite')
c = conn.cursor()

c.execute("DELETE FROM revisions WHERE note_id IN (SELECT id FROM notes WHERE title = 'ReviewFlow Test Note')")
c.execute("DELETE FROM notes WHERE title = 'ReviewFlow Test Note'")
conn.commit()

c.execute("SELECT id, title FROM notes")
notes = c.fetchall()
print("Total notes in DB:", len(notes))

conn.close()
