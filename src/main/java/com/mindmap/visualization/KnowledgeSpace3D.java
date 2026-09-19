package com.mindmap.visualization;

import com.mindmap.model.Connection;
import com.mindmap.model.Note;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.geometry.Point3D;
import javafx.scene.AmbientLight;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.PointLight;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Dedicated 3D Knowledge Space component managing the JavaFX 3D SubScene,
 * world graph, Fibonacci sphere positioning, 3D nodes, 3D cylinder connections,
 * multi-light rendering, and camera navigation.
 */
public class KnowledgeSpace3D {

    private final SubScene subScene;
    private final Group root3D;
    private final Group worldGroup;
    private final Group nodesGroup;
    private final Group edgesGroup;
    private final Group lightsGroup;

    private final PerspectiveCamera camera;
    private final CameraController cameraController;

    private final Map<Integer, Node3D> node3DMap = new HashMap<>();
    private final List<Connection3D> connection3DList = new ArrayList<>();

    private Node3D selectedNode;
    private Connection3D selectedConnection;
    private String activeSearchQuery = null;

    private Consumer<Note> onNodeSelectedCallback;
    private Consumer<Connection> onConnectionSelectedCallback;
    private Runnable onDeselectCallback;

    public KnowledgeSpace3D(double width, double height) {
        this.root3D = new Group();
        this.worldGroup = new Group();
        this.nodesGroup = new Group();
        this.edgesGroup = new Group();
        this.lightsGroup = new Group();

        // 3D scene graph hierarchy
        this.worldGroup.getChildren().addAll(edgesGroup, nodesGroup, lightsGroup);
        this.root3D.getChildren().add(worldGroup);

        // Perspective Camera and controller
        this.camera = new PerspectiveCamera(true);
        this.cameraController = new CameraController(camera);
        this.root3D.getChildren().add(camera);

        // SubScene with 3D depth buffer and antialiasing
        this.subScene = new SubScene(root3D, width, height, true, SceneAntialiasing.BALANCED);
        this.subScene.setCamera(camera);
        this.subScene.setFill(Color.rgb(248, 250, 252, 0.0)); // Transparent to show container background

        setupLighting();
        cameraController.attachTo(subScene);
        setupDeselect();
    }

    private void setupLighting() {
        // Soft ambient base lighting
        AmbientLight ambientLight = new AmbientLight(Color.rgb(140, 150, 165));

        // Key point light (top-right-front) for glossy specular highlights
        PointLight keyLight = new PointLight(Color.rgb(255, 255, 255));
        keyLight.setTranslateX(300);
        keyLight.setTranslateY(-350);
        keyLight.setTranslateZ(-850);

        // Fill point light (bottom-left) for depth perception
        PointLight fillLight = new PointLight(Color.rgb(190, 215, 255));
        fillLight.setTranslateX(-350);
        fillLight.setTranslateY(350);
        fillLight.setTranslateZ(-650);

        lightsGroup.getChildren().addAll(ambientLight, keyLight, fillLight);
    }

    private void setupDeselect() {
        subScene.setOnMouseClicked(event -> {
            if (event.isStillSincePress() && event.getTarget() == subScene) {
                clearSelection();
                if (onDeselectCallback != null) {
                    onDeselectCallback.run();
                }
            }
        });
    }

    /**
     * Binds the SubScene dimensions dynamically to the parent container.
     */
    public void bindSizeTo(ReadOnlyDoubleProperty widthProp, ReadOnlyDoubleProperty heightProp) {
        subScene.widthProperty().bind(widthProp);
        subScene.heightProperty().bind(heightProp);
    }

