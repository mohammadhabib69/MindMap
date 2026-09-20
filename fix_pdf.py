import re

with open('src/main/java/com/mindmap/export/pdf/PdfExportService.java', 'r') as f:
    content = f.read()

single_export_old = '''        Document document = new Document(PageSize.A4, 36, 36, 36, 36);
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
        }'''

single_export_new = '''        Document document = new Document(PageSize.A4, 36, 36, 36, 36);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(destination);
            PdfWriter.getInstance(document, fos);
            document.open();

            addHeader(document, "MindMap Note Export");
            addNoteToDocument(document, note);
            document.close();
        } catch (Exception e) {
            if (document.isOpen()) {
                try { document.close(); } catch (Exception ignored) {}
            }
            LOGGER.log(Level.SEVERE, "Failed to export single note to PDF", e);
            throw new PdfExportException("Failed to export note: " + e.getMessage(), e);
        } finally {
            if (fos != null) {
                try { fos.close(); } catch (IOException ignored) {}
            }
        }'''

multi_export_old = '''        Document document = new Document(PageSize.A4, 36, 36, 36, 36);
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
        }'''

multi_export_new = '''        Document document = new Document(PageSize.A4, 36, 36, 36, 36);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(destination);
            PdfWriter.getInstance(document, fos);
            document.open();

            addCoverPage(document, notes.size());
            
            for (int i = 0; i < notes.size(); i++) {
                document.newPage();
                addNoteToDocument(document, notes.get(i));
            }
            document.close();
        } catch (Exception e) {
            if (document.isOpen()) {
                try { document.close(); } catch (Exception ignored) {}
            }
            LOGGER.log(Level.SEVERE, "Failed to export all notes to PDF", e);
            throw new PdfExportException("Failed to export notes: " + e.getMessage(), e);
        } finally {
            if (fos != null) {
                try { fos.close(); } catch (IOException ignored) {}
            }
        }'''

content = content.replace(single_export_old, single_export_new)
content = content.replace(multi_export_old, multi_export_new)

with open('src/main/java/com/mindmap/export/pdf/PdfExportService.java', 'w') as f:
    f.write(content)
