import re

with open('src/main/resources/fxml/dashboard.fxml', 'r') as f:
    text = f.read()

pattern = r'[ \t]*<!-- Private Notes -->\s*<VBox fx:id="cardPrivateNotes".*?</VBox>'
text = re.sub(pattern, '', text, flags=re.DOTALL)

with open('src/main/resources/fxml/dashboard.fxml', 'w') as f:
    f.write(text)
