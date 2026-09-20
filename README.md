# MindMap
Personal Knowledge Base & Study Organizer

## Project Overview

MindMap is a comprehensive Java-based desktop application designed to serve as a personal knowledge base and study organizer. In a world of fragmented information, MindMap solves the problem of disconnected notes by allowing users to organically connect ideas into a network of knowledge, rather than relying on rigid hierarchical folders. 

By linking related notes and visualizing them through 2D and 3D graphs, users can intuitively understand the relationships between different concepts. The integration of an automated Spaced Repetition engine ensures that learned material is efficiently retained in long-term memory.

## Key Features

- **Notes CRUD:** Create, read, update, and delete rich-text notes with difficulty ratings and subjects.
- **Tags:** Assign multiple tags to notes for flexible categorization.
- **Search:** Full-text search with multi-criteria filtering (tags, subjects, dates, connection status).
- **Connections:** Form directional relationships between notes to build a web of knowledge.
- **2D Knowledge Graph:** Interactive visual network representing notes as nodes and connections as edges.
- **3D Knowledge Space:** An immersive, rotatable 3D visualization of the knowledge graph using a Fibonacci sphere layout.
- **Spaced Repetition:** Automated study scheduling based on user feedback (Again, Hard, Good, Easy).
- **Learning Timeline:** A chronological audit trail of note creations, updates, and review milestones.
- **Dashboard:** A real-time statistical overview of learning progress and database metrics.
- **Wikipedia Research:** Built-in tool to query Wikipedia, view summaries, and automatically generate linked notes from research.
- **JSON Import/Export:** Securely backup and restore the entire knowledge base (including tags, connections, and review history).
- **PDF Export:** Export individual notes or the entire database to a professionally formatted PDF, with full Unicode and Bengali font support.
- **Background Processing:** A centralized TaskExecutor manages heavy database and network operations asynchronously to keep the UI smooth.
- **Validation and Error Handling:** Comprehensive form validation and centralized dialog alerts prevent silent failures.

## Technology Stack

- **Java 17+** (Core programming language)
- **JavaFX** (Desktop GUI framework, FXML, CSS)
- **Maven** (Dependency management and build automation)
- **SQLite** (Embedded relational database)
- **Jackson** (JSON serialization/deserialization)
- **Java 11+ HTTP Client** (REST API requests for Wikipedia integration)
- **OpenPDF** (PDF generation with custom TrueType font embedding)
- **JUnit 5** (Automated unit and integration testing)

## Architecture

MindMap implements a strict layered architecture separating concerns:

```
JavaFX UI
   ↓
Controllers (com.mindmap.controller)
   ↓
Services (com.mindmap.service)
   ↓
Repositories (com.mindmap.repository)
   ↓
SQLite Database (com.mindmap.database)
```

- **Controllers:** Bind FXML views to the underlying logic, handle user events, and manage UI state on the JavaFX Application Thread.
- **Services:** Contain the core business logic, validation, spaced repetition algorithms, and coordinate data between the controllers and repositories.
- **Repositories:** Isolate direct database access, executing raw SQL queries and mapping `ResultSet` rows to Java Model objects.
- **Database:** SQLite manages persistent storage with enforced foreign key constraints to guarantee referential integrity.

## Project Structure

```
src/main/java/com/mindmap/
├── concurrency/     # Background task executor and threading models
├── controller/      # JavaFX UI controllers for all screens
├── database/        # SQLite connection manager and schema initializer
├── export/          # PDF and JSON export/import logic and DTOs
├── external/        # Wikipedia API integration and models
├── model/           # Domain entities (Note, Tag, Connection, ScheduledReview)
├── repository/      # Data access objects executing SQL queries
├── service/         # Business logic and cross-repository coordination
├── util/            # Utilities for dates, UI dialogs, and view management
├── visualization/   # 3D Knowledge Space sub-scene and geometry
└── Main.java        # Application entry point
```

## Installation

1. **Clone the repository:**
   ```bash
   git clone <repository-url>
   cd MindMap
   ```
2. **Ensure required Java version:**
   Verify that JDK 17 or higher is installed.
3. **Run Maven build and tests:**
   ```bash
   ./mvnw clean test
   ```
