package com.mindmap;

import com.mindmap.controller.DashboardController;
import com.mindmap.database.DatabaseInitializer;
import com.mindmap.model.DashboardStats;
import com.mindmap.service.DashboardService;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Phase 10.1: Dashboard Performance and Responsive Layout.
 * Verifies:
 * - Dashboard FXML loads correctly with responsive FlowPane containers
 * - Charts are optimized (animated=false, labelsVisible=false on PieChart)
 * - ScrollPane configured to eliminate horizontal scrollbar thrashing (fitToWidth=true, hbarPolicy=NEVER)
 * - Cards adapt widths correctly across wide and narrow breakpoints
 * - Zero database queries occur during window resizing / layout adaptation
 * - Clean scene placement at 720x480 minimum application dimensions
 */
public class DashboardResponsiveLayoutTest {

    private static boolean toolkitInitialized = false;

    @BeforeAll
    static void setUpAll() throws SQLException {
        DatabaseInitializer.initialize();

        CountDownLatch latch = new CountDownLatch(1);
        try {
            Platform.startup(latch::countDown);
            toolkitInitialized = true;
            latch.await(3, TimeUnit.SECONDS);
        } catch (IllegalStateException e) {
            toolkitInitialized = true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Test
    void testDashboardFxmlAndControlsLoadSuccessfully() throws Exception {
        if (!toolkitInitialized) return;

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dashboard.fxml"));
                Parent root = loader.load();
                assertNotNull(root, "Root node should load");
                assertTrue(root instanceof ScrollPane, "Root should be ScrollPane");

                DashboardController controller = loader.getController();
                assertNotNull(controller, "Controller should be instantiated");
                assertNotNull(controller.getScrollDashboard(), "scrollDashboard should be bound");
                assertNotNull(controller.getContentContainer(), "contentContainer should be bound");
                assertNotNull(controller.getPaneMetrics(), "paneMetrics should be bound");
                assertNotNull(controller.getPaneMiddleSection(), "paneMiddleSection should be bound");
                assertNotNull(controller.getPaneLowerSection(), "paneLowerSection should be bound");
                assertNotNull(controller.getChartSubjects(), "chartSubjects should be bound");
                assertNotNull(controller.getChartDifficulty(), "chartDifficulty should be bound");
                assertNotNull(controller.getCardKnowledgeDistribution(), "cardKnowledgeDistribution should be bound");
                assertNotNull(controller.getCardRevisionOverview(), "cardRevisionOverview should be bound");
            } catch (Exception e) {
                fail("Failed to load dashboard FXML: " + e.getMessage());
            } finally {
                latch.countDown();
            }
        });
        assertTrue(latch.await(5, TimeUnit.SECONDS), "FXML loading timed out");
    }

    @Test
    void testChartsOptimizedForPerformance() throws Exception {
        if (!toolkitInitialized) return;

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dashboard.fxml"));
                loader.load();
                DashboardController controller = loader.getController();

                BarChart<String, Number> barChart = controller.getChartSubjects();
                assertNotNull(barChart);
                assertFalse(barChart.getAnimated(), "BarChart animation should be disabled to prevent resize lag");
                assertFalse(barChart.getXAxis().getAnimated(), "CategoryAxis animation should be disabled");
                assertFalse(barChart.getYAxis().getAnimated(), "NumberAxis animation should be disabled");

                PieChart pieChart = controller.getChartDifficulty();
                assertNotNull(pieChart);
                assertFalse(pieChart.getAnimated(), "PieChart animation should be disabled");
                assertFalse(pieChart.getLabelsVisible(),
                        "PieChart slice labels should be false to eliminate collision detection loops on resize");
                assertTrue(pieChart.isLegendVisible(), "PieChart legend should remain visible to show data");
            } catch (Exception e) {
                fail("Chart optimization check failed: " + e.getMessage());
            } finally {
                latch.countDown();
            }
        });
        assertTrue(latch.await(5, TimeUnit.SECONDS), "Chart check timed out");
    }

    @Test
    void testScrollPaneConfiguredWithoutHorizontalJitter() throws Exception {
        if (!toolkitInitialized) return;

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dashboard.fxml"));
                loader.load();
                DashboardController controller = loader.getController();

                ScrollPane scroll = controller.getScrollDashboard();
                assertNotNull(scroll);
                assertTrue(scroll.isFitToWidth(), "ScrollPane must fit to width");
                assertEquals(ScrollPane.ScrollBarPolicy.NEVER, scroll.getHbarPolicy(),
                        "Horizontal scrollbar policy must be NEVER to prevent layout oscillation");
                assertEquals(ScrollPane.ScrollBarPolicy.AS_NEEDED, scroll.getVbarPolicy(),
                        "Vertical scrollbar policy should be AS_NEEDED");
            } catch (Exception e) {
                fail("ScrollPane check failed: " + e.getMessage());
            } finally {
                latch.countDown();
            }
        });
        assertTrue(latch.await(5, TimeUnit.SECONDS), "ScrollPane check timed out");
    }

    @Test
    void testResponsiveLayoutAdaptsToWideDimensions() throws Exception {
        if (!toolkitInitialized) return;

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dashboard.fxml"));
                loader.load();
                DashboardController controller = loader.getController();

                // Apply wide container width (1000px)
                controller.applyResponsiveWidths(1000.0);

                VBox cardLeft = controller.getCardKnowledgeDistribution();
                VBox cardRight = controller.getCardRevisionOverview();

                assertNotNull(cardLeft);
                assertNotNull(cardRight);

                double wLeft = cardLeft.getPrefWidth();
                double wRight = cardRight.getPrefWidth();

                assertTrue(wLeft > 0, "Left card should have positive width");
                assertTrue(wRight > 0, "Right card should have positive width");
                // In wide mode (usableWidth >= 800), cards should sit side-by-side:
                // usableWidth = 1000 - 48 = 952. Left gets 54% of (952-14) = ~506px.
                assertTrue(wLeft < 952, "Left card width should not be full width in side-by-side mode");
                assertTrue(wRight < 952, "Right card width should not be full width in side-by-side mode");
                assertTrue(wLeft + wRight <= 952, "Combined cards width plus gap should fit within usable container width");
            } catch (Exception e) {
                fail("Wide responsive adaptation failed: " + e.getMessage());
            } finally {
                latch.countDown();
            }
        });
        assertTrue(latch.await(5, TimeUnit.SECONDS), "Wide layout check timed out");
    }

    @Test
    void testResponsiveLayoutAdaptsToNarrowDimensions() throws Exception {
        if (!toolkitInitialized) return;

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dashboard.fxml"));
                loader.load();
                DashboardController controller = loader.getController();

                // Apply narrow container width (500px, usable width = 452px)
                controller.applyResponsiveWidths(500.0);

                VBox cardLeft = controller.getCardKnowledgeDistribution();
                VBox cardRight = controller.getCardRevisionOverview();

                assertNotNull(cardLeft);
                assertNotNull(cardRight);

                double wLeft = cardLeft.getPrefWidth();
                double wRight = cardRight.getPrefWidth();

                // In narrow mode (usableWidth < 800), cards should stack and each take full usable width
                assertEquals(452.0, wLeft, 1.0, "In narrow stacked mode, left card should take full usable width");
                assertEquals(452.0, wRight, 1.0, "In narrow stacked mode, right card should take full usable width");
            } catch (Exception e) {
                fail("Narrow responsive adaptation failed: " + e.getMessage());
            } finally {
                latch.countDown();
            }
        });
        assertTrue(latch.await(5, TimeUnit.SECONDS), "Narrow layout check timed out");
    }

    @Test
    void testNoDatabaseQueriesDuringResize() throws Exception {
        if (!toolkitInitialized) return;

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                AtomicInteger queryCount = new AtomicInteger(0);

                // Custom DashboardService that counts getDashboardStats calls
                DashboardService mockService = new DashboardService() {
                    @Override
                    public DashboardStats getDashboardStats() {
                        queryCount.incrementAndGet();
                        return super.getDashboardStats();
                    }
                };

                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dashboard.fxml"));
                loader.load();
                DashboardController controller = loader.getController();
                controller.setDashboardService(mockService);

                // Baseline query count after setup
                int initialCount = queryCount.get();

                // Simulate resizing 25 times across various window sizes
                for (int w = 400; w <= 1400; w += 40) {
                    controller.applyResponsiveWidths((double) w);
                }

                // Assert zero additional database queries occurred during resize
                assertEquals(initialCount, queryCount.get(),
                        "applyResponsiveWidths must NEVER query the database during resizing");
            } catch (Exception e) {
                fail("Database query check on resize failed: " + e.getMessage());
            } finally {
                latch.countDown();
            }
        });
        assertTrue(latch.await(5, TimeUnit.SECONDS), "Query count check timed out");
    }

    @Test
    void testDashboardScenePlacementAtSmallDimensions() throws Exception {
        if (!toolkitInitialized) return;

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dashboard.fxml"));
                Parent root = loader.load();

                // Place in 720x480 scene (MIN_WIDTH x MIN_HEIGHT)
                Scene scene = new Scene(root, 720, 480);
                assertNotNull(scene);
                assertEquals(720, scene.getWidth());
                assertEquals(480, scene.getHeight());
            } catch (Exception e) {
                fail("Placing dashboard in small scene failed: " + e.getMessage());
            } finally {
                latch.countDown();
            }
        });
        assertTrue(latch.await(5, TimeUnit.SECONDS), "Scene placement check timed out");
    }
}
