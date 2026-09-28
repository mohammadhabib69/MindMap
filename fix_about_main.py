with open('src/main/java/com/mindmap/controller/MainController.java', 'r') as f:
    text = f.read()

old_desc = """        javafx.scene.control.Label desc = new javafx.scene.control.Label(
            "An integrated platform for capturing thoughts,\\n" +
            "connecting ideas visually, and retaining\\n" +
            "knowledge through spaced repetition."
        );"""
new_desc = """        javafx.scene.control.Label desc = new javafx.scene.control.Label(
            "An integrated platform for capturing thoughts,\\n" +
            "connecting ideas visually, and retaining\\n" +
            "knowledge through spaced repetition."
        );
        desc.setStyle("-fx-font-size: 14px; -fx-text-fill: #475569; -fx-text-alignment: center;");
        
        javafx.scene.layout.VBox tech = new javafx.scene.layout.VBox(8);
        tech.setAlignment(javafx.scene.geometry.Pos.CENTER);
        javafx.scene.control.Label techTitle = new javafx.scene.control.Label("Technology");
        techTitle.setStyle("-fx-font-size: 11px; -fx-text-fill: #94a3b8; -fx-font-weight: bold;");
        javafx.scene.control.Label techDesc = new javafx.scene.control.Label("JavaFX • SQLite • Jackson • OpenPDF");
        techDesc.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b;");
        tech.getChildren().addAll(techTitle, techDesc);"""

if "techDesc" not in text:
    text = text.replace(old_desc, new_desc)
    
old_content = """        content.getChildren().addAll(header, version, desc);"""
new_content = """        content.getChildren().addAll(header, version, desc, tech);"""

if "tech)" not in text:
    text = text.replace(old_content, new_content)
    
# Wait, also we need to fix the button in the dialog to use primary button
old_btn = """        dialog.getDialogPane().getButtonTypes().add(javafx.scene.control.ButtonType.CLOSE);"""
new_btn = """        dialog.getDialogPane().getButtonTypes().add(javafx.scene.control.ButtonType.CLOSE);
        javafx.scene.Node closeButton = dialog.getDialogPane().lookupButton(javafx.scene.control.ButtonType.CLOSE);
        if (closeButton != null) {
            closeButton.getStyleClass().add("btn-secondary");
        }"""
if "closeButton.getStyleClass" not in text:
    text = text.replace(old_btn, new_btn)

with open('src/main/java/com/mindmap/controller/MainController.java', 'w') as f:
    f.write(text)
