with open('src/main/java/com/mindmap/controller/ResearchController.java', 'r') as f:
    text = f.read()

old_code = """    private void displaySummary(WikipediaPageSummary summary) {
        if (summary != null) {
            lblTitle.setText(summary.getTitle());
            lblSummary.setText(summary.getExtract());
            boxSource.setVisible(true);
            boxSource.setManaged(true);
            boxActions.setVisible(true);
        }
    }"""

new_code = """    private void displaySummary(WikipediaPageSummary summary) {
        if (summary != null) {
            lblTitle.setText(summary.getTitle());
            lblSummary.setText(summary.getExtract());
            if (lblSource != null) {
                lblSource.setText("Wikipedia");
            }
            if (boxSource != null) {
                boxSource.setVisible(true);
                boxSource.setManaged(true);
            }
            if (boxActions != null) {
                boxActions.setVisible(true);
            }
            
            if (boxEmptySelection != null) {
                boxEmptySelection.setVisible(false);
                boxEmptySelection.setManaged(false);
            }
            if (boxSelectionDetails != null) {
                boxSelectionDetails.setVisible(true);
                boxSelectionDetails.setManaged(true);
            }
        }
    }"""

if old_code in text:
    text = text.replace(old_code, new_code)
else:
    print("Failed to find old code!")

with open('src/main/java/com/mindmap/controller/ResearchController.java', 'w') as f:
    f.write(text)
