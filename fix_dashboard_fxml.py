import re

with open('src/main/resources/fxml/dashboard.fxml', 'r') as f:
    text = f.read()

# Replace the entire paneMetrics section with a clean version
clean_pane = """        <!-- Primary Metrics Cards -->
        <FlowPane fx:id="paneMetrics" hgap="12" vgap="12" alignment="TOP_LEFT">
            <!-- Total Notes -->
            <VBox fx:id="cardTotalNotes" spacing="4" styleClass="dashboard-metric-card" minWidth="130" prefWidth="155">
                <Label text="TOTAL NOTES" styleClass="inspector-section-header" style="-fx-text-fill: #2563eb;"/>
                <HBox alignment="BASELINE_LEFT" spacing="8">
                    <Label fx:id="lblTotalNotes" text="0" style="-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #0f172a;"/>
                    <Label text="notes saved" style="-fx-font-size: 11px; -fx-text-fill: #64748b;"/>
                </HBox>
            </VBox>

            <!-- Total Connections -->
            <VBox fx:id="cardTotalConnections" spacing="4" styleClass="dashboard-metric-card" minWidth="130" prefWidth="155">
                <Label text="CONNECTIONS" styleClass="inspector-section-header" style="-fx-text-fill: #8b5cf6;"/>
                <HBox alignment="BASELINE_LEFT" spacing="8">
                    <Label fx:id="lblTotalConnections" text="0" style="-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #0f172a;"/>
                    <Label text="relationships" style="-fx-font-size: 11px; -fx-text-fill: #64748b;"/>
                </HBox>
            </VBox>

            <!-- Private Notes -->
            <VBox fx:id="cardPrivateNotes" spacing="4" styleClass="dashboard-metric-card" minWidth="130" prefWidth="155">
                <Label text="PRIVATE NOTES" styleClass="inspector-section-header" style="-fx-text-fill: #ef4444;"/>
                <HBox alignment="BASELINE_LEFT" spacing="8">
                    <Label fx:id="lblPrivateNotes" text="0" style="-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #0f172a;"/>
                    <Label text="secured notes" style="-fx-font-size: 11px; -fx-text-fill: #64748b;"/>
                </HBox>
            </VBox>

            <!-- Reviews Due Today -->
            <VBox fx:id="cardReviewsDue" spacing="4" styleClass="dashboard-metric-card" minWidth="130" prefWidth="155">
                <Label text="DUE TODAY" styleClass="inspector-section-header" style="-fx-text-fill: #dc2626;"/>
                <HBox alignment="BASELINE_LEFT" spacing="8">
                    <Label fx:id="lblReviewsDue" text="0" style="-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #0f172a;"/>
                    <Label text="need review" style="-fx-font-size: 11px; -fx-text-fill: #64748b;"/>
                </HBox>
            </VBox>

            <!-- Upcoming Reviews -->
            <VBox fx:id="cardUpcoming" spacing="4" styleClass="dashboard-metric-card" minWidth="130" prefWidth="155">
                <Label text="UPCOMING" styleClass="inspector-section-header" style="-fx-text-fill: #0ea5e9;"/>
                <HBox alignment="BASELINE_LEFT" spacing="8">
                    <Label fx:id="lblUpcoming" text="0" style="-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #0f172a;"/>
                    <Label text="scheduled" style="-fx-font-size: 11px; -fx-text-fill: #64748b;"/>
                </HBox>
            </VBox>

            <!-- Total Tags -->
            <VBox fx:id="cardTotalTags" spacing="4" styleClass="dashboard-metric-card" minWidth="130" prefWidth="155">
                <Label text="TOTAL TAGS" styleClass="inspector-section-header" style="-fx-text-fill: #10b981;"/>
                <HBox alignment="BASELINE_LEFT" spacing="8">
                    <Label fx:id="lblTotalTags" text="0" style="-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #0f172a;"/>
                    <Label text="index tags" style="-fx-font-size: 11px; -fx-text-fill: #64748b;"/>
                </HBox>
            </VBox>
        </FlowPane>"""

# Using regex to replace everything between <FlowPane fx:id="paneMetrics" and </FlowPane>
text = re.sub(r'        <!-- Primary Metrics Cards -->\s*<FlowPane fx:id="paneMetrics".*?</FlowPane>', clean_pane, text, flags=re.DOTALL)

with open('src/main/resources/fxml/dashboard.fxml', 'w') as f:
    f.write(text)
