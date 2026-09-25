import re

with open('src/main/resources/fxml/dashboard.fxml', 'r') as f:
    content = f.read()

new_card = """            <!-- Total Connections -->
            <VBox fx:id="cardTotalConnections" spacing="4" styleClass="dashboard-metric-card" minWidth="130" prefWidth="155">
                <Label text="CONNECTIONS" styleClass="inspector-section-header" style="-fx-text-fill: #8b5cf6;"/>
                <HBox alignment="BASELINE_LEFT" spacing="8">
                    <Label fx:id="lblTotalConnections" text="0" style="-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #0f172a;"/>
                    <Label text="links created" style="-fx-font-size: 11px; -fx-text-fill: #64748b;"/>
                </HBox>
            </VBox>
            
            <!-- Private Notes -->
            <VBox fx:id="cardPrivateNotes" spacing="4" styleClass="dashboard-metric-card" minWidth="130" prefWidth="155">
                <Label text="PRIVATE NOTES" styleClass="inspector-section-header" style="-fx-text-fill: #ef4444;"/>
                <HBox alignment="BASELINE_LEFT" spacing="8">
                    <Label fx:id="lblPrivateNotes" text="0" style="-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #0f172a;"/>
                    <Label text="secured notes" style="-fx-font-size: 11px; -fx-text-fill: #64748b;"/>
                </HBox>
            </VBox>"""

# Using a simpler regex to insert after cardTotalConnections. Wait, I'll just replace the whole block up to <Label text="links created"...
old_block = """            <!-- Total Connections -->
            <VBox fx:id="cardTotalConnections" spacing="4" styleClass="dashboard-metric-card" minWidth="130" prefWidth="155">
                <Label text="CONNECTIONS" styleClass="inspector-section-header" style="-fx-text-fill: #8b5cf6;"/>
                <HBox alignment="BASELINE_LEFT" spacing="8">
                    <Label fx:id="lblTotalConnections" text="0" style="-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #0f172a;"/>"""

new_block = """            <!-- Total Connections -->
            <VBox fx:id="cardTotalConnections" spacing="4" styleClass="dashboard-metric-card" minWidth="130" prefWidth="155">
                <Label text="CONNECTIONS" styleClass="inspector-section-header" style="-fx-text-fill: #8b5cf6;"/>
                <HBox alignment="BASELINE_LEFT" spacing="8">
                    <Label fx:id="lblTotalConnections" text="0" style="-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #0f172a;"/>"""
# I'll just append it after the VBox for cardTotalConnections.

with open('src/main/resources/fxml/dashboard.fxml', 'r') as f:
    text = f.read()

find = '</HBox>\n            </VBox>'
insert = '\n            <!-- Private Notes -->\n            <VBox fx:id="cardPrivateNotes" spacing="4" styleClass="dashboard-metric-card" minWidth="130" prefWidth="155">\n                <Label text="PRIVATE NOTES" styleClass="inspector-section-header" style="-fx-text-fill: #ef4444;"/>\n                <HBox alignment="BASELINE_LEFT" spacing="8">\n                    <Label fx:id="lblPrivateNotes" text="0" style="-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #0f172a;"/>\n                    <Label text="secured notes" style="-fx-font-size: 11px; -fx-text-fill: #64748b;"/>\n                </HBox>\n            </VBox>'
text = text.replace(find, '</HBox>\n            </VBox>' + insert, 2)
# Reverting the replace since 2 might match multiple. Better to use regex on cardTotalConnections.

import re
content = re.sub(r'(<VBox fx:id="cardTotalConnections".*?</VBox>)', r'\1' + insert, text, flags=re.DOTALL)

with open('src/main/resources/fxml/dashboard.fxml', 'w') as f:
    f.write(content)
