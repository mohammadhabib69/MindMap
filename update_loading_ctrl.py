with open('src/main/java/com/mindmap/controller/ResearchController.java', 'r') as f:
    text = f.read()

if "boxLoadingSelection" not in text:
    text = text.replace('@FXML private VBox boxSelectionDetails;', '@FXML private VBox boxSelectionDetails;\n    @FXML private VBox boxLoadingSelection;\n    private String currentLoadingTitle;')

load_summary_old = """    private void loadSummary(String title) {
        setLoadingState(true, "Loading summary for " + title + "...");
        
        TaskExecutor.runAsync(() -> {
            return wikipediaService.getSummary(title);
        }, summary -> {
            setLoadingState(false, "Loaded summary.");
            currentSummary = summary;
            displaySummary(summary);
        }, error -> {
            LOGGER.log(Level.WARNING, "Failed to load summary", error);
            handleError(error);
        });
    }"""
    
load_summary_new = """    private void loadSummary(String title) {
        currentLoadingTitle = title;
        setLoadingState(true, "Loading summary for " + title + "...");
        
        if (boxEmptySelection != null) {
            boxEmptySelection.setVisible(false);
            boxEmptySelection.setManaged(false);
        }
        if (boxSelectionDetails != null) {
            boxSelectionDetails.setVisible(false);
            boxSelectionDetails.setManaged(false);
        }
        if (boxLoadingSelection != null) {
            boxLoadingSelection.setVisible(true);
            boxLoadingSelection.setManaged(true);
        }
        
        TaskExecutor.runAsync(() -> {
            return wikipediaService.getSummary(title);
        }, summary -> {
            if (!title.equals(currentLoadingTitle)) return;
            setLoadingState(false, "Loaded summary.");
            currentSummary = summary;
            
            if (boxLoadingSelection != null) {
                boxLoadingSelection.setVisible(false);
                boxLoadingSelection.setManaged(false);
            }
            displaySummary(summary);
        }, error -> {
            if (!title.equals(currentLoadingTitle)) return;
            LOGGER.log(Level.WARNING, "Failed to load summary", error);
            if (boxLoadingSelection != null) {
                boxLoadingSelection.setVisible(false);
                boxLoadingSelection.setManaged(false);
            }
            if (boxEmptySelection != null) {
                boxEmptySelection.setVisible(true);
                boxEmptySelection.setManaged(true);
            }
            lblStatus.setText("Unable to load this Wikipedia summary.");
        });
    }"""

text = text.replace(load_summary_old, load_summary_new)

with open('src/main/java/com/mindmap/controller/ResearchController.java', 'w') as f:
    f.write(text)
