with open('src/main/resources/fxml/research.fxml', 'r') as f:
    text = f.read()

old_stack = """            <StackPane VBox.vgrow="ALWAYS">
                <VBox fx:id="boxEmptySelection" alignment="CENTER" spacing="8" styleClass="placeholder-box">"""
new_stack = """            <StackPane VBox.vgrow="ALWAYS">
                <VBox fx:id="boxEmptySelection" alignment="CENTER" spacing="8" styleClass="placeholder-box">"""
                
loading_box = """
                <VBox fx:id="boxLoadingSelection" alignment="CENTER" spacing="12" styleClass="placeholder-box" visible="false" managed="false">
                    <ProgressIndicator prefWidth="32" prefHeight="32"/>
                    <Label text="Loading selected result..." styleClass="placeholder-heading"/>
                </VBox>"""

if "boxLoadingSelection" not in text:
    text = text.replace(old_stack, old_stack + loading_box)

with open('src/main/resources/fxml/research.fxml', 'w') as f:
    f.write(text)
