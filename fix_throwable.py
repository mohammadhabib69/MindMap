with open('src/main/java/com/mindmap/controller/ReviewDialogController.java', 'r') as f:
    text = f.read()

old_catch = """                throwable -> {
                    setOutcomeButtonsDisable(false);
                    LOGGER.log(Level.SEVERE, "Failed to complete review: " + throwable.getMessage(), throwable);
                    UiUtils.showError("Review Error", "Failed to save review outcome: " + throwable.getMessage());
                    currentIndex++;
                    loadCurrentReview();
                }"""

new_catch = """                throwable -> {
                    setOutcomeButtonsDisable(false);
                    LOGGER.log(Level.SEVERE, "Failed to complete review: " + throwable.getMessage(), throwable);
                    UiUtils.showError("Review Error", "Failed to save review outcome: " + throwable.getMessage());
                    // Do NOT advance currentIndex, allow user to retry
                }"""

if old_catch in text:
    text = text.replace(old_catch, new_catch)
    with open('src/main/java/com/mindmap/controller/ReviewDialogController.java', 'w') as f:
        f.write(text)
    print("Fixed!")
else:
    print("Pattern not found!")
