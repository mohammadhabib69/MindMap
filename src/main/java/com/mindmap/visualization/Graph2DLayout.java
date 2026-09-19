package com.mindmap.visualization;

import com.mindmap.model.Connection;
import com.mindmap.model.Note;
import javafx.geometry.Point2D;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * High-performance, deterministic graph-aware layout engine for the 2D Knowledge Graph.
 * <p>
 * Separates connected nodes into an organically relaxed central core network, while
 * distributing isolated notes into clean, concentric outer planetary rings without overlaps.
 */
public final class Graph2DLayout {

    public static final double NODE_WIDTH = 125.0;
    public static final double NODE_HEIGHT = 38.0;

    private Graph2DLayout() {
        // Prevent instantiation
    }

    /**
     * Result of a 2D graph layout calculation containing node positions and viewport metrics.
     */
    public static class LayoutResult {
        private final Map<Integer, Point2D> positions;
        private final double minX;
        private final double minY;
        private final double maxX;
        private final double maxY;
        private final double recommendedZoom;
        private final Point2D center;

        public LayoutResult(Map<Integer, Point2D> positions, double minX, double minY, double maxX, double maxY, double recommendedZoom, Point2D center) {
            this.positions = positions;
            this.minX = minX;
            this.minY = minY;
            this.maxX = maxX;
            this.maxY = maxY;
            this.recommendedZoom = recommendedZoom;
            this.center = center;
        }

        public Map<Integer, Point2D> getPositions() {
            return positions;
        }

        public Point2D getPosition(int noteId) {
            return positions.get(noteId);
        }

        public double getMinX() {
            return minX;
        }

        public double getMinY() {
            return minY;
        }

        public double getMaxX() {
            return maxX;
        }

        public double getMaxY() {
            return maxY;
        }

        public double getRecommendedZoom() {
            return recommendedZoom;
        }

        public Point2D getCenter() {
            return center;
        }
    }

