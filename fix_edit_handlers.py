import os
import re

def inject_edit_handler(filepath, controller_name, refresh_call):
    with open(filepath, 'r') as f:
        text = f.read()

    # If it already has setEditHandler for handleEditNote, skip
    if 'controller.setEditHandler' in text:
        # Check if we need to implement handleEditNote
        pass
    else:
        # Find where controller.setNote(note) or controller.setNote(selected) is called
        # and inject setEditHandler right after it
        text = re.sub(r'(controller\.setNote\([^)]+\);)', r'\1\n            controller.setEditHandler(this::handleEditNote);', text)
    
    # Check if handleEditNote exists
    if 'private void handleEditNote(Note note)' not in text and 'private void handleEditNote()' not in text:
        # Inject handleEditNote method at the bottom of the class
        edit_method = f"""
    private void handleEditNote(com.mindmap.model.Note note) {{
        if (note == null) return;
        try {{
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/note_editor.fxml"));
            javafx.scene.Parent root = loader.load();
            com.mindmap.controller.NoteEditorController controller = loader.getController();
            controller.setNoteService(new com.mindmap.service.NoteService());

            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("Edit Note - " + note.getTitle());
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            // Just use a new stage without owner if we can't easily get it
            stage.setScene(new javafx.scene.Scene(root));
            controller.setDialogStage(stage);

            com.mindmap.model.Note targetNote = new com.mindmap.service.NoteService().getNoteWithTags(note.getId()).orElse(note);
            controller.setNote(targetNote, com.mindmap.controller.NoteEditorMode.EDIT);

            stage.showAndWait();

            if (controller.isSaved()) {{
                {refresh_call}
            }}
        }} catch (java.io.IOException e) {{
            e.printStackTrace();
        }}
    }}
}}"""
        text = re.sub(r'}\s*$', edit_method, text)

    with open(filepath, 'w') as f:
        f.write(text)

inject_edit_handler('src/main/java/com/mindmap/controller/TimelineController.java', 'TimelineController', 'loadTimelineData();')
inject_edit_handler('src/main/java/com/mindmap/controller/RevisionController.java', 'RevisionController', 'loadRevisionData();')
inject_edit_handler('src/main/java/com/mindmap/controller/MainController.java', 'MainController', '')
# DashboardController already has handleEditNote(Note note), just needs setEditHandler
with open('src/main/java/com/mindmap/controller/DashboardController.java', 'r') as f:
    dash = f.read()
if 'controller.setEditHandler' not in dash:
    dash = re.sub(r'(controller\.setNote\(note\);)', r'\1\n            controller.setEditHandler(this::handleEditNote);', dash)
    with open('src/main/java/com/mindmap/controller/DashboardController.java', 'w') as f:
        f.write(dash)
