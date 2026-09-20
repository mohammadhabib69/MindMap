# Database Documentation

MindMap utilizes an embedded **SQLite** database (`mindmap.sqlite`) to ensure portability and zero-configuration setup for end users. The schema is highly normalized and relies heavily on foreign key constraints.

## Schema Overview

### 1. `notes`
The core entity representing a user's knowledge entry.
- **`id`** `INTEGER PRIMARY KEY AUTOINCREMENT`
- **`title`** `TEXT NOT NULL`
- **`content`** `TEXT`
- **`subject`** `TEXT` (Optional categorization)
- **`difficulty`** `TEXT` (Used for spaced repetition baselines)
- **`created_at`** `TEXT NOT NULL`
- **`updated_at`** `TEXT NOT NULL`

### 2. `tags`
Stores unique string tags for categorizing notes.
- **`id`** `INTEGER PRIMARY KEY AUTOINCREMENT`
- **`name`** `TEXT NOT NULL UNIQUE COLLATE NOCASE` (Enforces case-insensitive uniqueness at the database level)

### 3. `note_tags`
A junction table mapping the Many-to-Many relationship between `notes` and `tags`.
- **`note_id`** `INTEGER NOT NULL`
- **`tag_id`** `INTEGER NOT NULL`
- **`PRIMARY KEY`** `(note_id, tag_id)`
- **`FOREIGN KEY (note_id)`** `REFERENCES notes(id) ON DELETE CASCADE`
- **`FOREIGN KEY (tag_id)`** `REFERENCES tags(id) ON DELETE CASCADE`
- *Note:* If a note is deleted, its tag associations are automatically cleaned up.

### 4. `connections`
Defines directional relationships mapping the knowledge graph.
- **`id`** `INTEGER PRIMARY KEY AUTOINCREMENT`
- **`from_note_id`** `INTEGER NOT NULL`
- **`to_note_id`** `INTEGER NOT NULL`
- **`relation`** `TEXT` (Optional descriptive label for the connection)
- **`FOREIGN KEY (from_note_id)`** `REFERENCES notes(id) ON DELETE CASCADE`
- **`FOREIGN KEY (to_note_id)`** `REFERENCES notes(id) ON DELETE CASCADE`
- **`UNIQUE(from_note_id, to_note_id)`** (Prevents duplicate exact connections)

### 5. `revisions`
Stores the active Spaced Repetition scheduling state for a Note.
- **`note_id`** `INTEGER NOT NULL` (Acts as both a Foreign Key and implicitly unique identifier, as each Note has only one active schedule)
- **`review_date`** `TEXT NOT NULL` (The exact timestamp when the note is due for review)
- **`status`** `TEXT`
- **`interval_days`** `INTEGER` (The current spacing interval)
- **`created_at`** `TEXT NOT NULL`
- **`FOREIGN KEY (note_id)`** `REFERENCES notes(id) ON DELETE CASCADE`

### 6. `learning_events`
An append-only audit trail logging learning milestones for the Timeline view.
- **`id`** `INTEGER PRIMARY KEY AUTOINCREMENT`
- **`note_id`** `INTEGER`
- **`event_type`** `TEXT` (e.g., "NOTE_CREATED", "NOTE_REVIEWED")
- **`event_date`** `TEXT NOT NULL`
- **`description`** `TEXT`
- **`FOREIGN KEY (note_id)`** `REFERENCES notes(id) ON DELETE SET NULL`
- *Note:* `ON DELETE SET NULL` is used here instead of `CASCADE` so that historical timeline data is preserved even if the original note is deleted.
