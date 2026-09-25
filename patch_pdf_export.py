import re

with open('src/main/java/com/mindmap/controller/NotesController.java', 'r') as f:
    content = f.read()

old_pdf = """    @FXML
    private void handleExportPdf() {
        Note selected = tableResults.getSelectionModel().getSelectedItem();
        if (selected == null) return;"""

new_pdf = """    @FXML
    private void handleExportPdf() {
        Note selected = tableResults.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        if (!com.mindmap.util.SecurityHelper.verifyPin(selected)) return;"""

content = content.replace(old_pdf, new_pdf)

with open('src/main/java/com/mindmap/controller/NotesController.java', 'w') as f:
    f.write(content)