    /**
     * Calculates deterministic non-overlapping 2D coordinates for all notes.
     *
     * @param notes The notes to position.
     * @param connections The connections between notes.
     * @param canvasWidth Available canvas width.
     * @param canvasHeight Available canvas height.
     * @return LayoutResult containing top-left layout coordinates for each note and graph bounds.
     */
    public static LayoutResult calculateLayout(List<Note> notes, List<Connection> connections, double canvasWidth, double canvasHeight) {
        if (notes == null || notes.isEmpty()) {
            return new LayoutResult(Collections.emptyMap(), 0, 0, 0, 0, 1.0, new Point2D(0, 0));
        }

        double cw = (canvasWidth > 100) ? canvasWidth : 800.0;
        double ch = (canvasHeight > 100) ? canvasHeight : 600.0;
        double centerX = cw / 2.0;
        double centerY = ch / 2.0;

        int totalCount = notes.size();
        Map<Integer, Point2D> centerPositions = new HashMap<>();

        // Handle very small graphs directly
        if (totalCount == 1) {
            centerPositions.put(notes.get(0).getId(), new Point2D(centerX, centerY));
            return buildResult(centerPositions, cw, ch);
        } else if (totalCount == 2) {
            centerPositions.put(notes.get(0).getId(), new Point2D(centerX - 90.0, centerY));
            centerPositions.put(notes.get(1).getId(), new Point2D(centerX + 90.0, centerY));
            return buildResult(centerPositions, cw, ch);
        } else if (totalCount == 3) {
            double r = 110.0;
            for (int i = 0; i < 3; i++) {
                double a = (2 * Math.PI * i) / 3.0 - Math.PI / 2.0;
                centerPositions.put(notes.get(i).getId(), new Point2D(centerX + r * Math.cos(a), centerY + r * Math.sin(a)));
            }
            return buildResult(centerPositions, cw, ch);
        }

        // Build adjacency map
        Map<Integer, Set<Integer>> adjacency = new HashMap<>();
        for (Note n : notes) {
            adjacency.put(n.getId(), new HashSet<>());
        }
        if (connections != null) {
            for (Connection c : connections) {
                if (adjacency.containsKey(c.getFromNoteId()) && adjacency.containsKey(c.getToNoteId())) {
                    adjacency.get(c.getFromNoteId()).add(c.getToNoteId());
                    adjacency.get(c.getToNoteId()).add(c.getFromNoteId());
                }
            }
        }

        // Partition into connected notes vs isolated notes
        List<Note> connectedNotes = new ArrayList<>();
        List<Note> isolatedNotes = new ArrayList<>();

        for (Note n : notes) {
            if (adjacency.get(n.getId()).isEmpty()) {
                isolatedNotes.add(n);
            } else {
                connectedNotes.add(n);
            }
        }

        // 1. Arrange connected notes in the central region
        double coreRadius = 0.0;
        if (!connectedNotes.isEmpty()) {
            int k = connectedNotes.size();

            // Sort by degree descending so highly connected hub nodes anchor near center
            connectedNotes.sort((a, b) -> Integer.compare(adjacency.get(b.getId()).size(), adjacency.get(a.getId()).size()));

            double baseRadius = Math.max(90.0, 32.0 * Math.sqrt(k));
            Map<Integer, double[]> coords = new HashMap<>();

            // Initial deterministic placement
            for (int i = 0; i < k; i++) {
                Note n = connectedNotes.get(i);
                double angle = (2 * Math.PI * i) / k;
                double r = (i == 0 && adjacency.get(n.getId()).size() > 2) ? 0.0 : baseRadius * Math.sqrt((double) (i + 1) / k);
                coords.put(n.getId(), new double[]{centerX + r * Math.cos(angle), centerY + r * Math.sin(angle)});
            }

            // Force relaxation pass (35 iterations, deterministic)
            double optSpring = Math.max(150.0, 24.0 * Math.sqrt(k));
            for (int step = 0; step < 35; step++) {
                double temp = 1.0 - (step / 35.0);
                Map<Integer, double[]> forces = new HashMap<>();
                for (Note n : connectedNotes) {
                    forces.put(n.getId(), new double[]{0.0, 0.0});
                }

                // Repulsion between connected nodes
                for (int i = 0; i < k; i++) {
                    int idA = connectedNotes.get(i).getId();
                    double[] posA = coords.get(idA);
                    for (int j = i + 1; j < k; j++) {
                        int idB = connectedNotes.get(j).getId();
                        double[] posB = coords.get(idB);

                        double dx = posB[0] - posA[0];
                        double dy = posB[1] - posA[1];
                        double dist = Math.hypot(dx, dy);
                        if (dist < 1.0) {
                            dist = 1.0;
                            dx = 1.0;
                            dy = 0.0;
                        }

                        if (dist < optSpring * 2.2) {
                            double rep = (optSpring * optSpring) / (dist * dist) * 0.18;
                            rep = Math.min(rep, 35.0);
                            double fx = (dx / dist) * rep;
                            double fy = (dy / dist) * rep;

                            forces.get(idA)[0] -= fx;
                            forces.get(idA)[1] -= fy;
                            forces.get(idB)[0] += fx;
                            forces.get(idB)[1] += fy;
                        }
                    }
                }

                // Attraction along connection edges
                if (connections != null) {
                    for (Connection conn : connections) {
                        int from = conn.getFromNoteId();
                        int to = conn.getToNoteId();
                        if (coords.containsKey(from) && coords.containsKey(to)) {
                            double[] posA = coords.get(from);
                            double[] posB = coords.get(to);

                            double dx = posB[0] - posA[0];
                            double dy = posB[1] - posA[1];
                            double dist = Math.hypot(dx, dy);

                            if (dist > optSpring) {
                                double att = ((dist - optSpring) / optSpring) * 0.28;
                                att = Math.min(att, 30.0);
                                double fx = (dx / dist) * att;
                                double fy = (dy / dist) * att;

                                forces.get(from)[0] += fx;
                                forces.get(from)[1] += fy;
                                forces.get(to)[0] -= fx;
                                forces.get(to)[1] -= fy;
                            }
                        }
                    }
                }

                // Weak center gravity & step application
                double maxStep = Math.max(1.0, 25.0 * temp);
                for (Note n : connectedNotes) {
                    int id = n.getId();
                    double[] pos = coords.get(id);
                    double[] f = forces.get(id);

                    // Pull lightly toward center
                    f[0] += (centerX - pos[0]) * 0.03;
                    f[1] += (centerY - pos[1]) * 0.03;

                    double fLen = Math.hypot(f[0], f[1]);
                    if (fLen > maxStep) {
                        f[0] = (f[0] / fLen) * maxStep;
                        f[1] = (f[1] / fLen) * maxStep;
                    }

                    pos[0] += f[0];
                    pos[1] += f[1];
                }
            }

            // Save connected coordinates and calculate core bounding radius
            for (Note n : connectedNotes) {
                double[] p = coords.get(n.getId());
                centerPositions.put(n.getId(), new Point2D(p[0], p[1]));
                double rFromCenter = Math.hypot(p[0] - centerX, p[1] - centerY);
                if (rFromCenter > coreRadius) {
                    coreRadius = rFromCenter;
                }
            }
            coreRadius = Math.max(coreRadius, 140.0);
        }

        // 2. Arrange isolated notes in outer planetary rings
        if (!isolatedNotes.isEmpty()) {
            int m = isolatedNotes.size();

            // Sort deterministically: Subject -> Title -> ID
            isolatedNotes.sort(Comparator
                    .comparing((Note n) -> n.getSubject() != null ? n.getSubject().trim().toLowerCase() : "")
                    .thenComparing(n -> n.getTitle() != null ? n.getTitle().trim().toLowerCase() : "")
                    .thenComparingInt(Note::getId));

            double ringRadius = (connectedNotes.isEmpty()) ? Math.max(120.0, 32.0 * Math.sqrt(m)) : (coreRadius + 130.0);
            double deltaR = 75.0; // radial spacing between rings
            int noteIndex = 0;
            int ringNumber = 0;

            while (noteIndex < m) {
                int remaining = m - noteIndex;
                double circumference = 2 * Math.PI * ringRadius;
                // 155px slot width ensures generous lateral clearance for 125px cards
                int ringCapacity = Math.max(6, (int) Math.floor(circumference / 155.0));
                int countOnRing = Math.min(remaining, ringCapacity);

                // Golden ratio offset to interleave successive rings aesthetically
                double angleOffset = (ringNumber * 0.6180339887) * (2 * Math.PI);

                for (int i = 0; i < countOnRing; i++) {
                    double angle = angleOffset + (2 * Math.PI * i) / countOnRing;
                    double x = centerX + ringRadius * Math.cos(angle);
                    double y = centerY + ringRadius * Math.sin(angle);
                    centerPositions.put(isolatedNotes.get(noteIndex++).getId(), new Point2D(x, y));
                }

                ringRadius += deltaR;
                ringNumber++;
            }
        }

        return buildResult(centerPositions, cw, ch);
    }

