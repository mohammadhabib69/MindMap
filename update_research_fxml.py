fxml_content = """<?xml version="1.0" encoding="UTF-8"?>

<?import javafx.geometry.Insets?>
<?import javafx.scene.control.Button?>
<?import javafx.scene.control.Label?>
<?import javafx.scene.control.ListView?>
<?import javafx.scene.control.ProgressIndicator?>
<?import javafx.scene.control.ScrollPane?>
<?import javafx.scene.control.SplitPane?>
<?import javafx.scene.control.TextField?>
<?import javafx.scene.layout.HBox?>
<?import javafx.scene.layout.StackPane?>
<?import javafx.scene.layout.VBox?>
<?import javafx.scene.layout.Priority?>
<?import javafx.scene.layout.Region?>

<VBox xmlns="http://javafx.com/javafx"
      xmlns:fx="http://javafx.com/fxml"
      fx:controller="com.mindmap.controller.ResearchController"
      spacing="16"
      styleClass="screen-container"
      stylesheets="@../css/style.css">

    <padding>
        <Insets top="24" right="24" bottom="24" left="24"/>
    </padding>

    <!-- Header Section -->
    <VBox spacing="4">
        <Label text="Research / External Knowledge" styleClass="view-header"/>
        <Label text="Search Wikipedia to enrich your notes" styleClass="view-subtitle"/>
    </VBox>

    <!-- Search Bar Section -->
    <HBox spacing="10" alignment="CENTER_LEFT">
        <TextField fx:id="txtSearch" promptText="Search Wikipedia..." HBox.hgrow="ALWAYS" onAction="#handleSearch" styleClass="text-field"/>
        <Button fx:id="btnSearch" text="Search" onAction="#handleSearch" styleClass="btn-primary"/>
        <ProgressIndicator fx:id="progressIndicator" visible="false" managed="false" prefWidth="20" prefHeight="20"/>
    </HBox>

    <Label fx:id="lblStatus" text="" styleClass="setting-value" style="-fx-text-fill: #64748b;" />

    <!-- Split Pane for Results and Details -->
    <SplitPane VBox.vgrow="ALWAYS" dividerPositions="0.38" style="-fx-background-color: transparent; -fx-padding: 0;">
        <!-- Left: Results List -->
        <VBox spacing="12" styleClass="card" style="-fx-padding: 16;">
            <Label text="RESULTS" styleClass="inspector-section-header" style="-fx-text-fill: #64748b;"/>
            <StackPane VBox.vgrow="ALWAYS">
                <ListView fx:id="listResults" styleClass="research-list"/>
                <VBox fx:id="boxEmptyResults" alignment="CENTER" spacing="8" styleClass="placeholder-box" visible="false">
                    <Label text="No results found" styleClass="placeholder-heading"/>
                    <Label text="Try another search term." styleClass="placeholder-subtext"/>
                </VBox>
            </StackPane>
        </VBox>

        <!-- Right: Detail View -->
        <VBox spacing="12" styleClass="card" style="-fx-padding: 16;">
            <Label text="SELECTED RESULT" styleClass="inspector-section-header" style="-fx-text-fill: #64748b;"/>
            
            <StackPane VBox.vgrow="ALWAYS">
                <VBox fx:id="boxEmptySelection" alignment="CENTER" spacing="8" styleClass="placeholder-box">
                    <Label text="Select a result" styleClass="placeholder-heading"/>
                    <Label text="Choose a Wikipedia result to preview its summary and create a note." styleClass="placeholder-subtext" wrapText="true" alignment="CENTER"/>
                </VBox>

                <VBox fx:id="boxSelectionDetails" visible="false" spacing="12">
                    <ScrollPane fitToWidth="true" VBox.vgrow="ALWAYS" style="-fx-background-color: transparent; -fx-background: #ffffff; -fx-border-color: transparent; -fx-padding: 0;">
                        <VBox spacing="12" style="-fx-padding: 4 12 4 4;">
                            <Label fx:id="lblTitle" wrapText="true" style="-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #0f172a;"/>
                            <Label fx:id="lblSummary" wrapText="true" style="-fx-font-size: 13px; -fx-text-fill: #334155; -fx-line-spacing: 0.3em;"/>
                            
                            <HBox fx:id="boxSource" spacing="6" alignment="CENTER_LEFT" style="-fx-padding: 12 0 0 0;">
                                <Label text="Source:" style="-fx-font-weight: bold; -fx-text-fill: #64748b;"/>
                                <Label fx:id="lblSource" text="Wikipedia" style="-fx-text-fill: #3b82f6;"/>
                            </HBox>
                        </VBox>
                    </ScrollPane>
        
                    <HBox spacing="12" alignment="CENTER_RIGHT" fx:id="boxActions">
                        <Button fx:id="btnOpenSource" text="Open in Browser" onAction="#handleOpenSource" styleClass="btn-secondary"/>
                        <Button fx:id="btnCreateNote" text="Create Note" onAction="#handleCreateNote" styleClass="btn-primary"/>
                    </HBox>
                </VBox>
            </StackPane>
        </VBox>
    </SplitPane>
</VBox>
"""
with open('src/main/resources/fxml/research.fxml', 'w') as f:
    f.write(fxml_content)