    /**
     * Rebuilds the 3D scene from Note and Connection data using a deterministic
     * 3D Fibonacci sphere distribution.
     */
    public void updateData(List<Note> notes, List<Connection> connections) {
        nodesGroup.getChildren().clear();
        edgesGroup.getChildren().clear();
        node3DMap.clear();
        connection3DList.clear();
        selectedNode = null;
        selectedConnection = null;

        if (notes == null || notes.isEmpty()) {
            return;
        }

        // Sort deterministically by Note ID
        List<Note> sortedNotes = new ArrayList<>(notes);
        sortedNotes.sort(Comparator.comparingInt(Note::getId));

        int n = sortedNotes.size();
        // Dynamic Fibonacci sphere radius scaling: scales as O(sqrt(n)) to maintain generous spatial separation
        double baseRadius = (n <= 1) ? 0.0 : Math.max(280.0, 50.0 * Math.sqrt(n));

        // Adapt default camera distance so the entire 3D space fits in view cleanly
        cameraController.setAdaptiveDefaultDistance(-Math.max(1000.0, baseRadius * 2.15));

        // Sleek sphere radius for large graphs to prevent visual crowding
        double nodeRadius = (n > 80) ? 14.0 : (n > 40 ? 16.0 : Node3D.DEFAULT_RADIUS);

        // Fibonacci sphere 3D distribution
        for (int i = 0; i < n; i++) {
            Note note = sortedNotes.get(i);
            Point3D position;

            if (n == 1) {
                position = new Point3D(0, 0, 0);
            } else {
                double offset = 2.0 / n;
                double y = ((i * offset) - 1.0) + (offset / 2.0);
                double r = Math.sqrt(Math.max(0.0, 1.0 - y * y));
                double phi = i * Math.PI * (3.0 - Math.sqrt(5.0)); // Golden angle (~2.39996 rad)
                double x = Math.cos(phi) * r;
                double z = Math.sin(phi) * r;

                position = new Point3D(x * baseRadius, y * baseRadius, z * baseRadius);
            }

            Node3D node3D = new Node3D(note, position, nodeRadius);
            node3D.setOnSelectHandler(this::handleNodeClicked);
            node3DMap.put(note.getId(), node3D);
            nodesGroup.getChildren().add(node3D.getSphere());
        }

        // Build 3D connections (cylinders)
        if (connections != null) {
            for (Connection conn : connections) {
                Node3D from = node3DMap.get(conn.getFromNoteId());
                Node3D to = node3DMap.get(conn.getToNoteId());

                if (from != null && to != null) {
                    Connection3D conn3D = new Connection3D(conn, from, to);
                    conn3D.setOnSelectHandler(this::handleConnectionClicked);
                    connection3DList.add(conn3D);
                    edgesGroup.getChildren().add(conn3D.getEdgeGroup());
                }
            }
        }
    }

    private void handleNodeClicked(Node3D clickedNode) {
        selectNode(clickedNode);
        if (onNodeSelectedCallback != null) {
            onNodeSelectedCallback.accept(clickedNode.getNote());
        }
    }

    private void handleConnectionClicked(Connection3D clickedConn) {
        selectConnection(clickedConn);
        if (onConnectionSelectedCallback != null) {
            onConnectionSelectedCallback.accept(clickedConn.getConnection());
        }
    }

    /**
     * Selects a note by entity and highlights its 3D node and connected edges.
     */
    public void selectNote(Note note) {
        if (note == null) {
            clearSelection();
            return;
        }
        Node3D node3D = node3DMap.get(note.getId());
        if (node3D != null) {
            selectNode(node3D);
        }
    }

    private void selectNode(Node3D node3D) {
        clearSelection();
        this.selectedNode = node3D;

        // 1. Collect directly connected neighbor nodes and connecting edges
        Set<Node3D> connectedNeighbors = new HashSet<>();
        List<Connection3D> incidentEdges = new ArrayList<>();

        for (Connection3D edge : connection3DList) {
            boolean isFrom = (edge.getFromNode() == node3D);
            boolean isTo = (edge.getToNode() == node3D);
            if (isFrom || isTo) {
                incidentEdges.add(edge);
                if (isFrom && edge.getToNode() != node3D) {
                    connectedNeighbors.add(edge.getToNode());
                }
                if (isTo && edge.getFromNode() != node3D) {
                    connectedNeighbors.add(edge.getFromNode());
                }
            }
        }

        // 2. Highlight selected node
        node3D.setSelected(true);

        // 3. Highlight directly connected neighbor nodes; dim unrelated nodes
        for (Node3D otherNode : node3DMap.values()) {
            if (otherNode == node3D) {
                continue;
            }
            if (connectedNeighbors.contains(otherNode)) {
                otherNode.setConnectedHighlight(true);
            } else {
                otherNode.setDimmed(true);
            }
        }

        // 4. Highlight incident edges; dim unrelated edges
        for (Connection3D edge : connection3DList) {
            if (incidentEdges.contains(edge)) {
                edge.setSelected(true);
            } else {
                edge.setDimmed(true);
            }
        }
    }

