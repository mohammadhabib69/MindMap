package com.mindmap;

import com.mindmap.database.DatabaseInitializer;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.sql.SQLException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies Knowledge Graph Inspector UI layout constraints, content wrapping,
 * non-truncating action buttons, and responsive sizing.
 */
public class InspectorLayoutTest {

    private static boolean toolkitInitialized = false;

    @BeforeAll
    static void setUpAll() throws SQLException, InterruptedException {
        DatabaseInitializer.initialize();

        CountDownLatch latch = new CountDownLatch(1);
        try {
            Platform.startup(latch::countDown);
            toolkitInitialized = true;
            latch.await(3, TimeUnit.SECONDS);
        } catch (IllegalStateException e) {
            toolkitInitialized = true;
        }
    }

    @Test
    void testInspectorLayoutAndConstraints() throws Exception {
        if (!toolkitInitialized) return;

        URL url = getClass().getResource("/fxml/mindmap.fxml");
        assertNotNull(url, "mindmap.fxml should exist");

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(url);
                Parent root = loader.load();
                assertNotNull(root);

                // 1. Verify Inspector Panel sizing
                VBox inspectorPanel = (VBox) root.lookup("#inspectorPanel");
                assertNotNull(inspectorPanel, "inspectorPanel should exist");

                // Must size to content rather than filling vertical space (maxHeight == USE_PREF_SIZE)
                assertEquals(Region.USE_PREF_SIZE, inspectorPanel.getMaxHeight(),
                        "inspectorPanel must have maxHeight set to USE_PREF_SIZE (-1) to wrap content");
                assertTrue(inspectorPanel.getPrefWidth() >= 220 && inspectorPanel.getPrefWidth() <= 280,
                        "inspectorPanel prefWidth should be in a compact responsive range (220-280px)");
                assertTrue(inspectorPanel.getMinWidth() >= 180 && inspectorPanel.getMinWidth() <= 220,
                        "inspectorPanel minWidth should be in a compact range");

                // 2. Verify Note Title multi-line wrapping
                Label lblSelectedTitle = (Label) root.lookup("#lblSelectedTitle");
                assertNotNull(lblSelectedTitle, "lblSelectedTitle should exist");
                assertTrue(lblSelectedTitle.isWrapText(), "lblSelectedTitle must have wrapText=true");
                assertEquals(0, lblSelectedTitle.getMinWidth(), 0.01,
                        "lblSelectedTitle must have minWidth=0 to prevent forcing panel expansion");

                // 3. Verify Action Buttons in FlowPane
                FlowPane paneSelectedActions = (FlowPane) root.lookup("#paneSelectedActions");
                assertNotNull(paneSelectedActions, "paneSelectedActions must be a FlowPane for responsive wrapping");

                Button btnView = (Button) root.lookup("#btnViewSelected");
                Button btnEdit = (Button) root.lookup("#btnEditSelected");
                Button btnLink = (Button) root.lookup("#btnConnectFromSelected");

                assertNotNull(btnView);
                assertNotNull(btnEdit);
                assertNotNull(btnLink);

                assertEquals("View Note", btnView.getText(), "btnViewSelected label should be readable and not truncated");
                assertEquals("Edit Note", btnEdit.getText(), "btnEditSelected label should be readable and not truncated");
                assertEquals("+ Link", btnLink.getText(), "btnConnectFromSelected label should be '+ Link'");

                // 4. Verify Focus Node button
                Button btnFocus = (Button) root.lookup("#btnInspectorFocus");
                assertNotNull(btnFocus);
                assertTrue(btnFocus.getText().contains("Focus Node"), "Focus button should say 'Focus Node'");

                // 5. Verify Connections ScrollPane bounded height
                ScrollPane scrollConnections = (ScrollPane) root.lookup("#scrollConnections");
                assertNotNull(scrollConnections, "scrollConnections should exist");
                assertTrue(scrollConnections.getMaxHeight() <= 200,
                        "scrollConnections maxHeight must be bounded (<= 200px) to prevent vertical stretching");
                assertNull(VBox.getVgrow(scrollConnections),
                        "scrollConnections must not have VBox.vgrow=ALWAYS");

                // 6. Verify boxOverview does not have vertical grow
                VBox boxOverview = (VBox) root.lookup("#boxOverview");
                assertNotNull(boxOverview);
                assertNull(VBox.getVgrow(boxOverview),
                        "boxOverview must not have VBox.vgrow=ALWAYS to prevent unnecessary vertical expansion");

                // 7. Verify boxSelectedNote does not have vertical grow
                VBox boxSelectedNote = (VBox) root.lookup("#boxSelectedNote");
                assertNotNull(boxSelectedNote);
                assertNull(VBox.getVgrow(boxSelectedNote),
                        "boxSelectedNote must not have VBox.vgrow=ALWAYS to prevent unnecessary vertical expansion");

            } catch (Exception e) {
                fail("Exception checking inspector layout: " + e.getMessage());
            } finally {
                latch.countDown();
            }
        });

        assertTrue(latch.await(4, TimeUnit.SECONDS), "FXML inspection timed out");
    }
}
