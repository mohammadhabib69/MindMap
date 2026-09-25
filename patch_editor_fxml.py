import re

with open('src/main/resources/fxml/note_editor.fxml', 'r') as f:
    content = f.read()

old_hbox = """    <!-- Action Buttons -->
    <HBox spacing="12" alignment="CENTER_LEFT">
        <ToggleButton fx:id="btnFavorite" text="☆ Favorite" onAction="#handleToggleFavorite" styleClass="btn-secondary"/>
        <Region HBox.hgrow="ALWAYS"/>
        <Button fx:id="btnCancel" text="Cancel" onAction="#handleCancel" cancelButton="true" styleClass="btn-secondary"/>
        <Button fx:id="btnSave" text="Save" onAction="#handleSave" defaultButton="true" styleClass="btn-primary"/>
    </HBox>"""

new_hbox = """    <!-- Action Buttons -->
    <HBox spacing="12" alignment="CENTER_LEFT">
        <ToggleButton fx:id="btnFavorite" text="☆ Favorite" onAction="#handleToggleFavorite" styleClass="btn-secondary"/>
        <javafx.scene.control.CheckBox fx:id="chkPrivate" text="🔒 Private Note"/>
        <javafx.scene.control.PasswordField fx:id="txtPin" promptText="Enter PIN (e.g. 1234)" visible="false" managed="false" prefWidth="150" styleClass="input-field"/>
        <Region HBox.hgrow="ALWAYS"/>
        <Button fx:id="btnCancel" text="Cancel" onAction="#handleCancel" cancelButton="true" styleClass="btn-secondary"/>
        <Button fx:id="btnSave" text="Save" onAction="#handleSave" defaultButton="true" styleClass="btn-primary"/>
    </HBox>"""

content = content.replace(old_hbox, new_hbox)

with open('src/main/resources/fxml/note_editor.fxml', 'w') as f:
    f.write(content)
