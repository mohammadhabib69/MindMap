import re

with open('src/main/java/com/mindmap/controller/SettingsController.java', 'r') as f:
    content = f.read()

replacement = '''MindMapExport data = importExportService.validateAndPreview(file);
                    
                    Platform.runLater(() -> {
                        String preview = "Found " + (data.getNotes() != null ? data.getNotes().size() : 0) + " notes and " + 
                                         (data.getConnections() != null ? data.getConnections().size() : 0) + " connections.\\n\\nDo you want to proceed?";
                        boolean proceed = UiUtils.showConfirmation("Import Preview", "Ready to import from " + file.getName() + "\\n\\n" + preview);
                        if (proceed) {'''

content = content.replace('''MindMapExport data = importExportService.validateAndPreview(file);
                    
                    Platform.runLater(() -> {
                        boolean proceed = UiUtils.showConfirmation("Import Preview", "Ready to import from " + file.getName() + "\\n\\n" + previewText);
                        if (proceed) {''', replacement)

with open('src/main/java/com/mindmap/controller/SettingsController.java', 'w') as f:
    f.write(content)
