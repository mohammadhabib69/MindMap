with open('src/main/java/com/mindmap/controller/ResearchController.java', 'r') as f:
    text = f.read()

clear_old = """    private void clearDetails() {
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
clear_new = """    private void clearDetails() {
        currentLoadingTitle = null;
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
        if (boxLoadingSelection != null) {
            boxLoadingSelection.setVisible(false);
            boxLoadingSelection.setManaged(false);
        }
    }"""

text = text.replace(clear_old, clear_new)

with open('src/main/java/com/mindmap/controller/ResearchController.java', 'w') as f:
    f.write(text)
