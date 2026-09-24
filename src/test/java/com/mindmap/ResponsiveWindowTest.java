package com.mindmap;

import com.mindmap.database.DatabaseInitializer;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.net.URL;
import java.sql.SQLException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies Phase 5.5 responsive window sizing constraints and FXML loadability.
 */
public class ResponsiveWindowTest {

    private static boolean toolkitInitialized = false;

    @BeforeAll
    static void setUpAll() throws SQLException, InterruptedException {
        DatabaseInitializer.initialize();

        // Initialize JavaFX toolkit once for test execution if needed
        CountDownLatch latch = new CountDownLatch(1);
        try {
            Platform.startup(latch::countDown);
            toolkitInitialized = true;
            latch.await(3, TimeUnit.SECONDS);
        } catch (IllegalStateException e) {
            // Toolkit already initialized
            toolkitInitialized = true;
        }
    }

    @Test
    void testMainMinimumDimensionsUpdated() throws NoSuchFieldException, IllegalAccessException {
        Field minWidthField = Main.class.getDeclaredField("MIN_WIDTH");
        minWidthField.setAccessible(true);
        int minWidth = (int) minWidthField.get(null);

        Field minHeightField = Main.class.getDeclaredField("MIN_HEIGHT");
        minHeightField.setAccessible(true);
        int minHeight = (int) minHeightField.get(null);

        assertEquals(720, minWidth, "Minimum window width should be set to 720px for responsive compact displays");
        assertEquals(480, minHeight, "Minimum window height should be set to 480px for responsive compact displays");
    }

    @Test
    void testScreensLoadableAtSmallDimensions() throws Exception {
        if (!toolkitInitialized) return;

        String[] fxmlPaths = {
                "/fxml/main.fxml",
                "/fxml/dashboard.fxml",
                "/fxml/notes.fxml",
                "/fxml/mindmap.fxml",
                
                "/fxml/revision.fxml",
                "/fxml/timeline.fxml",
                "/fxml/settings.fxml"
        };

        for (String path : fxmlPaths) {
            URL url = getClass().getResource(path);
            assertNotNull(url, "FXML should exist: " + path);

            CountDownLatch latch = new CountDownLatch(1);
            Platform.runLater(() -> {
                try {
                    Parent root = FXMLLoader.load(url);
                    assertNotNull(root, "Root node should load for: " + path);
                    // Verify that placing in a 720x480 scene does not fail
                    Scene scene = new Scene(root, 720, 480);
                    assertNotNull(scene);
                } catch (Exception e) {
                    fail("Failed to load and size FXML: " + path + " with error: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
            assertTrue(latch.await(3, TimeUnit.SECONDS), "Loading FXML timed out for: " + path);
        }
    }
}
