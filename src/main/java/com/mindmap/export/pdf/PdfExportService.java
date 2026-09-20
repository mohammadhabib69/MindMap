package com.mindmap.export.pdf;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.mindmap.model.Note;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PdfExportService {
    
    private static final Logger LOGGER = Logger.getLogger(PdfExportService.class.getName());
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private Font titleFont;
    private Font subtitleFont;
    private Font normalFont;
    private Font metadataFont;

    public PdfExportService() {
        initFonts();
    }

    private void initFonts() {
        try {
            byte[] fontBytes = loadFontBytes("/fonts/NotoSansBengali-Regular.ttf");
            if (fontBytes != null) {
                BaseFont baseFont = BaseFont.createFont("NotoSansBengali-Regular.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, true, fontBytes, null);
                titleFont = new Font(baseFont, 18, Font.BOLD);
                subtitleFont = new Font(baseFont, 14, Font.BOLD);
                normalFont = new Font(baseFont, 11, Font.NORMAL);
                metadataFont = new Font(baseFont, 10, Font.ITALIC);
            } else {
                LOGGER.warning("Could not load Bengali font, falling back to default.");
                titleFont = new Font(Font.HELVETICA, 18, Font.BOLD);
                subtitleFont = new Font(Font.HELVETICA, 14, Font.BOLD);
                normalFont = new Font(Font.HELVETICA, 11, Font.NORMAL);
                metadataFont = new Font(Font.HELVETICA, 10, Font.ITALIC);
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error initializing PDF fonts", e);
            titleFont = new Font(Font.HELVETICA, 18, Font.BOLD);
            subtitleFont = new Font(Font.HELVETICA, 14, Font.BOLD);
            normalFont = new Font(Font.HELVETICA, 11, Font.NORMAL);
            metadataFont = new Font(Font.HELVETICA, 10, Font.ITALIC);
        }
    }

    private byte[] loadFontBytes(String path) {
        try (InputStream is = getClass().getResourceAsStream(path)) {
            if (is == null) return null;
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int nRead;
            byte[] data = new byte[16384];
            while ((nRead = is.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            return buffer.toByteArray();
        } catch (IOException e) {
            return null;
        }
    }

    public void exportSingleNote(Note note, File destination) throws PdfExportException {
        if (note == null) throw new IllegalArgumentException("Note cannot be null");
        if (destination == null) throw new IllegalArgumentException("Destination file cannot be null");

        Document document = new Document(PageSize.A4, 36, 36, 36, 36);
        try {
            FileOutputStream fos = new FileOutputStream(destination);
            PdfWriter.getInstance(document, fos);
            document.open();

            addHeader(document, "MindMap Note Export");
            addNoteToDocument(document, note);
            document.close();
            fos.close();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to export single note to PDF", e);
            throw new PdfExportException("Failed to export note: " + e.getMessage(), e);
        }
    }

    public void exportMultipleNotes(List<Note> notes, File destination) throws PdfExportException {
        if (notes == null || notes.isEmpty()) {
            throw new PdfExportException("No notes available to export.");
        }
        if (destination == null) throw new IllegalArgumentException("Destination file cannot be null");

        Document document = new Document(PageSize.A4, 36, 36, 36, 36);
        try {
            FileOutputStream fos = new FileOutputStream(destination);
            PdfWriter.getInstance(document, fos);
            document.open();

            addCoverPage(document, notes.size());
            
            for (int i = 0; i < notes.size(); i++) {
                document.newPage();
                addNoteToDocument(document, notes.get(i));
            }
            document.close();
            fos.close();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to export all notes to PDF", e);
            throw new PdfExportException("Failed to export notes: " + e.getMessage(), e);
        }
    }

    private void addCoverPage(Document document, int count) throws DocumentException {
        Paragraph title = new Paragraph("MindMap", new Font(titleFont.getBaseFont(), 24, Font.BOLD));
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingBefore(150);
        document.add(title);
        
        Paragraph subtitle = new Paragraph("Personal Knowledge Base", new Font(titleFont.getBaseFont(), 18, Font.NORMAL));
        subtitle.setAlignment(Element.ALIGN_CENTER);
        subtitle.setSpacingAfter(50);
        document.add(subtitle);
        
        Paragraph details = new Paragraph("Exported: " + LocalDateTime.now().format(DATE_FORMATTER) + "\nTotal Notes: " + count, normalFont);
        details.setAlignment(Element.ALIGN_CENTER);
        document.add(details);
    }

    private void addHeader(Document document, String headerText) throws DocumentException {
        Paragraph header = new Paragraph(headerText, metadataFont);
        header.setAlignment(Element.ALIGN_RIGHT);
        header.setSpacingAfter(20);
        document.add(header);
    }

    private void addNoteToDocument(Document document, Note note) throws DocumentException {
        // Title
        Paragraph title = new Paragraph(note.getTitle() != null ? note.getTitle() : "Untitled", titleFont);
        title.setSpacingAfter(10);
        document.add(title);
        
        // Metadata table
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingAfter(20);
        try {
            table.setWidths(new float[]{1, 3});
        } catch (DocumentException e) {
            // Should not happen
        }
        
        addMetaRow(table, "Subject:", note.getSubject());
        addMetaRow(table, "Difficulty:", note.getDifficulty());
        addMetaRow(table, "Tags:", note.getTagsString());
        addMetaRow(table, "Created:", note.getCreatedAt() != null ? note.getCreatedAt().format(DATE_FORMATTER) : "N/A");
        addMetaRow(table, "Updated:", note.getUpdatedAt() != null ? note.getUpdatedAt().format(DATE_FORMATTER) : "N/A");
        
        document.add(table);
        
        // Separator
        Paragraph separator = new Paragraph("----------------------------------------------------------------------", metadataFont);
        separator.setSpacingAfter(15);
        document.add(separator);
        
        // Content
        if (note.getContent() != null && !note.getContent().trim().isEmpty()) {
            Paragraph content = new Paragraph(note.getContent(), normalFont);
            content.setSpacingAfter(15);
            document.add(content);
        }
    }

    private void addMetaRow(PdfPTable table, String label, String value) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, subtitleFont));
        labelCell.setBorder(0);
        
        PdfPCell valueCell = new PdfPCell(new Phrase(value != null ? value : "", normalFont));
        valueCell.setBorder(0);
        
        table.addCell(labelCell);
        table.addCell(valueCell);
    }
}