    private static LayoutResult buildResult(Map<Integer, Point2D> centerPositions, double canvasWidth, double canvasHeight) {
        Map<Integer, Point2D> topLeftPositions = new HashMap<>();

        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE;
        double maxY = -Double.MAX_VALUE;

        for (Map.Entry<Integer, Point2D> entry : centerPositions.entrySet()) {
            Point2D cp = entry.getValue();
            double tlX = cp.getX() - (NODE_WIDTH / 2.0);
            double tlY = cp.getY() - (NODE_HEIGHT / 2.0);

            topLeftPositions.put(entry.getKey(), new Point2D(tlX, tlY));

            if (tlX < minX) minX = tlX;
            if (tlY < minY) minY = tlY;
            if (tlX + NODE_WIDTH > maxX) maxX = tlX + NODE_WIDTH;
            if (tlY + NODE_HEIGHT > maxY) maxY = tlY + NODE_HEIGHT;
        }

        double graphW = (maxX - minX) + 80.0;
        double graphH = (maxY - minY) + 80.0;

        double fitZoom = 1.0;
        if (graphW > canvasWidth || graphH > canvasHeight) {
            double zX = canvasWidth / graphW;
            double zY = canvasHeight / graphH;
            fitZoom = Math.min(zX, zY);
        }
        double recommendedZoom = Math.max(0.20, Math.min(1.0, fitZoom));

        Point2D center = new Point2D((minX + maxX) / 2.0, (minY + maxY) / 2.0);
        return new LayoutResult(topLeftPositions, minX, minY, maxX, maxY, recommendedZoom, center);
    }
}
