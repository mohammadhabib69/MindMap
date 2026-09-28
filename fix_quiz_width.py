with open('src/main/resources/fxml/quiz.fxml', 'r') as f:
    text = f.read()

text = text.replace(
    '<StackPane VBox.vgrow="ALWAYS" alignment="TOP_LEFT" minHeight="0" style="-fx-padding: 0 32 24 32;">',
    '<StackPane VBox.vgrow="ALWAYS" alignment="TOP_LEFT" minHeight="0" minWidth="0">'
)

text = text.replace(
    '<HBox spacing="16">',
    '<HBox spacing="16" minWidth="0">'
)

text = text.replace(
    '<VBox spacing="4" HBox.hgrow="ALWAYS"',
    '<VBox spacing="4" HBox.hgrow="ALWAYS" minWidth="0"'
)

text = text.replace(
    '<HBox spacing="8" alignment="CENTER_LEFT">',
    '<HBox spacing="8" alignment="CENTER_LEFT" minWidth="0">'
)

with open('src/main/resources/fxml/quiz.fxml', 'w') as f:
    f.write(text)
