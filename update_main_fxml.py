fxml_content = """<?xml version="1.0" encoding="UTF-8"?>

<?import javafx.geometry.Insets?>
<?import javafx.scene.control.Button?>
<?import javafx.scene.control.Label?>
<?import javafx.scene.control.SplitPane?>
<?import javafx.scene.layout.Region?>
<?import javafx.scene.layout.StackPane?>
<?import javafx.scene.layout.VBox?>

<SplitPane xmlns="http://javafx.com/javafx"
            xmlns:fx="http://javafx.com/fxml"
            fx:controller="com.mindmap.controller.MainController"
            prefWidth="1100" prefHeight="720"
            styleClass="main-root" dividerPositions="0.2">

    <!-- Left Sidebar -->
    <VBox spacing="10" styleClass="sidebar" prefWidth="230" minWidth="200" maxWidth="280" SplitPane.resizableWithParent="false">
        <padding>
            <Insets top="24" right="16" bottom="24" left="16"/>
        </padding>

        <!-- Brand Header -->
        <VBox spacing="4" styleClass="sidebar-brand">
            <padding>
                <Insets bottom="24" left="8" right="8"/>
            </padding>
            <Label text="MindMap" styleClass="brand-title"/>
            <Label text="Personal Knowledge Base" styleClass="brand-subtitle"/>
        </VBox>

        <!-- Navigation Buttons -->
        <VBox spacing="4" styleClass="nav-group">
            <Label text="OVERVIEW" styleClass="nav-section-label"/>
            <Button fx:id="btnDashboard" text="  Dashboard" maxWidth="Infinity" alignment="BASELINE_LEFT"
                    onAction="#showDashboard" styleClass="nav-button"/>

            <Region prefHeight="8"/>
            <Label text="KNOWLEDGE" styleClass="nav-section-label"/>
            <Button fx:id="btnNotes" text="  Notes" maxWidth="Infinity" alignment="BASELINE_LEFT"
                    onAction="#showNotes" styleClass="nav-button"/>
            <Button fx:id="btnMindMap" text="  Mind Map" maxWidth="Infinity" alignment="BASELINE_LEFT"
                    onAction="#showMindMap" styleClass="nav-button"/>

            <Region prefHeight="8"/>
            <Label text="STUDY" styleClass="nav-section-label"/>
            <Button fx:id="btnRevision" text="  Revision" maxWidth="Infinity" alignment="BASELINE_LEFT"
                    onAction="#showRevision" styleClass="nav-button"/>
            <Button fx:id="btnTimeline" text="  Timeline" maxWidth="Infinity" alignment="BASELINE_LEFT"
                    onAction="#showTimeline" styleClass="nav-button"/>

            <Region prefHeight="8"/>
            <Label text="RESEARCH" styleClass="nav-section-label"/>
            <Button fx:id="btnResearch" text="  Research" maxWidth="Infinity" alignment="BASELINE_LEFT"
                    onAction="#showResearch" styleClass="nav-button"/>
        </VBox>

        <!-- Flexible Spacer pushing bottom buttons down -->
        <Region VBox.vgrow="ALWAYS"/>

        <!-- Utility / System Buttons -->
        <VBox spacing="4" styleClass="sidebar-footer">
            <Label text="SYSTEM" styleClass="nav-section-label"/>
            <Button fx:id="btnSettings" text="  Settings" maxWidth="Infinity" alignment="BASELINE_LEFT"
                    onAction="#showSettings" styleClass="nav-button"/>
            <Button fx:id="btnAbout" text="  About" maxWidth="Infinity" alignment="BASELINE_LEFT"
                    onAction="#handleAbout" styleClass="nav-button"/>
            <Button fx:id="btnExit" text="  Exit" maxWidth="Infinity" alignment="BASELINE_LEFT"
                    onAction="#handleExit" styleClass="nav-button"/>
        </VBox>
    </VBox>

    <!-- Dynamic Main Content Area -->
    <StackPane fx:id="contentArea" minWidth="600" minHeight="0" styleClass="content-area">
        <!-- Dynamically populated with selected FXML screen -->
    </StackPane>

</SplitPane>
"""
with open('src/main/resources/fxml/main.fxml', 'w') as f:
    f.write(fxml_content)
