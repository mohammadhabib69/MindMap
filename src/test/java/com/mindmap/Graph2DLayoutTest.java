package com.mindmap;

import com.mindmap.model.Connection;
import com.mindmap.model.Note;
import com.mindmap.visualization.Graph2DLayout;
import javafx.geometry.Point2D;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class Graph2DLayoutTest {

    private Note makeNote(int id, String title, String subject) {
        Note note = new Note();
        note.setId(id);
        note.setTitle(title);
        note.setSubject(subject);
        return note;
    }

    @Test
    void testEmptyGraph() {
        Graph2DLayout.LayoutResult result = Graph2DLayout.calculateLayout(new ArrayList<>(), new ArrayList<>(), 800, 600);
        assertNotNull(result);
        assertTrue(result.getPositions().isEmpty());
        assertEquals(1.0, result.getRecommendedZoom());
    }

    @Test
    void testSingleNode() {
        List<Note> notes = List.of(makeNote(1, "Single Node", "Computer Science"));
        Graph2DLayout.LayoutResult result = Graph2DLayout.calculateLayout(notes, List.of(), 800, 600);

        assertEquals(1, result.getPositions().size());
        Point2D p = result.getPosition(1);
        assertNotNull(p);
        // Center position minus half node width/height
        assertEquals(400 - (Graph2DLayout.NODE_WIDTH / 2.0), p.getX(), 0.01);
        assertEquals(300 - (Graph2DLayout.NODE_HEIGHT / 2.0), p.getY(), 0.01);
    }

    @Test
    void testTwoAndThreeNodes() {
        List<Note> notes2 = List.of(
                makeNote(1, "Node 1", "Math"),
                makeNote(2, "Node 2", "Math")
        );
        Graph2DLayout.LayoutResult result2 = Graph2DLayout.calculateLayout(notes2, List.of(), 800, 600);
        assertEquals(2, result2.getPositions().size());
        assertNotEquals(result2.getPosition(1), result2.getPosition(2));

        List<Note> notes3 = List.of(
                makeNote(1, "Node 1", "Science"),
                makeNote(2, "Node 2", "Science"),
                makeNote(3, "Node 3", "Science")
        );
        Graph2DLayout.LayoutResult result3 = Graph2DLayout.calculateLayout(notes3, List.of(), 800, 600);
        assertEquals(3, result3.getPositions().size());
    }

    @Test
    void testConnectedCoreClusteringVsIsolatedRings() {
        // Create 4 connected notes and 6 isolated notes
        List<Note> notes = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            notes.add(makeNote(i, "Note " + i, i <= 4 ? "Core" : "Peripheral"));
        }

        List<Connection> connections = List.of(
                new Connection(1, 2, "links"),
                new Connection(2, 3, "links"),
                new Connection(3, 4, "links"),
                new Connection(4, 1, "links")
        );

        Graph2DLayout.LayoutResult result = Graph2DLayout.calculateLayout(notes, connections, 800, 600);
        assertEquals(10, result.getPositions().size());

        Point2D center = new Point2D(400, 300);

        // Check that connected notes (1-4) are clustered closer to center than isolated notes (5-10)
        double maxConnectedDist = 0;
        for (int i = 1; i <= 4; i++) {
            Point2D pos = result.getPosition(i);
            double dist = Math.hypot(pos.getX() + Graph2DLayout.NODE_WIDTH / 2.0 - center.getX(),
                    pos.getY() + Graph2DLayout.NODE_HEIGHT / 2.0 - center.getY());
            if (dist > maxConnectedDist) {
                maxConnectedDist = dist;
            }
        }

        double minIsolatedDist = Double.MAX_VALUE;
        for (int i = 5; i <= 10; i++) {
            Point2D pos = result.getPosition(i);
            double dist = Math.hypot(pos.getX() + Graph2DLayout.NODE_WIDTH / 2.0 - center.getX(),
                    pos.getY() + Graph2DLayout.NODE_HEIGHT / 2.0 - center.getY());
            if (dist < minIsolatedDist) {
                minIsolatedDist = dist;
            }
        }

        assertTrue(minIsolatedDist >= maxConnectedDist,
                "Isolated notes should be arranged in outer rings outside the connected core cluster");
    }

    @Test
    void test179NotesPerformanceAndDeterminism() {
        // Simulates the user's database size (~179 notes)
        List<Note> notes = new ArrayList<>();
        for (int i = 1; i <= 179; i++) {
            notes.add(makeNote(i, "Knowledge Concept #" + i, "Subject " + (i % 6)));
        }

        List<Connection> connections = List.of(
                new Connection(1, 2, "explains"),
                new Connection(2, 3, "relates to")
        );

        long start = System.nanoTime();
        Graph2DLayout.LayoutResult run1 = Graph2DLayout.calculateLayout(notes, connections, 800, 600);
        long durationMs = (System.nanoTime() - start) / 1_000_000;

        // Verify layout performance: 179 notes layout must complete swiftly (<150ms, typically <20ms)
        assertTrue(durationMs < 150, "Layout calculation took " + durationMs + "ms, expected < 150ms");

        // Verify determinism: re-running with same inputs produces identical coordinates
        Graph2DLayout.LayoutResult run2 = Graph2DLayout.calculateLayout(notes, connections, 800, 600);
        for (Note n : notes) {
            Point2D p1 = run1.getPosition(n.getId());
            Point2D p2 = run2.getPosition(n.getId());
            assertNotNull(p1);
            assertNotNull(p2);
            assertEquals(p1.getX(), p2.getX(), 0.0001, "Position X must be deterministic");
            assertEquals(p1.getY(), p2.getY(), 0.0001, "Position Y must be deterministic");
        }

        // Verify bounds and zoom recommendation
        assertTrue(run1.getRecommendedZoom() > 0.15 && run1.getRecommendedZoom() <= 1.0,
                "Recommended zoom should be a reasonable value, got: " + run1.getRecommendedZoom());
        assertTrue(run1.getMaxX() > run1.getMinX());
        assertTrue(run1.getMaxY() > run1.getMinY());

        // Verify pairwise center separation: no two nodes should have identical coordinates
        Map<Integer, Point2D> positions = run1.getPositions();
        List<Point2D> pts = new ArrayList<>(positions.values());
        for (int i = 0; i < pts.size(); i++) {
            Point2D a = pts.get(i);
            for (int j = i + 1; j < pts.size(); j++) {
                Point2D b = pts.get(j);
                double dist = Math.hypot(a.getX() - b.getX(), a.getY() - b.getY());
                assertTrue(dist > 25.0, "Nodes are placed too close: distance was " + dist);
            }
        }
    }
}
