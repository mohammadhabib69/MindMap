import re

with open('src/main/resources/fxml/notes.fxml', 'r') as f:
    content = f.read()

old_results_header = """    <!-- Results Header & Actions Bar -->
    <HBox spacing="10" alignment="CENTER_LEFT">
        <Label fx:id="lblResultCount" text="Showing 0 notes" styleClass="card-title"/>"""

new_results_header = """    <!-- Results Header & Actions Bar -->
    <HBox spacing="10" alignment="CENTER_LEFT">
        <Label text="View:" styleClass="stat-label"/>
        <ComboBox fx:id="cmbViewFilter" promptText="All Notes" prefWidth="150" styleClass="input-field"/>
        <Label fx:id="lblResultCount" text="Showing 0 notes" styleClass="card-title">
            <padding>
                <Insets left="8"/>
            </padding>
        </Label>"""

content = content.replace(old_results_header, new_results_header)

with open('src/main/resources/fxml/notes.fxml', 'w') as f:
    f.write(content)
