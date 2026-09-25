with open('src/main/java/com/mindmap/controller/ReviewDialogController.java', 'r') as f:
    text = f.read()

text = text.replace('reviewQueue.get(currentIndex).setNote(updatedNote);', '')
text = text.replace('displayCurrentCard();', 'loadCurrentCard();')

with open('src/main/java/com/mindmap/controller/ReviewDialogController.java', 'w') as f:
    f.write(text)
