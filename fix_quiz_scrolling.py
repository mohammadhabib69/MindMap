import re

with open('src/main/resources/fxml/quiz.fxml', 'r') as f:
    text = f.read()

# Add minHeight="0" to the flexible containers to ensure they can shrink
text = text.replace('<StackPane VBox.vgrow="ALWAYS" alignment="TOP_LEFT">', '<StackPane VBox.vgrow="ALWAYS" alignment="TOP_LEFT" minHeight="0">')
text = text.replace('<VBox fx:id="boxQuestionBank" spacing="16" VBox.vgrow="ALWAYS">', '<VBox fx:id="boxQuestionBank" spacing="16" VBox.vgrow="ALWAYS" minHeight="0">')
text = text.replace('<HBox spacing="16" VBox.vgrow="ALWAYS">', '<HBox spacing="16" VBox.vgrow="ALWAYS" minHeight="0">')
text = text.replace('<VBox spacing="12" HBox.hgrow="ALWAYS">', '<VBox spacing="12" HBox.hgrow="ALWAYS" minHeight="0">')
text = text.replace('<ListView fx:id="listQuestionBank" VBox.vgrow="ALWAYS" style="-fx-background-color: transparent; -fx-background-insets: 0; -fx-padding: 0;"/>', '<ListView fx:id="listQuestionBank" VBox.vgrow="ALWAYS" minHeight="0" style="-fx-background-color: transparent; -fx-background-insets: 0; -fx-padding: 0;"/>')

with open('src/main/resources/fxml/quiz.fxml', 'w') as f:
    f.write(text)
