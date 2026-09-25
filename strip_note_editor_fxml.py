import re

with open('src/main/resources/fxml/note_editor.fxml', 'r') as f:
    text = f.read()

# Remove the entire Private Note Section block
pattern = r'[ \t]*<!-- Private Note Section -->.*?</VBox>'
text = re.sub(pattern, '', text, flags=re.DOTALL)

with open('src/main/resources/fxml/note_editor.fxml', 'w') as f:
    f.write(text)
