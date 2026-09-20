# Presentation / Demo Guide

This guide outlines a structured, 5-minute live demonstration flow, perfect for a university project defense.

## Recommended Live Demonstration Flow

1. **Launch Application:** Start at the Dashboard. Show the empty or overview state.
2. **Create a Note:** Navigate to Notes. Click "New Note". Type a Title, content, and select a difficulty.
3. **Add Tags:** Add multiple tags separated by commas. Save the Note.
4. **Search/Filter:** Go to Search. Type a partial title or select a Tag from the dropdown to prove the database filtering works instantly.
5. **Create Connections:** Back in Notes, select a note, click "Connections", and link it to a second note to form a relationship.
6. **2D Knowledge Graph:** Navigate to Mind Map. Show the nodes automatically formatting themselves. Hover over a node to show the tooltip.
7. **3D Knowledge Space:** Click "3D Space". Click and drag the mouse to rotate the interactive Fibonacci sphere of knowledge.
8. **Spaced Repetition (Revision):** Go to Revision. Show the "Due Today" counter. Start a session, reveal a note, and grade it (e.g., "Good") to schedule it for the future.
9. **Learning Timeline:** Switch to Timeline. Show the chronological audit trail of the notes you just created and the review you just completed.
10. **Dashboard:** Return to the Dashboard to show the statistics (Total Notes, Total Connections) have updated in real-time.
11. **Wikipedia Research:** Go to Research. Search for "Machine Learning". Wait for the summary to load. Click "Create Note" to demonstrate seamless API-to-Database integration.
12. **JSON Export:** Go to Settings. Export the database to JSON to prove data portability.
13. **PDF Export:** In Settings, export all notes to PDF. Open the PDF to prove Bengali and complex formatting exported successfully.

---

## Presentation Scripts

### Project Introduction (30–60 seconds)
*"Good morning. My project is 'MindMap', a comprehensive personal knowledge base and study organizer. Traditional note-taking applications organize information in isolated folders. MindMap solves this by allowing users to organically connect notes into a 'Knowledge Graph'. By visualizing these connections in 2D and 3D, users can better understand complex topics. Furthermore, it incorporates an automated Spaced Repetition engine that tests the user's memory on due notes, mathematically scheduling future reviews to ensure long-term retention."*

### Technical Architecture Explanation (1–2 minutes)
*"Technically, MindMap is built in Java using a strict MVC-style layered architecture. The front-end uses JavaFX for hardware-accelerated UI and 3D rendering. When a user interacts with the UI, the Controller offloads the work to a custom background `TaskExecutor` thread pool to prevent the application from freezing. The request is routed through a Service layer for business validation, down to a Repository layer, which executes raw SQL queries against a local, embedded SQLite database. The database enforces strict foreign-key relationships. Once the data is fetched, it is safely marshaled back to the JavaFX Application Thread. We also integrate the live Wikipedia REST API using Java's HttpClient, and utilize Jackson and OpenPDF for our import and export pipelines."*
