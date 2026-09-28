with open('src/main/java/com/mindmap/controller/QuizResultController.java', 'r') as f:
    text = f.read()

import_statement = "import javafx.fxml.FXMLLoader;\nimport javafx.stage.Stage;\nimport javafx.scene.Scene;\nimport javafx.stage.Modality;\nimport com.mindmap.model.Note;\n"

# Add imports
text = text.replace("import javafx.scene.layout.VBox;\n", "import javafx.scene.layout.VBox;\n" + import_statement)

# Fix source button action
old_action = """                        sourceBtn.setOnAction(e -> {
                            try {
                                ViewManager.ViewResult result = ViewManager.loadViewWithController("/fxml/mindmap.fxml");
                                // Can't easily jump to specific note without passing through MainController,
                                // but we can show an alert or a quick view dialog.
                                UiUtils.showInfo("Source Note", item.getSourceNote().getContent());
                            } catch (Exception ex) {}
                        });"""

new_action = """                        sourceBtn.setOnAction(e -> {
                            try {
                                Note note = item.getSourceNote();
                                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/note_view.fxml"));
                                Parent root = loader.load();
                                NoteViewController controller = loader.getController();
                                Stage stage = new Stage();
                                stage.setTitle("View Note - " + note.getTitle());
                                stage.initModality(Modality.APPLICATION_MODAL);
                                if (sourceBtn.getScene() != null && sourceBtn.getScene().getWindow() != null) {
                                    stage.initOwner(sourceBtn.getScene().getWindow());
                                }
                                stage.setScene(new Scene(root));
                                controller.setNote(note, stage, null);
                                stage.show();
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        });"""

text = text.replace(old_action, new_action)

with open('src/main/java/com/mindmap/controller/QuizResultController.java', 'w') as f:
    f.write(text)