    private void selectConnection(Connection3D conn3D) {
        clearSelection();
        this.selectedConnection = conn3D;

        conn3D.setSelected(true);
        conn3D.getFromNode().setSelected(true);
        conn3D.getToNode().setConnectedHighlight(true);

        // Dim unrelated nodes and edges
        for (Node3D node : node3DMap.values()) {
            if (node != conn3D.getFromNode() && node != conn3D.getToNode()) {
                node.setDimmed(true);
            }
        }
        for (Connection3D edge : connection3DList) {
            if (edge != conn3D) {
                edge.setDimmed(true);
            }
        }
    }

    /**
     * Selects a connection by entity and highlights its 3D cylinder and connected nodes.
     */
    public void selectConnection(Connection conn) {
        if (conn == null) {
            clearSelection();
            return;
        }
        for (Connection3D edge : connection3DList) {
            if (edge.getConnection().getId() == conn.getId()) {
                selectConnection(edge);
                break;
            }
        }
    }

    public void clearSelection() {
        if (selectedNode != null) {
            selectedNode.setSelected(false);
            selectedNode = null;
        }
        for (Node3D node : node3DMap.values()) {
            node.setSelected(false);
            node.setConnectedHighlight(false);
            node.setDimmed(false);
        }
        for (Connection3D edge : connection3DList) {
            edge.setSelected(false);
            edge.setDimmed(false);
        }
        selectedConnection = null;

        // If there is an active search query, re-apply it
        if (activeSearchQuery != null && !activeSearchQuery.trim().isEmpty()) {
            applySearchFilter(activeSearchQuery);
        }
    }

    /**
     * Filters 3D nodes and edges based on a search query.
     */
    public void search(String query) {
        this.activeSearchQuery = query;
        applySearchFilter(query);
    }

    private void applySearchFilter(String query) {
        if (query == null || query.trim().isEmpty()) {
            for (Node3D node : node3DMap.values()) {
                node.setDimmed(false);
            }
            for (Connection3D edge : connection3DList) {
                edge.setDimmed(false);
            }
            return;
        }

        String lower = query.trim().toLowerCase();

        for (Node3D node : node3DMap.values()) {
            Note n = node.getNote();
            boolean matches = (n.getTitle() != null && n.getTitle().toLowerCase().contains(lower))
                    || (n.getSubject() != null && n.getSubject().toLowerCase().contains(lower))
                    || (n.getContent() != null && n.getContent().toLowerCase().contains(lower));

            node.setDimmed(!matches);
        }

        for (Connection3D edge : connection3DList) {
            boolean fromDimmed = edge.getFromNode().isDimmed();
            boolean toDimmed = edge.getToNode().isDimmed();
            edge.setDimmed(fromDimmed || toDimmed);
        }
    }

    /**
     * Smoothly focuses the camera on the currently selected 3D node.
     */
    public void focusSelected() {
        if (selectedNode != null) {
            cameraController.focusOnNode(selectedNode);
        }
    }

    /**
     * Restores camera orbit, pan, and zoom to initial state.
     */
    public void resetView() {
        cameraController.resetView();
    }

    public SubScene getSubScene() {
        return subScene;
    }

    public CameraController getCameraController() {
        return cameraController;
    }

    public Map<Integer, Node3D> getNode3DMap() {
        return node3DMap;
    }

    public List<Connection3D> getConnection3DList() {
        return connection3DList;
    }

    public Node3D getSelectedNode() {
        return selectedNode;
    }

    public void setOnNodeSelectedCallback(Consumer<Note> onNodeSelectedCallback) {
        this.onNodeSelectedCallback = onNodeSelectedCallback;
    }

    public void setOnConnectionSelectedCallback(Consumer<Connection> onConnectionSelectedCallback) {
        this.onConnectionSelectedCallback = onConnectionSelectedCallback;
    }

    public void setOnDeselectCallback(Runnable onDeselectCallback) {
        this.onDeselectCallback = onDeselectCallback;
    }
}
