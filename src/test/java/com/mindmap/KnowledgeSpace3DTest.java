package com.mindmap;

import com.mindmap.database.DatabaseInitializer;
import com.mindmap.model.Connection;
import com.mindmap.model.Note;
import com.mindmap.visualization.CameraController;
import com.mindmap.visualization.Connection3D;
import com.mindmap.visualization.KnowledgeSpace3D;
import com.mindmap.visualization.Node3D;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point3D;
import javafx.scene.Parent;
import javafx.scene.shape.Cylinder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Automated test suite for Phase 6 JavaFX 3D Knowledge Space:
 * tests SubScene, Fibonacci distribution, Cylinder geometry, CameraController,
 * selection synchronization, and search filtering.
 */
public class KnowledgeSpace3DTest {

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
            // Already initialized
            toolkitInitialized = true;
        }
    }

    private Note createMockNote(int id, String title, String subject, String difficulty) {
        Note note = new Note();
        note.setId(id);
        note.setTitle(title);
        note.setContent("Content for " + title);
        note.setSubject(subject);
        note.setDifficulty(difficulty);
        return note;
    }

    @Test
    void test3DKnowledgeSpaceInstantiationAndLighting() throws Exception {
        if (!toolkitInitialized) return;

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<KnowledgeSpace3D> ref = new AtomicReference<>();

        Platform.runLater(() -> {
            try {
                KnowledgeSpace3D space = new KnowledgeSpace3D(800, 600);
                ref.set(space);
            } finally {
                latch.countDown();
            }
        });

        assertTrue(latch.await(3, TimeUnit.SECONDS));
        KnowledgeSpace3D space = ref.get();
        assertNotNull(space, "KnowledgeSpace3D instance should be created");
        assertNotNull(space.getSubScene(), "SubScene must be initialized");
        assertNotNull(space.getCameraController(), "CameraController must be initialized");
        assertEquals(800.0, space.getSubScene().getWidth(), 0.01);
        assertEquals(600.0, space.getSubScene().getHeight(), 0.01);
    }

    @Test
    void testFibonacciSphereDistributionNonColliding() throws Exception {
        if (!toolkitInitialized) return;

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<KnowledgeSpace3D> ref = new AtomicReference<>();

        Platform.runLater(() -> {
            try {
                KnowledgeSpace3D space = new KnowledgeSpace3D(800, 600);
                List<Note> notes = new ArrayList<>();
                for (int i = 1; i <= 8; i++) {
                    notes.add(createMockNote(i, "Note " + i, "Math", "EASY"));
                }
                space.updateData(notes, new ArrayList<>());
                ref.set(space);
            } finally {
                latch.countDown();
            }
        });

        assertTrue(latch.await(3, TimeUnit.SECONDS));
        KnowledgeSpace3D space = ref.get();
        assertEquals(8, space.getNode3DMap().size(), "All 8 notes should have 3D representations");

        // Verify that distinct nodes have non-zero distance (no overlapping at origin)
        List<Node3D> nodes = new ArrayList<>(space.getNode3DMap().values());
        for (int i = 0; i < nodes.size(); i++) {
            for (int j = i + 1; j < nodes.size(); j++) {
                Point3D p1 = nodes.get(i).getPosition();
                Point3D p2 = nodes.get(j).getPosition();
                double dist = p1.distance(p2);
                assertTrue(dist > 50.0, "Nodes " + i + " and " + j + " must not collide. Distance: " + dist);
            }
        }
    }

    @Test
    void testConnection3DCylinderGeometry() throws Exception {
        if (!toolkitInitialized) return;

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<KnowledgeSpace3D> ref = new AtomicReference<>();

        Platform.runLater(() -> {
            try {
                KnowledgeSpace3D space = new KnowledgeSpace3D(800, 600);
                List<Note> notes = List.of(
                        createMockNote(1, "Alpha", "Physics", "EASY"),
                        createMockNote(2, "Beta", "Physics", "MEDIUM")
                );

                Connection conn = new Connection();
                conn.setId(101);
                conn.setFromNoteId(1);
                conn.setToNoteId(2);
                conn.setRelation("causes");

                space.updateData(notes, List.of(conn));
                ref.set(space);
            } finally {
                latch.countDown();
            }
        });

        assertTrue(latch.await(3, TimeUnit.SECONDS));
        KnowledgeSpace3D space = ref.get();
        assertEquals(1, space.getConnection3DList().size());

        Connection3D conn3D = space.getConnection3DList().get(0);
        Point3D p1 = conn3D.getFromNode().getPosition();
        Point3D p2 = conn3D.getToNode().getPosition();
        double expectedDistance = p1.distance(p2);

        Cylinder cylinder = conn3D.getCylinder();
        assertEquals(expectedDistance, cylinder.getHeight(), 0.1, "Cylinder height must match 3D distance between nodes");
        assertTrue(cylinder.getRadius() > 0, "Cylinder radius must be positive");
    }

    @Test
    void testCameraControllerNavigationAndClamping() throws Exception {
        if (!toolkitInitialized) return;

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<CameraController> ref = new AtomicReference<>();

        Platform.runLater(() -> {
            try {
                KnowledgeSpace3D space = new KnowledgeSpace3D(800, 600);
                ref.set(space.getCameraController());
            } finally {
                latch.countDown();
            }
        });

        assertTrue(latch.await(3, TimeUnit.SECONDS));
        CameraController controller = ref.get();
        assertEquals(CameraController.DEFAULT_CAMERA_DISTANCE, controller.getZoomTranslate().getZ(), 0.01);

        // Test zooming in past limit
        Platform.runLater(() -> controller.zoomBy(5000.0));
        Thread.sleep(100);
        assertEquals(CameraController.MAX_CAMERA_DISTANCE, controller.getZoomTranslate().getZ(), 0.01);

        // Test zooming out past limit
        Platform.runLater(() -> controller.zoomBy(-10000.0));
        Thread.sleep(100);
        assertEquals(CameraController.MIN_CAMERA_DISTANCE, controller.getZoomTranslate().getZ(), 0.01);
    }

    @Test
    void testSelectionAndSearchFiltering3D() throws Exception {
        if (!toolkitInitialized) return;

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<KnowledgeSpace3D> ref = new AtomicReference<>();

        Note n1 = createMockNote(10, "Quantum Mechanics", "Physics", "HARD");
        Note n2 = createMockNote(20, "Linear Algebra", "Math", "MEDIUM");
        Note n3 = createMockNote(30, "Classical Mechanics", "Physics", "MEDIUM");

        Connection c1 = new Connection();
        c1.setId(1);
        c1.setFromNoteId(10);
        c1.setToNoteId(20);
        c1.setRelation("prerequisite");

        Platform.runLater(() -> {
            try {
                KnowledgeSpace3D space = new KnowledgeSpace3D(800, 600);
                space.updateData(List.of(n1, n2, n3), List.of(c1));
                ref.set(space);
            } finally {
                latch.countDown();
            }
        });

        assertTrue(latch.await(3, TimeUnit.SECONDS));
        KnowledgeSpace3D space = ref.get();

        // 1. Test Selection & Connected Subgraph Focus
        Platform.runLater(() -> space.selectNote(n1));
        Thread.sleep(100);

        assertNotNull(space.getSelectedNode());
        assertEquals(10, space.getSelectedNode().getNote().getId());
        assertTrue(space.getSelectedNode().isSelected(), "Target node must be selected");
        assertTrue(space.getNode3DMap().get(20).isConnectedHighlight(), "Directly connected neighbor (n2) must have connectedHighlight");
        assertTrue(space.getNode3DMap().get(30).isDimmed(), "Unrelated node (n3) must be dimmed");
        assertTrue(space.getConnection3DList().get(0).isSelected(), "Connected edge must be selected when node is selected");

        // 2. Test Clear Selection
        Platform.runLater(space::clearSelection);
        Thread.sleep(100);
        assertNull(space.getSelectedNode());
        assertFalse(space.getNode3DMap().get(20).isConnectedHighlight());
        assertFalse(space.getNode3DMap().get(30).isDimmed());
        assertFalse(space.getConnection3DList().get(0).isSelected());

        // 3. Test Search Filtering
        Platform.runLater(() -> space.search("Quantum"));
        Thread.sleep(100);
        assertFalse(space.getNode3DMap().get(10).isDimmed(), "Matching node (Quantum) must not be dimmed");
        assertTrue(space.getNode3DMap().get(20).isDimmed(), "Non-matching node (Linear Algebra) must be dimmed");
        assertTrue(space.getNode3DMap().get(30).isDimmed(), "Non-matching node (Classical Mechanics) must be dimmed");
        assertTrue(space.getConnection3DList().get(0).isDimmed(), "Edge connected to dimmed node must be dimmed");

        // 4. Test Clear Search
        Platform.runLater(() -> space.search(""));
        Thread.sleep(100);
        assertFalse(space.getNode3DMap().get(10).isDimmed());
        assertFalse(space.getNode3DMap().get(20).isDimmed());
        assertFalse(space.getNode3DMap().get(30).isDimmed());
        assertFalse(space.getConnection3DList().get(0).isDimmed());
    }

    @Test
    void testLargeScaleFibonacciSeparation147Nodes() throws Exception {
        if (!toolkitInitialized) return;

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<KnowledgeSpace3D> ref = new AtomicReference<>();

        Platform.runLater(() -> {
            try {
                KnowledgeSpace3D space = new KnowledgeSpace3D(800, 600);
                List<Note> notes = new ArrayList<>();
                for (int i = 1; i <= 147; i++) {
                    notes.add(createMockNote(i, "Concept " + i, "Subject" + (i % 5), "MEDIUM"));
                }
                space.updateData(notes, new ArrayList<>());
                ref.set(space);
            } finally {
                latch.countDown();
            }
        });

        assertTrue(latch.await(4, TimeUnit.SECONDS));
        KnowledgeSpace3D space = ref.get();
        assertEquals(147, space.getNode3DMap().size(), "All 147 notes should be distributed in 3D");

        // Verify that for all 147 nodes, the nearest neighbor distance is >= 40 units (no overlaps)
        List<Node3D> nodes = new ArrayList<>(space.getNode3DMap().values());
        for (int i = 0; i < nodes.size(); i++) {
            Point3D pi = nodes.get(i).getPosition();
            double minNeighborDist = Double.MAX_VALUE;
            for (int j = 0; j < nodes.size(); j++) {
                if (i != j) {
                    double dist = pi.distance(nodes.get(j).getPosition());
                    if (dist < minNeighborDist) {
                        minNeighborDist = dist;
                    }
                }
            }
            assertTrue(minNeighborDist >= 40.0,
                    "Node " + i + " nearest neighbor distance (" + minNeighborDist + ") must be >= 40.0 to prevent overlaps");
        }
    }

    @Test
    void testMindMapFxmlIncludes3DComponents() throws Exception {
        if (!toolkitInitialized) return;

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Parent> rootRef = new AtomicReference<>();

        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/mindmap.fxml"));
                Parent root = loader.load();
                rootRef.set(root);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                latch.countDown();
            }
        });

        assertTrue(latch.await(3, TimeUnit.SECONDS));
        Parent root = rootRef.get();
        assertNotNull(root, "mindmap.fxml must load successfully");
        assertNotNull(root.lookup("#btn3DView"), "3D view toggle button must exist");
        assertNotNull(root.lookup("#btn2DView"), "2D view toggle button must exist");
        assertNotNull(root.lookup("#btnFocusSelected"), "Focus button must exist");
        assertNotNull(root.lookup("#space3DContainer"), "3D space container must exist");
    }
}
