with open('src/main/resources/fxml/quiz.fxml', 'r') as f:
    text = f.read()

text = text.replace(
    '<HBox spacing="16" VBox.vgrow="ALWAYS" minHeight="0" minWidth="0">\n                <!-- Left side: The List of questions -->\n                <VBox spacing="12" HBox.hgrow="ALWAYS" minHeight="0" minWidth="0">',
    '<!-- The List of questions -->\n            <VBox spacing="12" VBox.vgrow="ALWAYS" minHeight="0" minWidth="0">'
)
text = text.replace(
    '</VBox>\n            </HBox>\n        </VBox>\n\n        <!-- EXAM SETUP VIEW -->',
    '</VBox>\n        </VBox>\n\n        <!-- EXAM SETUP VIEW -->'
)

with open('src/main/resources/fxml/quiz.fxml', 'w') as f:
    f.write(text)
