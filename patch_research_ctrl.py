import re

with open('src/main/java/com/mindmap/controller/ResearchController.java', 'r') as f:
    text = f.read()

# Add the missing FXML fields
imports = """
import javafx.scene.layout.VBox;
"""
if "import javafx.scene.layout.VBox;" not in text:
    text = text.replace('import javafx.scene.layout.HBox;', 'import javafx.scene.layout.HBox;\nimport javafx.scene.layout.VBox;')

fields = """
    @FXML private VBox boxEmptyResults;
    @FXML private VBox boxEmptySelection;
    @FXML private VBox boxSelectionDetails;
"""
if "boxEmptyResults" not in text:
    text = text.replace('@FXML private HBox boxSource;', '@FXML private HBox boxSource;\n' + fields)

# Modify clearDetails to hide selection details and show empty selection
clear_details_old = """    private void clearDetails() {
        lblTitle.setText("");
        lblSummary.setText("");
        boxSource.setVisible(false);
        boxSource.setManaged(false);
        boxActions.setVisible(false);
        currentSummary = null;
    }"""
clear_details_new = """    private void clearDetails() {
        lblTitle.setText("");
        lblSummary.setText("");
        currentSummary = null;
        if (boxEmptySelection != null) {
            boxEmptySelection.setVisible(true);
            boxEmptySelection.setManaged(true);
        }
        if (boxSelectionDetails != null) {
            boxSelectionDetails.setVisible(false);
            boxSelectionDetails.setManaged(false);
        }
    }"""
text = text.replace(clear_details_old, clear_details_new)

# Modify handleSearch to toggle boxEmptyResults
search_success = """            setLoadingState(false, results.isEmpty() ? "No matching Wikipedia articles found." : results.size() + " results found.");
            listResults.getItems().setAll(results);"""
search_success_new = """            setLoadingState(false, results.isEmpty() ? "No matching Wikipedia articles found." : results.size() + " results found.");
            listResults.getItems().setAll(results);
            if (boxEmptyResults != null) {
                boolean empty = results.isEmpty();
                boxEmptyResults.setVisible(empty);
                boxEmptyResults.setManaged(empty);
            }
"""
text = text.replace(search_success, search_success_new)

# Modify displaySummary to show selection details and hide empty selection
display_summary_old = """    private void displaySummary(WikipediaPageSummary summary) {
        lblTitle.setText(summary.getTitle());
        lblSummary.setText(summary.getExtract());
        lblSource.setText("Wikipedia");
        boxSource.setVisible(true);
        boxSource.setManaged(true);
        boxActions.setVisible(true);
    }"""
display_summary_new = """    private void displaySummary(WikipediaPageSummary summary) {
        lblTitle.setText(summary.getTitle());
        lblSummary.setText(summary.getExtract());
        lblSource.setText("Wikipedia");
        
        if (boxEmptySelection != null) {
            boxEmptySelection.setVisible(false);
            boxEmptySelection.setManaged(false);
        }
        if (boxSelectionDetails != null) {
            boxSelectionDetails.setVisible(true);
            boxSelectionDetails.setManaged(true);
        }
    }"""
text = text.replace(display_summary_old, display_summary_new)

# Improve list cell factory
cell_factory_old = """        listResults.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(WikipediaSearchResult item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getTitle() + "\\n" + item.getSnippet());
                }
            }
        });"""
cell_factory_new = """        listResults.setCellFactory(param -> new ListCell<>() {
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
text = text.replace(cell_factory_old, cell_factory_new)

with open('src/main/java/com/mindmap/controller/ResearchController.java', 'w') as f:
    f.write(text)
