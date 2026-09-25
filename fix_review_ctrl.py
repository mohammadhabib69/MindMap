import re
with open('src/main/java/com/mindmap/controller/ReviewDialogController.java', 'r') as f:
    text = f.read()

text = re.sub(r'(controller\.setNote\(note\);)(?!\s*controller\.setEditHandler)', r'\1\n            controller.setEditHandler(this::handleEditNote);', text)

edit_method = """
    private void handleEditNote(com.mindmap.model.Note note) {
        if (note == null) return;
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/note_editor.fxml"));
            javafx.scene.Parent root = loader.load();
            com.mindmap.controller.NoteEditorController controller = loader.getController();
            controller.setNoteService(new com.mindmap.service.NoteService());

            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("Edit Note - " + note.getTitle());
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            if (dialogStage != null) {
                stage.initOwner(dialogStage);
            }
            stage.setScene(new javafx.scene.Scene(root));
            controller.setDialogStage(stage);

            com.mindmap.model.Note targetNote = new com.mindmap.service.NoteService().getNoteWithTags(note.getId()).orElse(note);
            controller.setNote(targetNote, com.mindmap.controller.NoteEditorMode.EDIT);

            stage.showAndWait();

            if (controller.isSaved()) {
                // Refresh the current note in the queue if needed, or just let it be.
                com.mindmap.model.Note updatedNote = new com.mindmap.service.NoteService().getNoteWithTags(note.getId()).orElse(note);
                if (currentIndex >= 0 && currentIndex < reviewQueue.size()) {
                    reviewQueue.get(currentIndex).setNote(updatedNote);
                    displayCurrentCard();
                }
            }
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }
}"""
if 'private void handleEditNote' not in text:
    text = re.sub(r'}\s*$', edit_method, text)

with open('src/main/java/com/mindmap/controller/ReviewDialogController.java', 'w') as f:
    f.write(text)
