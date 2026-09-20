# Development History

The MindMap project was built incrementally following a rigorous phased approach. Each phase introduced a specific set of features and architectural layers, building systematically upon the previous foundation.

### Phase 1: Foundation
Established the core project structure using Maven, JavaFX, and an embedded SQLite database. Configured the `DatabaseManager` and initialized the baseline database schema (`notes`, `tags`, `note_tags`).

### Phase 2: UI and Navigation
Designed the JavaFX application shell, establishing the primary left-hand navigation sidebar and the main content area. Implemented the `ViewManager` to hot-swap FXML screens efficiently.

### Phase 3: OOP Models and Repository Layer
Implemented the core Domain Model objects (POJOs) and the Data Access Object (DAO) pattern via Repositories. Bridged the Java objects to raw SQLite `PreparedStatement` executions.

### Phase 4: Notes CRUD and Tags
Wired the JavaFX UI to the repository layer, enabling Create, Read, Update, and Delete operations for Notes. Built the tagging system, including the comma-separated input parsing and Many-to-Many relational linking.

### Phase 5: Connections and 2D Knowledge Graph Foundation
Introduced the `connections` schema. Developed the visual 2D interactive canvas utilizing a recursive auto-layout algorithm, allowing users to select and link distinct notes visually.

### Phase 6: 3D Knowledge Space
Engineered the 3D visualizer using JavaFX `SubScene` and `PerspectiveCamera`. Implemented the Fibonacci sphere mathematical distribution algorithm to map notes uniformly around a 3D globe.

### Phase 7: Advanced Search and Filtering
Constructed the Search module, enabling full-text query matching alongside dropdown filters for Subjects, Difficulty, Connection status, and Date Ranges. 

### Phase 7.5: 2D Knowledge Graph Visual Redesign
Polished the 2D graph with refined aesthetics, smooth panning and zooming via Affine transforms, and contextual node-inspector tooltips.

### Phase 8: Spaced Repetition
Integrated the `revisions` table and built the scheduling engine. Implemented the deterministic algorithm to adjust review intervals dynamically based on user feedback (Again, Hard, Good, Easy).

### Phase 8.1: Revision Performance Optimization
Optimized the SQL queries underlying the spaced repetition engine to handle massive review queues smoothly without stalling the application.

### Phase 9: Learning Timeline
Created the `learning_events` audit table. Built the specialized Timeline UI utilizing a custom JavaFX `ListCell` factory to draw continuous visual tracks for historical learning milestones.

### Phase 10: Dashboard and Statistics
Built the central Dashboard. Implemented heavy aggregation SQL queries to map data into visual JavaFX Charts and statistical counter cards.

### Phase 10.1: Dashboard Performance Improvements
Refactored the dashboard components to dynamically resize properly, improving fluid responsiveness on window maximization.

### Phase 11: Multithreading and Background Tasks
Introduced the `TaskExecutor` utility. Migrated all heavy database transactions off the JavaFX Application Thread onto a Daemon worker pool, securing a 60 FPS UI experience.

### Phase 12: JSON Import/Export
Implemented Jackson serialization to dump the entire relational database into a flat `.json` file. Engineered the atomic SQL transaction merger to safely import and deduplicate notes globally.

### Phase 13: Wikipedia Research/API Integration
Integrated Java 11's `HttpClient` to query the live Wikipedia REST API asynchronously. Allowed users to automatically instantiate pre-filled Notes directly from fetched research summaries.

### Phase 14: PDF Export
Leveraged OpenPDF to generate formatted study documents. Resolved complex character encoding bugs by bundling and explicitly embedding the `FreeSerif` TrueType font for native Bengali and Unicode support.

### Phase 15: Error Handling, Validation and UX Polish
Conducted a complete sweep to replace raw stack traces with centralized `UiUtils` dialogs. Hardened edge cases (like unsaved editor changes) and improved Empty State UI feedback across all screens.

### Phase 16: Testing, Bug Fixing and QA
Executed a massive Quality Assurance pass. Fixed complex resource leaks, OS-level window closure bypasses, and threading edge cases. Ran the full 156-test suite to secure the final stable build.

### Phase 17: Documentation and Academic Presentation Preparation
Finalized the architectural, database, and feature documentation. Mapped the project implementation to academic Computer Science theories in preparation for university viva defense.
