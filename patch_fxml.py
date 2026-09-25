import re

with open('src/main/resources/fxml/note_editor.fxml', 'r') as f:
    content = f.read()

new_security_section = """    <!-- Private Note Section -->
    <VBox spacing="8" style="-fx-background-color: #f8fafc; -fx-padding: 12; -fx-background-radius: 6; -fx-border-color: #e2e8f0; -fx-border-radius: 6;">
        <javafx.scene.control.CheckBox fx:id="chkPrivate" text="🔒 Private Note (Requires PIN to view/edit)" style="-fx-font-weight: bold;"/>
        
        <VBox fx:id="boxPinFields" spacing="8" visible="false" managed="false">
            <Label text="Your PIN protects this note's private content." style="-fx-text-fill: #64748b; -fx-font-size: 11px;"/>
            <HBox spacing="12">
                <VBox spacing="4" HBox.hgrow="ALWAYS">
                    <Label text="PIN" styleClass="form-label"/>
                    <javafx.scene.control.PasswordField fx:id="txtPin" promptText="Enter PIN" styleClass="input-field"/>
                </VBox>
                <VBox spacing="4" HBox.hgrow="ALWAYS">
                    <Label text="Confirm PIN" styleClass="form-label"/>
                    <javafx.scene.control.PasswordField fx:id="txtConfirmPin" promptText="Confirm PIN" styleClass="input-field"/>
                </VBox>
            </HBox>
        </VBox>
    </VBox>

    <!-- Error Message -->"""

content = re.sub(r'    <!-- Error Message -->', new_security_section, content)

# Remove the old fields from the Action Buttons HBox
old_hbox = """    <!-- Action Buttons -->
    <HBox spacing="12" alignment="CENTER_LEFT">
        <ToggleButton fx:id="btnFavorite" text="☆ Favorite" onAction="#handleToggleFavorite" styleClass="btn-secondary"/>
        <javafx.scene.control.CheckBox fx:id="chkPrivate" text="🔒 Private Note"/>
        <javafx.scene.control.PasswordField fx:id="txtPin" promptText="Enter PIN (e.g. 1234)" visible="false" managed="false" prefWidth="150" styleClass="input-field"/>
        <Region HBox.hgrow="ALWAYS"/>
        <Button fx:id="btnCancel" text="Cancel" onAction="#handleCancel" cancelButton="true" styleClass="btn-secondary"/>
        <Button fx:id="btnSave" text="Save" onAction="#handleSave" defaultButton="true" styleClass="btn-primary"/>
    </HBox>"""

new_hbox = """    <!-- Action Buttons -->
    <HBox spacing="12" alignment="CENTER_LEFT">
        <ToggleButton fx:id="btnFavorite" text="☆ Favorite" onAction="#handleToggleFavorite" styleClass="btn-secondary"/>
        <Region HBox.hgrow="ALWAYS"/>
        <Button fx:id="btnCancel" text="Cancel" onAction="#handleCancel" cancelButton="true" styleClass="btn-secondary"/>
        <Button fx:id="btnSave" text="Save" onAction="#handleSave" defaultButton="true" styleClass="btn-primary"/>
    </HBox>"""

content = content.replace(old_hbox, new_hbox)

with open('src/main/resources/fxml/note_editor.fxml', 'w') as f:
    f.write(content)
