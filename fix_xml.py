with open('src/main/resources/fxml/quiz.fxml', 'r') as f:
    text = f.read()

text = text.replace(
    '''            <HBox spacing="16" VBox.vgrow="ALWAYS" minHeight="0">
                <!-- Left side: The List of questions -->
                <VBox spacing="12" HBox.hgrow="ALWAYS" minHeight="0">
                    <HBox spacing="8" alignment="CENTER_LEFT" minWidth="0">
                        <TextField fx:id="txtSearchBank" promptText="🔍 Search questions..." HBox.hgrow="ALWAYS" style="-fx-padding: 8 12; -fx-background-radius: 6; -fx-border-color: #cbd5e1; -fx-border-radius: 6; -fx-background-color: white;"/>
                        <ComboBox fx:id="comboFilterSubject" promptText="Subject" styleClass="modern-combo"/>
                        <ComboBox fx:id="comboFilterTopic" promptText="Topic" styleClass="modern-combo"/>
                        <ComboBox fx:id="comboFilterDifficulty" promptText="Difficulty" styleClass="modern-combo"/>
                        <ComboBox fx:id="comboFilterType" promptText="Type" styleClass="modern-combo"/>
                        <Button fx:id="btnGenerateBank" text="+ Generate Questions" onAction="#handleGenerateBank" style="-fx-background-color: #10b981; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 16; -fx-background-radius: 6; -fx-cursor: hand;"/>
                    </HBox>
                    <ListView fx:id="listQuestionBank" VBox.vgrow="ALWAYS" minHeight="0" style="-fx-background-color: transparent; -fx-background-insets: 0; -fx-padding: 0;"/>
                </VBox>
        </VBox>''',
    '''            <VBox spacing="12" VBox.vgrow="ALWAYS" minHeight="0">
                <HBox spacing="8" alignment="CENTER_LEFT" minWidth="0">
                    <TextField fx:id="txtSearchBank" promptText="🔍 Search questions..." HBox.hgrow="ALWAYS" style="-fx-padding: 8 12; -fx-background-radius: 6; -fx-border-color: #cbd5e1; -fx-border-radius: 6; -fx-background-color: white;"/>
                    <ComboBox fx:id="comboFilterSubject" promptText="Subject" styleClass="modern-combo"/>
                    <ComboBox fx:id="comboFilterTopic" promptText="Topic" styleClass="modern-combo"/>
                    <ComboBox fx:id="comboFilterDifficulty" promptText="Difficulty" styleClass="modern-combo"/>
                    <ComboBox fx:id="comboFilterType" promptText="Type" styleClass="modern-combo"/>
                    <Button fx:id="btnGenerateBank" text="+ Generate Questions" onAction="#handleGenerateBank" style="-fx-background-color: #10b981; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 16; -fx-background-radius: 6; -fx-cursor: hand;"/>
                </HBox>
                <ListView fx:id="listQuestionBank" VBox.vgrow="ALWAYS" minHeight="0" style="-fx-background-color: transparent; -fx-background-insets: 0; -fx-padding: 0;"/>
            </VBox>
        </VBox>'''
)

with open('src/main/resources/fxml/quiz.fxml', 'w') as f:
    f.write(text)
