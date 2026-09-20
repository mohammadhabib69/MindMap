import re

with open('src/main/java/com/mindmap/export/pdf/PdfExportService.java', 'r') as f:
    content = f.read()

# Replace NotoSansBengali with FreeSerif
content = content.replace('NotoSansBengali-Regular.ttf', 'FreeSerif.ttf')

# Ensure we have FreeSerif in initFonts
old_init_fonts = r'''            byte\[\] fontBytes = loadFontBytes\("/fonts/FreeSerif.ttf"\);
            if \(fontBytes != null\) \{
                BaseFont baseFont = BaseFont.createFont\("FreeSerif.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, true, fontBytes, null\);
                titleFont = new Font\(baseFont, 18, Font.BOLD\);
                subtitleFont = new Font\(baseFont, 14, Font.BOLD\);
                normalFont = new Font\(baseFont, 11, Font.NORMAL\);
                metadataFont = new Font\(baseFont, 10, Font.ITALIC\);
            \} else \{
                LOGGER.warning\("Could not load Bengali font, falling back to default."\);
                titleFont = new Font\(Font.HELVETICA, 18, Font.BOLD\);
                subtitleFont = new Font\(Font.HELVETICA, 14, Font.BOLD\);
                normalFont = new Font\(Font.HELVETICA, 11, Font.NORMAL\);
                metadataFont = new Font\(Font.HELVETICA, 10, Font.ITALIC\);
            \}'''

new_init_fonts = r'''            byte[] fontBytes = loadFontBytes("/fonts/FreeSerif.ttf");
            if (fontBytes != null) {
                BaseFont baseFont = BaseFont.createFont("FreeSerif.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, true, fontBytes, null);
                titleFont = new Font(baseFont, 18, Font.BOLD);
                subtitleFont = new Font(baseFont, 14, Font.BOLD);
                normalFont = new Font(baseFont, 11, Font.NORMAL);
                metadataFont = new Font(baseFont, 10, Font.ITALIC);
            } else {
                LOGGER.warning("Could not load Unicode font, falling back to default.");
                titleFont = new Font(Font.HELVETICA, 18, Font.BOLD);
                subtitleFont = new Font(Font.HELVETICA, 14, Font.BOLD);
                normalFont = new Font(Font.HELVETICA, 11, Font.NORMAL);
                metadataFont = new Font(Font.HELVETICA, 10, Font.ITALIC);
            }'''

content = re.sub(old_init_fonts, new_init_fonts.replace('\\', '\\\\'), content)

with open('src/main/java/com/mindmap/export/pdf/PdfExportService.java', 'w') as f:
    f.write(content)