4. **Launch the application:**
   ```bash
   ./mvnw javafx:run
   ```

## Database

MindMap uses an embedded **SQLite** database.
- **Database File:** `mindmap.sqlite` (generated in the project root directory)
- **Database URL:** `jdbc:sqlite:mindmap.sqlite`
- **Foreign Keys:** Enforced via `PRAGMA foreign_keys = ON;`.
- **Cascade Behavior:** `ON DELETE CASCADE` is strictly implemented for relationships (e.g., deleting a note automatically cleans up its tags, connections, and revision history).

**IMPORTANT:** Do not modify the database URL or manually alter the schema, as the application relies on strict foreign key integrity to function.

## How To Use

- **Dashboard:** View high-level statistics and upcoming reviews.
- **Notes & Tags:** Navigate to the "Notes" tab to create a new note. Add a title, content, difficulty, and tags.
- **Connecting Notes:** In the Notes list, select a note, click "Connections", and link it to another related note.
- **Knowledge Graphs:** Open the "Mind Map" tab to interact with the 2D network. Click the "3D Space" button to view the network positioned in a 3D Fibonacci sphere.
- **Spaced Repetition:** Go to "Revision" to study due notes. Read the title, mentally recall the content, click "Reveal", and grade your memory (Again, Hard, Good, Easy) to schedule the next review.
- **Research:** Use the "Research" tab to query Wikipedia. Select an article, review the summary, and click "Create Note" to instantly save the research into your database.
- **Export/Import:** Go to "Settings" to export your entire database as a JSON backup, or export a formatted PDF of your notes for printing.

## JSON Import/Export

- **Export Format:** Structured JSON encompassing `notes`, `tags`, `connections`, `revisions`, and `learningEvents`.
- **Metadata:** Includes `formatVersion` and `exportedAt` timestamps.
- **Import Behavior:** Operates within a strict SQL transaction. It gracefully merges data by attempting to match existing notes by `title`. 
- **Duplicate Handling:** If a note with the exact title already exists, it is safely updated and merged rather than duplicated. Tags and connections are deduplicated dynamically.

## PDF Export

- **Functionality:** Users can export a single note or generate a master document containing all notes.
- **Font Support:** Bundled with the GNU FreeSerif TrueType font (`FreeSerif.ttf`), ensuring full Unicode and **Bengali** character support without visual corruption.
- **Execution:** Runs on background worker threads to prevent the UI from freezing during heavy I/O file operations.

## External API

- **Integration:** Utilizes the Wikipedia REST API (`https://en.wikipedia.org/api/rest_v1/page/summary/`).
- **Workflow:** Queries live data asynchronously. Successful responses are parsed into summaries, capturing the source URL. Users can automatically bootstrap a new Note from the summary.
- **Resilience:** Implements defensive error handling for timeouts and missing networks, safely restoring the UI to an interactive state.

## Multithreading

- **TaskExecutor:** A custom concurrency utility utilizing a fixed daemon thread pool (3 workers).
- **UI Thread Safety:** All heavy operations (database queries, network requests, file I/O) are submitted to the `TaskExecutor`. Upon completion, callbacks are dispatched back to the JavaFX UI via `Platform.runLater()`.
- **Shutdown:** Because the thread pool uses Daemon threads, the application shuts down cleanly immediately upon window closure.

## Testing

The project possesses a comprehensive automated test suite validating the database, services, and concurrency behavior.
- **Final Test Count:** 156 tests
- **Result:** 156 Passed, 0 Failures, 0 Errors, 0 Skipped.

## Known Limitations

- **Deduplication strategy:** JSON Import merges incoming notes with existing notes based on an exact `title` match. Notes with intentionally identical titles may be accidentally merged during an import.

## Future Improvements

*(These are proposed ideas for future development, not currently implemented)*
- **Advanced Graph Algorithms:** Implementing shortest-path or clustering algorithms to suggest undiscovered connections between notes.
- **Cloud Synchronization:** Replacing the local SQLite database with a cloud-hosted PostgreSQL instance for cross-device syncing.
- **Richer Media:** Allowing image attachments and rich markdown rendering inside the Note editor.
