# Viva Preparation Q&A

This document provides concise, technically accurate answers to anticipated questions during the final academic project defense (Viva).

## General
**Q: What is MindMap?**
A: MindMap is a desktop application functioning as a personal knowledge base. It allows users to create notes, link them together conceptually to form a knowledge graph, and automatically schedule them for review using Spaced Repetition.

**Q: What problem does it solve?**
A: Traditional note-taking apps use rigid folders, which isolate information. MindMap mimics human memory by connecting related ideas, helping users retain information long-term via active recall and spaced repetition.

**Q: Why did you choose JavaFX?**
A: JavaFX provides a robust, hardware-accelerated GUI toolkit capable of complex 2D and 3D rendering natively within Java, which is essential for the Knowledge Graph visualizations.

**Q: Why SQLite?**
A: SQLite is an embedded database. It stores the entire relational structure in a single local file (`mindmap.sqlite`), eliminating the need for users to install or configure a separate database server like MySQL.

**Q: Why Maven?**
A: Maven automates the build process, handles the compilation pipeline, and strictly manages external dependencies like Jackson (JSON) and OpenPDF, ensuring the project compiles consistently on any machine.

## Object-Oriented Programming
**Q: Where is OOP used in this project?**
A: Everywhere. The `model` package uses Encapsulation to secure entity states. The layered architecture strictly separates UI (Controllers) from Data Access (Repositories).

**Q: Why use Services and Repositories?**
A: It's the Single Responsibility Principle. Repositories only handle SQL queries. Services handle the actual business logic (like preventing duplicate tags). This makes the code modular and easily testable.

## Database
**Q: What is a foreign key and where is it used?**
A: A foreign key links a column in one table to the primary key of another, ensuring data consistency. In MindMap, the `connections` table uses foreign keys pointing to `notes(id)`.

**Q: Why use a `note_tags` table?**
A: Because Notes and Tags have a Many-to-Many relationship (a note can have many tags, and a tag can belong to many notes). A junction table is required to normalize this in SQL.

**Q: Why use Transactions during JSON Import?**
A: A JSON import may insert hundreds of notes. If it fails halfway, we don't want a partially corrupted database. Wrapping it in a transaction ensures it's "all or nothing."

**Q: Why use `ON DELETE CASCADE`?**
A: If a user deletes a Note, the database automatically deletes any associated rows in the `note_tags`, `connections`, and `revisions` tables, preventing orphaned data without requiring extra Java code.

## Graph Visualizations
**Q: What is a Knowledge Graph?**
A: A network where nodes represent distinct concepts (Notes) and edges represent the relationships (Connections) between them.

**Q: How are connections stored?**
A: In the `connections` table as directional pairs (`from_note_id` to `to_note_id`).

**Q: How does the 3D visualization work?**
A: It uses JavaFX `PerspectiveCamera` and `SubScene`. The mathematical Fibonacci Sphere algorithm calculates uniform 3D coordinates to wrap the nodes around a globe.

**Q: Why is the 3D graph just a visualization layer?**
A: It reads the same SQLite database as the rest of the app. It's simply a different View mapping the existing model, adhering strictly to the Model-View-Controller paradigm.

## Multithreading
**Q: Why use background threads?**
A: Database queries and network requests are slow. If executed on the main UI thread, the application would freeze.

**Q: What is `TaskExecutor`?**
A: A custom utility class managing a fixed pool of daemon worker threads to execute heavy operations asynchronously.

**Q: Why use `Platform.runLater()`?**
A: In JavaFX, you can only modify UI components on the specific JavaFX Application Thread. `Platform.runLater()` safely hands the background result back to the UI thread for rendering.

## JSON and API
**Q: Why use Data Transfer Objects (DTOs) for JSON Export?**
A: It decouples our database structure from our export format. If we change our SQL schema later, we can still map it to the stable DTO to maintain backward compatibility with older backups.

**Q: How does Wikipedia integration work?**
A: It fires an asynchronous HTTP GET request to the Wikipedia REST API using Java 11's `HttpClient`. The JSON response is parsed to extract the summary text and source URL.

## PDF Export
**Q: How is Unicode/Bengali handled in the PDF?**
A: Standard PDF fonts don't support Bengali glyphs. We bundled a specialized TrueType font (`FreeSerif.ttf`), loaded it as bytes, and explicitly embedded it into the PDF payload via OpenPDF.

## Testing & QA
**Q: How many tests are present?**
A: 156 automated tests.

**Q: How are regression bugs handled?**
A: If a bug is found (like the PDF resource leak in Phase 16), we fix the root cause and ensure the test suite still passes without breaking existing features. 
