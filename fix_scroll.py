with open('src/main/java/com/mindmap/controller/ResearchController.java', 'r') as f:
    text = f.read()

old_factory = """        listResults.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(WikipediaSearchResult item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    VBox vBox = new VBox(2);
                    javafx.scene.control.Label titleLbl = new javafx.scene.control.Label(item.getTitle());
                    titleLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #0f172a; -fx-font-size: 13px;");
                    javafx.scene.control.Label snippetLbl = new javafx.scene.control.Label(item.getSnippet());
                    snippetLbl.setStyle("-fx-text-fill: #64748b; -fx-font-size: 11px;");
                    snippetLbl.setWrapText(true);
                    vBox.getChildren().addAll(titleLbl, snippetLbl);
                    setGraphic(vBox);
                    setText(null);
                }
            }
        });"""

new_factory = """        listResults.setCellFactory(param -> new ListCell<>() {
            private final VBox vBox = new VBox(2);
            private final javafx.scene.control.Label titleLbl = new javafx.scene.control.Label();
            private final javafx.scene.control.Label snippetLbl = new javafx.scene.control.Label();

            {
                titleLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #0f172a; -fx-font-size: 13px;");
                titleLbl.setWrapText(true);
                snippetLbl.setStyle("-fx-text-fill: #64748b; -fx-font-size: 11px;");
                snippetLbl.setWrapText(true);
                vBox.getChildren().addAll(titleLbl, snippetLbl);
                // Bind to list cell width minus some padding to prevent horizontal scroll
                vBox.prefWidthProperty().bind(widthProperty().subtract(30));
                vBox.maxWidthProperty().bind(widthProperty().subtract(30));
            }

            @Override
            protected void updateItem(WikipediaSearchResult item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    titleLbl.setText(item.getTitle());
                    snippetLbl.setText(item.getSnippet());
                    setGraphic(vBox);
                    setText(null);
                }
            }
        });"""

text = text.replace(old_factory, new_factory)

with open('src/main/java/com/mindmap/controller/ResearchController.java', 'w') as f:
    f.write(text)
