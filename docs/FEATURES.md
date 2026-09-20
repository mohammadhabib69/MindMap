# Feature Documentation

MindMap consists of several interconnected modules. This document outlines the purpose and implementation details of the major systems.

## 1. Notes
- **Purpose:** The fundamental unit of knowledge storage.
- **How it works:** Provides full CRUD (Create, Read, Update, Delete) capabilities. Notes support titles, subjects, difficulty ratings, and rich-text content.
- **Implementation Idea:** The UI automatically blocks invalid states (empty titles) using `UiUtils.showError()`. Unsaved changes are intercepted securely when attempting to close the editor.

## 2. Tags
- **Purpose:** Flexible categorization across subjects.
- **How it works:** Users enter comma-separated tags. 
- **Implementation Idea:** The system aggressively sanitizes input, collapsing case-insensitive duplicates. The `note_tags` table organically links them.

## 3. Search
- **Purpose:** Fast retrieval of specific notes from large databases.
- **How it works:** Users can search via text matching, or utilize dropdown filters for specific Tags, Subjects, Difficulties, Dates, or Connection statuses.
- **Implementation Idea:** The `SearchController` builds dynamic SQL queries via the `NoteRepository` to filter precisely on the database level, preventing memory bloat in Java.

## 4. Connections
- **Purpose:** Linking conceptual ideas together.
- **How it works:** A user selects a Note and assigns a directed link to another Note.
- **Implementation Idea:** The `ConnectionService` implements business rules strictly blocking self-connections (`noteA -> noteA`) and duplicate connections.

## 5. 2D Knowledge Graph
- **Purpose:** A visual network mapping the user's interconnected brain.
- **How it works:** Renders interactive `Circle` nodes and `Line` edges on a JavaFX `Pane`.
- **Implementation Idea:** Incorporates a recursive force-directed auto-layout algorithm to untangle overlapping nodes visually, offering panning and zooming via affine transformations.

## 6. 3D Knowledge Space
- **Purpose:** An immersive, scalable representation of the knowledge graph.
- **How it works:** Leverages JavaFX's `SubScene` and `PerspectiveCamera` to render spheres in a 3D environment.
- **Implementation Idea:** Uses a mathematical **Fibonacci Sphere** algorithm to uniformly distribute nodes across a 3D globe surface. The camera supports smooth orbital rotation via mouse dragging.

## 7. Spaced Repetition (Revision)
- **Purpose:** Automating the study process to maximize memory retention.
- **How it works:** Notes are surfaced in a queue based on their `review_date`.
- **Implementation Idea:** A deterministic interval ladder algorithm processes user feedback. "EASY" rapidly expands the interval days, while "AGAIN" collapses it back to 1 day.

## 8. Timeline
- **Purpose:** A chronological log of learning progression.
- **How it works:** Presents an audit trail of note creations, updates, and reviews.
- **Implementation Idea:** Rendered using a heavily customized JavaFX `ListView` cell factory to draw continuous connecting lines and visual milestones dynamically.

## 9. Dashboard
- **Purpose:** A high-level statistical overview.
- **How it works:** Aggregates database metrics into visual charts and counters.
- **Implementation Idea:** Database `COUNT` operations are offloaded to background threads. Includes fallback "Empty State" panes when no data exists.

## 10. Wikipedia Research
- **Purpose:** Frictionless contextual research without leaving the application.
- **How it works:** Users search Wikipedia; the system parses the REST API JSON response into a summary.
- **Implementation Idea:** Clicking "Create Note" auto-populates the editor with the Wikipedia summary, title, and a permanent source link.

## 11. JSON Import/Export
- **Purpose:** Data portability and backup.
- **How it works:** Dumps the complete relational graph to a flat JSON file using Jackson.
- **Implementation Idea:** `ImportExportService` merges incoming data inside an atomic SQL transaction, rolling back automatically if an error occurs to prevent database corruption.

## 12. PDF Export
- **Purpose:** Document generation for physical study.
- **How it works:** Converts Notes into professionally formatted pages using OpenPDF.
- **Implementation Idea:** The system bundles and explicitly embeds the `FreeSerif.ttf` Unicode font into the PDF payload, ensuring seamless rendering of **Bengali** and complex characters.

## 13. Multithreading
- **Purpose:** Keeping the JavaFX GUI strictly responsive.
- **How it works:** A bespoke `TaskExecutor` thread pool handles DB reads. 
- **Implementation Idea:** Background callbacks safely tunnel back to the UI via `Platform.runLater()`. UI buttons are temporarily disabled during fetch states to prevent duplicate events.

## 14. Validation/Error Handling
- **Purpose:** Ensuring data integrity and user clarity.
- **How it works:** `UiUtils` centrally manages dialogs.
- **Implementation Idea:** Raw Java stack traces (like `SQLException`) are caught at the controller layer and translated into user-friendly alerts, preventing application crashes.
