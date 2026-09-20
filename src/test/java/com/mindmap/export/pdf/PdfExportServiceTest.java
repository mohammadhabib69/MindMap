package com.mindmap.export.pdf;

import com.mindmap.model.Note;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

public class PdfExportServiceTest {

    private PdfExportService pdfService;

    @BeforeEach
    void setUp() {
        pdfService = new PdfExportService();
    }

    @Test
    void testExportSingleNote() throws Exception {
        Note note = new Note();
        note.setTitle("Machine Learning");
        note.setSubject("Research");
        note.setDifficulty("Medium");
        note.setContent("Machine learning is a field of study...\n\nSource: Wikipedia\nhttps://en.wikipedia.org/wiki/Machine_learning");
        note.setCreatedAt(LocalDateTime.now());
        note.setUpdatedAt(LocalDateTime.now());
        note.addTag(new com.mindmap.model.Tag("wikipedia"));

        File tempFile = File.createTempFile("single_note", ".pdf");
        tempFile.deleteOnExit();

        pdfService.exportSingleNote(note, tempFile);

        assertTrue(tempFile.exists());
        assertTrue(tempFile.length() > 0, "PDF file should not be empty");
    }

    @Test
    void testExportBengaliNote() throws Exception {
        Note note = new Note();
        note.setTitle("বাংলাদেশ");
        note.setSubject("History");
        note.setContent("বাংলাদেশ দক্ষিণ এশিয়ার একটি রাষ্ট্র।");

        File tempFile = File.createTempFile("bengali_note", ".pdf");
        tempFile.deleteOnExit();

        pdfService.exportSingleNote(note, tempFile);

        assertTrue(tempFile.exists());
        assertTrue(tempFile.length() > 0);
    }

    @Test
    void testExportMultipleNotes() throws Exception {
        Note note1 = new Note("Note 1", "Content 1", "Subject 1", "Easy");
        Note note2 = new Note("Note 2", "Content 2", "Subject 2", "Hard");

        File tempFile = File.createTempFile("all_notes", ".pdf");
        tempFile.deleteOnExit();

        pdfService.exportMultipleNotes(Arrays.asList(note1, note2), tempFile);

        assertTrue(tempFile.exists());
        assertTrue(tempFile.length() > 0);
    }

    @Test
    void testExportEmptyNotesListThrowsException() throws Exception {
        File tempFile = File.createTempFile("empty", ".pdf");
        tempFile.deleteOnExit();

        assertThrows(PdfExportException.class, () -> {
            pdfService.exportMultipleNotes(Collections.emptyList(), tempFile);
        });
    }

    @Test
    void testExportSingleNoteWithNullValues() throws Exception {
        Note note = new Note(); // everything null
        File tempFile = File.createTempFile("null_note", ".pdf");
        tempFile.deleteOnExit();

        pdfService.exportSingleNote(note, tempFile);

        assertTrue(tempFile.exists());
        assertTrue(tempFile.length() > 0);
    }
}
