with open('src/main/resources/fxml/quiz.fxml', 'r') as f:
    text = f.read()

text = text.replace(
    '<StackPane VBox.vgrow="ALWAYS" alignment="TOP_LEFT" minHeight="0">',
    '<StackPane VBox.vgrow="ALWAYS" alignment="TOP_LEFT" minHeight="0" style="-fx-padding: 0 32 24 32;">'
)

with open('src/main/resources/fxml/quiz.fxml', 'w') as f:
    f.write(text)
