# Course & Academic Concept Mapping

The MindMap project practically demonstrates numerous fundamental concepts taught in a standard Computer Science curriculum. This document maps the implementation to corresponding academic subjects.

## Object-Oriented Programming (OOP)
- **Encapsulation:** Domain entities (like `Note`, `Tag`) hide their internal state and provide access via public getters and setters.
- **Polymorphism & Inheritance:** Demonstrated extensively through JavaFX's UI component hierarchy (e.g., custom `ListCell` extensions in `TimelineController`).
- **Separation of Concerns:** The project utilizes a strict Controller → Service → Repository layered architecture.
- **DAO Pattern:** The `Repository` classes abstract raw SQL queries away from the business logic.

## Data Structures
- **Graphs (Directed/Undirected):** The core knowledge base is a mathematical Graph where Notes are Nodes and Connections are Edges.
- **Maps / Hash Tables:** Utilized heavily in the JSON `ImportExportService` (`HashMap<String, Integer>`) to track and deduplicate entities in memory before executing SQL merges.
- **Lists & Sets:** Used throughout the application to manage collections of data and ensure uniqueness (e.g., deduplicating tags locally using `HashSet`).

## Algorithms
- **Graph Layout:** The 2D Knowledge Graph employs an iterative recursive function to position nodes dynamically around a central root node to prevent overlapping.
- **3D Sphere Distribution:** The `KnowledgeSpace3D` relies on the **Fibonacci Sphere algorithm** to calculate uniform `(x, y, z)` spatial distributions across a globe's surface.
- **Scheduling (Spaced Repetition):** Implements a deterministic time-interval ladder algorithm to calculate exponentially expanding review dates based on graded human feedback.

## Database Management Systems (DBMS)
- **Relational Design:** Highly normalized schema separating entities.
- **Many-to-Many Relationships:** Demonstrated using the `note_tags` junction table to map Notes to Tags.
- **Referential Integrity:** Strictly enforced via `FOREIGN KEY` constraints.
- **Cascade Operations:** `ON DELETE CASCADE` is utilized to organically prune orphaned connections and tags when a parent note is destroyed.
- **ACID Transactions:** The `ImportExportService` leverages `conn.setAutoCommit(false)` and `conn.commit()` to guarantee atomicity during massive bulk imports.

## Operating Systems & Concurrency
- **Threading:** Separates the UI rendering thread (JavaFX Application Thread) from heavy I/O operations.
- **Thread Pools:** `TaskExecutor` implements a fixed worker pool, mitigating the overhead of constantly spawning and destroying raw threads.
- **Daemon Threads:** Worker threads are flagged as daemons, allowing the JVM to safely terminate upon window closure without zombie processes.
- **Synchronization:** Callbacks are safely marshaled back to the UI thread using `Platform.runLater()`.

## Web Technologies & APIs
- **RESTful Consumption:** Integrates with the live Wikipedia REST API.
- **JSON Parsing:** Uses Jackson to serialize complex cyclical Java object graphs into flat hierarchical JSON files, and vice versa.
- **HTTP Client:** Uses `java.net.http.HttpClient` to manage asynchronous network streams, timeouts, and headers.

## Software Engineering
- **Version Control:** Managed systematically using Git, with discrete commits per development phase.
- **Automated Testing:** Contains 156 comprehensive JUnit 5 tests covering Repository SQL, Service business logic, and concurrent execution behavior.
- **Defensive Programming:** Implements robust input validation and translates system Exceptions into safe, user-friendly UI dialogs.
