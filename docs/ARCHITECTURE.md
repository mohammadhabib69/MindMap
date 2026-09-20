# Architecture Documentation

MindMap strictly adheres to a standard layered application architecture, designed to ensure modularity, maintainability, and clean separation of concerns.

## Application Architecture Flow

The data and control flow follows a top-down approach:

**JavaFX View (.fxml) → Controller → Service → Repository → SQLite Database**

1. **User Interaction:** The user interacts with the JavaFX UI.
2. **Controller:** The UI Controller captures the event, validates basic input, and requests data from the Service layer.
3. **Concurrency:** To prevent UI freezing, the Controller offloads the request to the `TaskExecutor`.
4. **Service:** The Service executes business logic and orchestrates one or more Repositories.
5. **Repository:** The Repository executes raw SQL queries against the Database.
6. **Database:** SQLite processes the query and returns a `ResultSet`.
7. **Callback:** The result bubbles back up, and `Platform.runLater()` safely updates the UI.

## Layer Responsibilities

### 1. Controller (`com.mindmap.controller`)
- Manages JavaFX lifecycle and FXML injection (`@FXML`).
- Handles user events (button clicks, list selections).
- Modifies UI state (enabling/disabling buttons, showing loading spinners).
- Contains **no** raw database queries or complex business logic.
- Utilizes `com.mindmap.util.UiUtils` for centralized error and confirmation dialogs.

### 2. Service (`com.mindmap.service`)
- Enforces core domain business logic and validation (e.g., preventing a note from connecting to itself, sanitizing duplicate tags).
- Coordinates transactions involving multiple repositories (e.g., creating a note, assigning tags, and generating a timeline event in one logical operation).
- Prepares Data Transfer Objects (DTOs) for import and export.

### 3. Repository (`com.mindmap.repository`)
- Solely responsible for Database Access (DAO pattern).
- Executes raw `PreparedStatement` SQL queries.
- Maps SQLite `ResultSet` rows directly into Java Model domain objects.
- Isolates the rest of the application from specific SQL syntax.

### 4. Model (`com.mindmap.model`)
- Plain Old Java Objects (POJOs) representing domain entities: `Note`, `Tag`, `Connection`, `ScheduledReview`, `TimelineEvent`.
- Contains only fields, getters, setters, and basic utility methods.

### 5. Database (`com.mindmap.database`)
- Manages the JDBC Connection lifecycle.
- Initializes the database schema if it doesn't exist.
- Executes startup `PRAGMA` statements to enforce foreign keys and concurrent timeouts.

### 6. Concurrency (`com.mindmap.concurrency`)
- **`TaskExecutor`**: A specialized utility managing a fixed pool of Daemon worker threads.
- Ensures the JavaFX Application Thread is never blocked by heavy I/O tasks.
- Exposes `runAsync(Supplier, onSuccess, onError)` to easily route background results back to the UI thread.

### 7. External (`com.mindmap.external`)
- Encapsulates interactions with the Wikipedia REST API using `java.net.http.HttpClient`.
- Parses incoming JSON responses into application-friendly models (`WikipediaPageSummary`).
