package com.mindmap.export;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mindmap.database.DatabaseInitializer;
import com.mindmap.database.DatabaseManager;
import com.mindmap.export.dto.ExportConnection;
import com.mindmap.export.dto.ExportNote;
import com.mindmap.export.dto.ExportRevision;
import com.mindmap.export.dto.MindMapExport;
import com.mindmap.service.ImportExportService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

public class JsonImportExportTest {

    private ImportExportService service;
    private File tempFile;

    @BeforeEach
    void setUp() throws Exception {
        // Initialize an in-memory database for testing if possible, or clear existing DB.
        // Assuming DatabaseInitializer clears or we can clear manually.
        DatabaseInitializer.initialize();
        clearDatabase();
        service = new ImportExportService();
        tempFile = File.createTempFile("mindmap_test", ".json");
    }

    @AfterEach
    void tearDown() throws Exception {
        clearDatabase();
        if (tempFile.exists()) {
            tempFile.delete();
        }
    }

    private void clearDatabase() throws Exception {
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM learning_events");
            stmt.execute("DELETE FROM revisions");
            stmt.execute("DELETE FROM connections");
            stmt.execute("DELETE FROM note_tags");
            stmt.execute("DELETE FROM tags");
            stmt.execute("DELETE FROM notes");
        }
    }

    @Test
    void testBasicSerializationAndDeserialization() throws Exception {
        MindMapExport export = new MindMapExport();
        export.setExportedAt(LocalDateTime.now());
        
        ExportNote note = new ExportNote();
        note.setExportId("uuid-123");
        note.setTitle("Test Note");
        note.setContent("Test Content বাংলা");
        note.setCreatedAt(LocalDateTime.now());
        export.getNotes().add(note);

        ObjectMapper mapper = JsonUtil.getMapper();
        String json = mapper.writeValueAsString(export);

        MindMapExport imported = mapper.readValue(json, MindMapExport.class);
        assertEquals(1, imported.getNotes().size());
        assertEquals("Test Note", imported.getNotes().get(0).getTitle());
        assertEquals("Test Content বাংলা", imported.getNotes().get(0).getContent());
    }

    @Test
    void testUnsupportedFormatVersion() throws Exception {
        String json = "{ \"formatVersion\": 2, \"notes\": [] }";
        Files.writeString(tempFile.toPath(), json);

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            service.validateAndPreview(tempFile);
        });
        assertTrue(thrown.getMessage().contains("Unsupported format version"));
    }

    @Test
    void testRoundTripExportImport() throws Exception {
        // Create an export DTO manually to simulate an import
        MindMapExport export = new MindMapExport();
        ExportNote note1 = new ExportNote();
        note1.setExportId("note-1");
        note1.setTitle("Note 1");
        note1.setContent("Content 1");
        note1.setTags(Collections.singletonList("Tag1"));
        note1.setCreatedAt(LocalDateTime.now());
        note1.setUpdatedAt(LocalDateTime.now());
        
        ExportNote note2 = new ExportNote();
        note2.setExportId("note-2");
        note2.setTitle("Note 2");
        note2.setContent("Content 2");
        note2.setCreatedAt(LocalDateTime.now());
        note2.setUpdatedAt(LocalDateTime.now());

        export.getNotes().add(note1);
        export.getNotes().add(note2);

        ExportConnection conn = new ExportConnection();
        conn.setFromNoteExportId("note-1");
        conn.setToNoteExportId("note-2");
        conn.setRelation("related");
        export.getConnections().add(conn);

        // Import the data
        service.importData(export);

        // Export it back
        MindMapExport roundTrip = service.exportData();
        assertEquals(2, roundTrip.getNotes().size());
        assertEquals(1, roundTrip.getTags().size());
        assertEquals("Tag1", roundTrip.getTags().get(0).getName());
        assertEquals(1, roundTrip.getConnections().size());
        assertEquals("related", roundTrip.getConnections().get(0).getRelation());

        // Also test duplicate import doesn't create new notes
        service.importData(export);
        MindMapExport roundTrip2 = service.exportData();
        assertEquals(2, roundTrip2.getNotes().size()); // should still be 2 due to merge strategy
    }

    @Test
    void testTransactionRollbackOnInvalidData() throws Exception {
        MindMapExport export = new MindMapExport();
        
        ExportNote note1 = new ExportNote();
        note1.setExportId("note-1");
        note1.setTitle("Valid Note");
        note1.setContent("Content 1");
        note1.setCreatedAt(LocalDateTime.now());
        note1.setUpdatedAt(LocalDateTime.now());
        export.getNotes().add(note1);

        // This note will fail because of title null
        ExportNote note2 = new ExportNote();
        note2.setExportId("note-2");
        note2.setTitle(null); // Invalid!
        export.getNotes().add(note2);

        assertThrows(Exception.class, () -> service.importData(export));

        // Verify DB is completely empty (rollback successful)
        MindMapExport roundTrip = service.exportData();
        assertEquals(0, roundTrip.getNotes().size());
    }
}
