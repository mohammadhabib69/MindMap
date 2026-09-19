package com.mindmap.controller;

import com.mindmap.model.Connection;
import com.mindmap.model.Note;
import com.mindmap.service.ConnectionService;
import com.mindmap.service.NoteService;
import com.mindmap.util.AnimationUtil;
import com.mindmap.visualization.KnowledgeSpace3D;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for the Mind Map (Knowledge Graph) 2D screen.
 * Handles note nodes, directional connection edges, node dragging, search filtering,
 * automatic layout, inspector details panel, and connection creation/deletion.
 */
public class MindMapController {

    private static final Logger LOGGER = Logger.getLogger(MindMapController.class.getName());

    // Toolbar & Controls
    @FXML private Button btnAddConnection;
    @FXML private Button btnAutoLayout;
    @FXML private Button btnRefresh;
    @FXML private Button btnZoomOut;
    @FXML private Button btnZoomIn;
    @FXML private Button btnResetView;
    @FXML private Label lblZoomLevel;
    @FXML private TextField txtSearch;
    @FXML private Button btnClearSearch;
    @FXML private Label lblGraphStats;

    // View Switching & 3D
    @FXML private ToggleGroup viewToggleGroup;
    @FXML private ToggleButton btn3DView;
    @FXML private ToggleButton btn2DView;
    @FXML private Button btnFocusSelected;
    @FXML private Button btnInspectorFocus;
    @FXML private StackPane space3DContainer;

    // 3D Knowledge Space
    private KnowledgeSpace3D knowledgeSpace3D;
    private boolean is3DMode = true;

    // Canvas
    @FXML private StackPane canvasContainer;
    @FXML private Pane graphCanvasPane;
    @FXML private VBox bannerNoConnections;
    @FXML private VBox emptyStateNoNotes;
    @FXML private Button btnCreateFirstNote;

    // Inspector Panel
    @FXML private VBox inspectorPanel;
    @FXML private VBox boxOverview;
    @FXML private Label lblOverviewTotalNotes;
    @FXML private Label lblOverviewTotalConnections;

    // Selected Note Inspector
    @FXML private VBox boxSelectedNote;
    @FXML private Label lblSelectedTitle;
    @FXML private Label lblSelectedSubject;
    @FXML private Label lblSelectedDifficulty;
    @FXML private FlowPane flowSelectedTags;
    @FXML private Button btnViewSelected;
    @FXML private Button btnEditSelected;
    @FXML private Button btnConnectFromSelected;
    @FXML private Label lblConnectionsHeader;
    @FXML private Label lblConnectionCount;
    @FXML private VBox boxConnectionsList;

    // Services
    private final NoteService noteService = new NoteService();
    private final ConnectionService connectionService = new ConnectionService();

    // Visual Graph Hierarchy
    private final Group graphGroup = new Group();
    private final Group edgesGroup = new Group();
    private final Group nodesGroup = new Group();

    // Graph Data Structures
    private final Map<Integer, Note> notesMap = new HashMap<>();
    private final Map<Integer, NodeCardView> nodeViews = new HashMap<>();
    private final List<EdgeView> edgeViews = new ArrayList<>();
    private final List<Connection> currentConnections = new ArrayList<>();

    // Interaction State
    private Note selectedNote;
    private Connection selectedConnection;
    private double zoomFactor = 1.0;
    private double panX = 0;
    private double panY = 0;
    private double dragAnchorX = 0;
    private double dragAnchorY = 0;

    @FXML
    public void initialize() {
        setup3DKnowledgeSpace();
        setupViewToggles();
        setupGraphHierarchy();
        setupCanvasPanningAndZooming();
        setupSearchFiltering();
        setupButtonAnimations();

        loadGraphData();
    }

    private void setup3DKnowledgeSpace() {
        if (space3DContainer != null) {
            knowledgeSpace3D = new KnowledgeSpace3D(750, 550);
            knowledgeSpace3D.bindSizeTo(space3DContainer.widthProperty(), space3DContainer.heightProperty());
            space3DContainer.getChildren().add(knowledgeSpace3D.getSubScene());

            knowledgeSpace3D.setOnNodeSelectedCallback(this::selectNode);
            knowledgeSpace3D.setOnConnectionSelectedCallback(this::selectConnection);
            knowledgeSpace3D.setOnDeselectCallback(this::handleDeselect);
        }
    }

    private void setupViewToggles() {
        if (viewToggleGroup != null) {
            viewToggleGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal == null && oldVal != null) {
                    oldVal.setSelected(true);
                }
            });
        }
        handleSwitchTo3D();
    }

    private void setupGraphHierarchy() {
        graphGroup.getChildren().addAll(edgesGroup, nodesGroup);
        graphCanvasPane.getChildren().add(graphGroup);

        // Canvas clip to container bounds
        javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle();
        clip.widthProperty().bind(graphCanvasPane.widthProperty());
        clip.heightProperty().bind(graphCanvasPane.heightProperty());
        graphCanvasPane.setClip(clip);
    }

    private void setupButtonAnimations() {
        AnimationUtil.addButtonHoverEffect(btnAddConnection);
        AnimationUtil.addButtonHoverEffect(btnAutoLayout);
        AnimationUtil.addButtonHoverEffect(btnRefresh);
        AnimationUtil.addButtonHoverEffect(btnZoomOut);
        AnimationUtil.addButtonHoverEffect(btnZoomIn);
        AnimationUtil.addButtonHoverEffect(btnResetView);
        AnimationUtil.addButtonHoverEffect(btnClearSearch);
        if (btnFocusSelected != null) AnimationUtil.addButtonHoverEffect(btnFocusSelected);
        if (btnInspectorFocus != null) AnimationUtil.addButtonHoverEffect(btnInspectorFocus);
        if (btnCreateFirstNote != null) {
            AnimationUtil.addButtonHoverEffect(btnCreateFirstNote);
        }
        if (btnViewSelected != null) AnimationUtil.addButtonHoverEffect(btnViewSelected);
        if (btnEditSelected != null) AnimationUtil.addButtonHoverEffect(btnEditSelected);
        if (btnConnectFromSelected != null) AnimationUtil.addButtonHoverEffect(btnConnectFromSelected);
    }

    private void setupCanvasPanningAndZooming() {
        // Pan viewport on background drag
        graphCanvasPane.setOnMousePressed(event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.getTarget() == graphCanvasPane) {
                dragAnchorX = event.getSceneX() - panX;
                dragAnchorY = event.getSceneY() - panY;
                handleDeselect();
            }
        });

        graphCanvasPane.setOnMouseDragged(event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.getTarget() == graphCanvasPane) {
                panX = event.getSceneX() - dragAnchorX;
                panY = event.getSceneY() - dragAnchorY;
                applyTransform();
            }
        });

        // Zoom via mouse wheel
        graphCanvasPane.setOnScroll(event -> {
            double delta = event.getDeltaY();
            if (delta > 0) {
                zoomFactor = Math.min(2.5, zoomFactor * 1.08);
            } else if (delta < 0) {
                zoomFactor = Math.max(0.4, zoomFactor / 1.08);
            }
            applyTransform();
            event.consume();
        });
    }

    private void setupSearchFiltering() {
        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> applySearchFilter(newVal));
    }

    private void applyTransform() {
        graphGroup.setScaleX(zoomFactor);
        graphGroup.setScaleY(zoomFactor);
        graphGroup.setTranslateX(panX);
        graphGroup.setTranslateY(panY);

        if (lblZoomLevel != null) {
            lblZoomLevel.setText((int) Math.round(zoomFactor * 100) + "%");
        }
    }

    // ==================================================
    // Graph Data Loading & Rendering
    // ==================================================

    public void loadGraphData() {
        // Remember previously selected note ID if any
        Integer prevSelectedId = (selectedNote != null) ? selectedNote.getId() : null;

        nodesGroup.getChildren().clear();
        edgesGroup.getChildren().clear();
        nodeViews.clear();
        edgeViews.clear();
        notesMap.clear();
        currentConnections.clear();

        List<Note> notes = noteService.getAllNotesWithTags();
        List<Connection> connections = connectionService.getAllConnections();

        for (Note note : notes) {
            notesMap.put(note.getId(), note);
        }
        currentConnections.addAll(connections);

        updateStats(notes.size(), connections.size());
        updateEmptyStates(notes.size(), connections.size());

        if (notes.isEmpty()) {
            if (knowledgeSpace3D != null) {
                knowledgeSpace3D.updateData(notes, connections);
            }
            handleDeselect();
            return;
        }

        // Build visual note cards
        for (Note note : notes) {
            NodeCardView nodeCard = new NodeCardView(note);
            nodeViews.put(note.getId(), nodeCard);
            nodesGroup.getChildren().add(nodeCard);
        }

        // Build visual connection edges
        for (Connection conn : connections) {
            NodeCardView fromView = nodeViews.get(conn.getFromNoteId());
            NodeCardView toView = nodeViews.get(conn.getToNoteId());
            if (fromView != null && toView != null) {
                EdgeView edgeView = new EdgeView(conn, fromView, toView);
                edgeViews.add(edgeView);
                edgesGroup.getChildren().addAll(edgeView.line, edgeView.arrow, edgeView.badge);
            }
        }

        // Pass data to 3D Knowledge Space
        if (knowledgeSpace3D != null) {
            knowledgeSpace3D.updateData(notes, connections);
        }

        // Initial layout if needed
        Platform.runLater(this::runAutoLayout);

        // Restore selection if possible
        if (prevSelectedId != null && notesMap.containsKey(prevSelectedId)) {
            selectNode(notesMap.get(prevSelectedId));
        } else {
            handleDeselect();
        }
    }

    private void updateStats(int noteCount, int connectionCount) {
        if (lblGraphStats != null) {
            lblGraphStats.setText(noteCount + " notes • " + connectionCount + " links");
        }
        if (lblOverviewTotalNotes != null) {
            lblOverviewTotalNotes.setText(String.valueOf(noteCount));
        }
        if (lblOverviewTotalConnections != null) {
            lblOverviewTotalConnections.setText(String.valueOf(connectionCount));
        }
    }

    private void updateEmptyStates(int noteCount, int connectionCount) {
        if (emptyStateNoNotes != null) {
            emptyStateNoNotes.setVisible(noteCount == 0);
            emptyStateNoNotes.setManaged(noteCount == 0);
        }
        if (bannerNoConnections != null) {
            bannerNoConnections.setVisible(noteCount > 1 && connectionCount == 0);
        }
    }

    // ==================================================
    // Layout Algorithm
    // ==================================================

    @FXML
    private void handleAutoLayout() {
        runAutoLayout();
    }

    private void runAutoLayout() {
        int count = nodeViews.size();
        if (count == 0) return;

        double canvasW = graphCanvasPane.getWidth();
        double canvasH = graphCanvasPane.getHeight();
        if (canvasW <= 0) canvasW = 750;
        if (canvasH <= 0) canvasH = 550;

        double centerX = canvasW / 2.0;
        double centerY = canvasH / 2.0;

        List<NodeCardView> cards = new ArrayList<>(nodeViews.values());

        if (count == 1) {
            NodeCardView single = cards.get(0);
            single.setLayoutX(centerX - 80);
            single.setLayoutY(centerY - 35);
            updateAllEdgeGeometries();
            return;
        }

        // Initial geometric arrangement
        if (count <= 10) {
            double radius = Math.min(260.0, Math.min(canvasW, canvasH) * 0.35);
            for (int i = 0; i < count; i++) {
                double angle = (2 * Math.PI * i) / count;
                double x = centerX + radius * Math.cos(angle) - 80;
                double y = centerY + radius * Math.sin(angle) - 35;
                cards.get(i).setLayoutX(x);
                cards.get(i).setLayoutY(y);
            }
        } else {
            // Multi-ring concentric circle arrangement
            int innerCount = Math.min(6, count / 2);
            int outerCount = count - innerCount;
            double r1 = 150.0;
            double r2 = 280.0;

            for (int i = 0; i < innerCount; i++) {
                double angle = (2 * Math.PI * i) / innerCount;
                cards.get(i).setLayoutX(centerX + r1 * Math.cos(angle) - 80);
                cards.get(i).setLayoutY(centerY + r1 * Math.sin(angle) - 35);
            }
            for (int i = 0; i < outerCount; i++) {
                double angle = (2 * Math.PI * i) / outerCount;
                cards.get(innerCount + i).setLayoutX(centerX + r2 * Math.cos(angle) - 80);
                cards.get(innerCount + i).setLayoutY(centerY + r2 * Math.sin(angle) - 35);
            }
        }

        // Lightweight force relaxation pass (20 iterations)
        for (int step = 0; step < 20; step++) {
            // Node-to-node repulsion
            for (int i = 0; i < count; i++) {
                NodeCardView u = cards.get(i);
                for (int j = i + 1; j < count; j++) {
                    NodeCardView v = cards.get(j);
                    double dx = (v.getLayoutX() + 80) - (u.getLayoutX() + 80);
                    double dy = (v.getLayoutY() + 35) - (u.getLayoutY() + 35);
                    double dist = Math.hypot(dx, dy);
                    if (dist < 220 && dist > 1.0) {
                        double repForce = (220 - dist) * 0.08;
                        double fx = (dx / dist) * repForce;
                        double fy = (dy / dist) * repForce;
                        u.setLayoutX(u.getLayoutX() - fx);
                        u.setLayoutY(u.getLayoutY() - fy);
                        v.setLayoutX(v.getLayoutX() + fx);
                        v.setLayoutY(v.getLayoutY() + fy);
                    }
                }
            }

            // Edge attraction
            for (EdgeView edge : edgeViews) {
                NodeCardView u = edge.fromNode;
                NodeCardView v = edge.toNode;
                double dx = (v.getLayoutX() + 80) - (u.getLayoutX() + 80);
                double dy = (v.getLayoutY() + 35) - (u.getLayoutY() + 35);
                double dist = Math.hypot(dx, dy);
                double targetDist = 180.0;
                if (dist > targetDist) {
                    double attForce = (dist - targetDist) * 0.04;
                    double fx = (dx / dist) * attForce;
                    double fy = (dy / dist) * attForce;
                    u.setLayoutX(u.getLayoutX() + fx);
                    u.setLayoutY(u.getLayoutY() + fy);
                    v.setLayoutX(v.getLayoutX() - fx);
                    v.setLayoutY(v.getLayoutY() - fy);
                }
            }
        }

        // Clamp to visible viewport
        for (NodeCardView card : cards) {
            double maxX = Math.max(10, canvasW - 170);
            double maxY = Math.max(10, canvasH - 85);
            double clampedX = Math.max(10, Math.min(maxX, card.getLayoutX()));
            double clampedY = Math.max(10, Math.min(maxY, card.getLayoutY()));
            card.setLayoutX(clampedX);
            card.setLayoutY(clampedY);
        }

        updateAllEdgeGeometries();
    }

    private void updateAllEdgeGeometries() {
        for (EdgeView edge : edgeViews) {
            edge.updateGeometry();
        }
    }

    // ==================================================
    // Selection & Inspector
    // ==================================================

    public void selectNode(Note note) {
        handleDeselect();
        this.selectedNote = note;

        // Highlight selected node card
        NodeCardView card = nodeViews.get(note.getId());
        if (card != null) {
            card.getStyleClass().add("graph-node-selected");
        }

        // Highlight connected edges
        for (EdgeView edge : edgeViews) {
            if (edge.connection.getFromNoteId() == note.getId() || edge.connection.getToNoteId() == note.getId()) {
                edge.setSelected(true);
            }
        }

        // Highlight in 3D Knowledge Space
        if (knowledgeSpace3D != null) {
            knowledgeSpace3D.selectNote(note);
        }

        populateInspectorForNote(note);
    }

    public void selectConnection(Connection connection) {
        handleDeselect();
        this.selectedConnection = connection;

        for (EdgeView edge : edgeViews) {
            if (edge.connection.getId() == connection.getId()) {
                edge.setSelected(true);
            }
        }

        // Highlight in 3D Knowledge Space
        if (knowledgeSpace3D != null) {
            knowledgeSpace3D.selectConnection(connection);
        }

        Note fromNote = notesMap.get(connection.getFromNoteId());
        if (fromNote != null) {
            selectNode(fromNote);
        }
    }

    @FXML
    private void handleDeselect() {
        if (selectedNote != null) {
            NodeCardView card = nodeViews.get(selectedNote.getId());
            if (card != null) {
                card.getStyleClass().remove("graph-node-selected");
            }
        }
        for (EdgeView edge : edgeViews) {
            edge.setSelected(false);
        }
        if (knowledgeSpace3D != null) {
            knowledgeSpace3D.clearSelection();
        }
        selectedNote = null;
        selectedConnection = null;

        // Show Overview state in inspector
        boxOverview.setVisible(true);
        boxOverview.setManaged(true);
        boxSelectedNote.setVisible(false);
        boxSelectedNote.setManaged(false);
    }

    private void populateInspectorForNote(Note note) {
        boxOverview.setVisible(false);
        boxOverview.setManaged(false);
        boxSelectedNote.setVisible(true);
        boxSelectedNote.setManaged(true);

        lblSelectedTitle.setText(note.getTitle());

        // Subject Badge
        if (note.getSubject() != null && !note.getSubject().trim().isEmpty()) {
            lblSelectedSubject.setText(note.getSubject().trim());
            lblSelectedSubject.setVisible(true);
            lblSelectedSubject.setManaged(true);
        } else {
            lblSelectedSubject.setVisible(false);
            lblSelectedSubject.setManaged(false);
        }

        // Difficulty Badge
        if (note.getDifficulty() != null && !note.getDifficulty().trim().isEmpty()) {
            lblSelectedDifficulty.setText(note.getDifficulty().trim().toUpperCase());
            lblSelectedDifficulty.getStyleClass().removeAll("badge-easy", "badge-medium", "badge-hard");
            switch (note.getDifficulty().toUpperCase()) {
                case "EASY" -> lblSelectedDifficulty.getStyleClass().add("badge-easy");
                case "HARD" -> lblSelectedDifficulty.getStyleClass().add("badge-hard");
                default -> lblSelectedDifficulty.getStyleClass().add("badge-medium");
            }
            lblSelectedDifficulty.setVisible(true);
            lblSelectedDifficulty.setManaged(true);
        } else {
            lblSelectedDifficulty.setVisible(false);
            lblSelectedDifficulty.setManaged(false);
        }

        // Tags
        flowSelectedTags.getChildren().clear();
        if (note.getTags() != null && !note.getTags().isEmpty()) {
            for (com.mindmap.model.Tag tag : note.getTags()) {
                Label tagLabel = new Label("#" + tag.getName());
                tagLabel.getStyleClass().add("tag-badge");
                flowSelectedTags.getChildren().add(tagLabel);
            }
            flowSelectedTags.setVisible(true);
            flowSelectedTags.setManaged(true);
        } else {
            flowSelectedTags.setVisible(false);
            flowSelectedTags.setManaged(false);
        }

        // Connections for this note
        populateConnectionsList(note);
    }

    private void populateConnectionsList(Note note) {
        boxConnectionsList.getChildren().clear();

        List<Connection> noteConnections = connectionService.getConnectionsForNote(note.getId());
        lblConnectionCount.setText(String.valueOf(noteConnections.size()));

        if (noteConnections.isEmpty()) {
            Label emptyLabel = new Label("No connections yet.\nClick '+ Link' above to connect to another note.");
            emptyLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #94a3b8; -fx-font-style: italic; -fx-padding: 8 0;");
            emptyLabel.setWrapText(true);
            boxConnectionsList.getChildren().add(emptyLabel);
            return;
        }

        // Group into Outgoing and Incoming
        List<Connection> outgoing = noteConnections.stream()
                .filter(c -> c.getFromNoteId() == note.getId())
                .toList();

        List<Connection> incoming = noteConnections.stream()
                .filter(c -> c.getToNoteId() == note.getId())
                .toList();

        if (!outgoing.isEmpty()) {
            Label lblOut = new Label("OUTGOING (" + outgoing.size() + ")");
            lblOut.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #64748b; -fx-padding: 4 0 2 0;");
            boxConnectionsList.getChildren().add(lblOut);

            for (Connection conn : outgoing) {
                Note target = notesMap.get(conn.getToNoteId());
                String targetTitle = target != null ? target.getTitle() : "Note #" + conn.getToNoteId();
                boxConnectionsList.getChildren().add(createConnectionItemRow("→", conn.getRelation(), targetTitle, conn));
            }
        }

        if (!incoming.isEmpty()) {
            Label lblIn = new Label("INCOMING (" + incoming.size() + ")");
            lblIn.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #64748b; -fx-padding: 6 0 2 0;");
            boxConnectionsList.getChildren().add(lblIn);

            for (Connection conn : incoming) {
                Note source = notesMap.get(conn.getFromNoteId());
                String sourceTitle = source != null ? source.getTitle() : "Note #" + conn.getFromNoteId();
                boxConnectionsList.getChildren().add(createConnectionItemRow("←", conn.getRelation(), sourceTitle, conn));
            }
        }
    }

    private HBox createConnectionItemRow(String dirArrow, String relation, String otherTitle, Connection conn) {
        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("connection-item");

        Label arrowLabel = new Label(dirArrow);
        arrowLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #3b82f6; -fx-font-size: 12px;");

        Label relationBadge = new Label(relation);
        relationBadge.getStyleClass().add("connection-relation-badge");

        Label titleLabel = new Label(otherTitle);
        titleLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #1e293b; -fx-font-weight: 500;");
        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(130);
        HBox.setHgrow(titleLabel, Priority.ALWAYS);

        Button btnDelete = new Button("✕");
        btnDelete.getStyleClass().add("btn-delete-mini");
        btnDelete.setTooltip(new Tooltip("Delete connection"));
        btnDelete.setOnAction(e -> handleDeleteConnection(conn));

        row.getChildren().addAll(arrowLabel, relationBadge, titleLabel, btnDelete);
        return row;
    }

    // ==================================================
    // Action Handlers
    // ==================================================

    @FXML
    private void handleAddConnection() {
        openConnectDialog(selectedNote, null);
    }

    @FXML
    private void handleConnectFromSelected() {
        if (selectedNote != null) {
            openConnectDialog(selectedNote, null);
        }
    }

    private void openConnectDialog(Note initialSource, Note initialTarget) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/connection_dialog.fxml"));
            Parent root = loader.load();

            ConnectionDialogController controller = loader.getController();
            controller.setServices(connectionService, noteService);
            controller.setInitialSelection(initialSource, initialTarget);

            Stage stage = new Stage();
            stage.setTitle("Connect Notes");
            stage.initModality(Modality.APPLICATION_MODAL);
            if (canvasContainer.getScene() != null && canvasContainer.getScene().getWindow() != null) {
                stage.initOwner(canvasContainer.getScene().getWindow());
            }
            stage.setScene(new Scene(root));
            controller.setDialogStage(stage);

            stage.showAndWait();

            if (controller.isConnected()) {
                loadGraphData();
                Connection created = controller.getCreatedConnection();
                if (created != null) {
                    Note toNote = notesMap.get(created.getToNoteId());
                    if (toNote != null) {
                        selectNode(toNote);
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to open connection dialog: " + e.getMessage(), e);
            showErrorAlert("Error", "Could not open connection dialog: " + e.getMessage());
        }
    }

    private void handleDeleteConnection(Connection conn) {
        Note from = notesMap.get(conn.getFromNoteId());
        Note to = notesMap.get(conn.getToNoteId());
        String fromTitle = from != null ? from.getTitle() : "Note #" + conn.getFromNoteId();
        String toTitle = to != null ? to.getTitle() : "Note #" + conn.getToNoteId();

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Connection");
        alert.setHeaderText("Delete relationship between notes?");
        alert.setContentText("Are you sure you want to remove the relationship '" + conn.getRelation()
                + "' between '" + fromTitle + "' and '" + toTitle + "'?\n\nThis will only delete the connection. Both notes will remain intact.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean deleted = connectionService.deleteConnection(conn.getId());
            if (deleted) {
                loadGraphData();
            } else {
                showErrorAlert("Delete Failed", "Could not delete connection from database.");
            }
        }
    }

    @FXML
    private void handleViewSelectedNote() {
        if (selectedNote == null) return;
        Note fresh = noteService.getNoteWithTags(selectedNote.getId()).orElse(selectedNote);

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/note_view.fxml"));
            Parent root = loader.load();

            NoteViewController controller = loader.getController();
            Stage stage = new Stage();
            stage.setTitle("View Note - " + fresh.getTitle());
            stage.initModality(Modality.APPLICATION_MODAL);
            if (canvasContainer.getScene() != null && canvasContainer.getScene().getWindow() != null) {
                stage.initOwner(canvasContainer.getScene().getWindow());
            }
            stage.setScene(new Scene(root));
            controller.setDialogStage(stage);
            controller.setNote(fresh);
            controller.setEditHandler(note -> handleEditSelectedNote());

            stage.showAndWait();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to open note view dialog: " + e.getMessage(), e);
            showErrorAlert("Error", "Could not open note view dialog: " + e.getMessage());
        }
    }

    @FXML
    private void handleEditSelectedNote() {
        if (selectedNote == null) return;
        Note fresh = noteService.getNoteWithTags(selectedNote.getId()).orElse(selectedNote);

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/note_editor.fxml"));
            Parent root = loader.load();

            NoteEditorController controller = loader.getController();
            controller.setNoteService(noteService);

            Stage stage = new Stage();
            stage.setTitle("Edit Note");
            stage.initModality(Modality.APPLICATION_MODAL);
            if (canvasContainer.getScene() != null && canvasContainer.getScene().getWindow() != null) {
                stage.initOwner(canvasContainer.getScene().getWindow());
            }
            stage.setScene(new Scene(root));
            controller.setDialogStage(stage);
            controller.setNote(fresh, NoteEditorMode.EDIT);

            stage.showAndWait();

            if (controller.isSaved()) {
                loadGraphData();
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to open note editor dialog: " + e.getMessage(), e);
            showErrorAlert("Error", "Could not open note editor: " + e.getMessage());
        }
    }

    @FXML
    private void handleCreateFirstNote() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/note_editor.fxml"));
            Parent root = loader.load();

            NoteEditorController controller = loader.getController();
            controller.setNoteService(noteService);

            Stage stage = new Stage();
            stage.setTitle("Create First Note");
            stage.initModality(Modality.APPLICATION_MODAL);
            if (canvasContainer.getScene() != null && canvasContainer.getScene().getWindow() != null) {
                stage.initOwner(canvasContainer.getScene().getWindow());
            }
            stage.setScene(new Scene(root));
            controller.setDialogStage(stage);
            controller.setNote(null, NoteEditorMode.CREATE);

            stage.showAndWait();

            if (controller.isSaved()) {
                loadGraphData();
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to open note editor dialog: " + e.getMessage(), e);
            showErrorAlert("Error", "Could not open note editor: " + e.getMessage());
        }
    }

    @FXML
    private void handleRefresh() {
        loadGraphData();
    }

    @FXML
    private void handleClearSearch() {
        txtSearch.clear();
    }

    @FXML
    private void handleSwitchTo3D() {
        is3DMode = true;
        if (btn3DView != null) btn3DView.setSelected(true);
        if (btn2DView != null) btn2DView.setSelected(false);
        if (space3DContainer != null) {
            space3DContainer.setVisible(true);
            space3DContainer.setManaged(true);
        }
        if (graphCanvasPane != null) {
            graphCanvasPane.setVisible(false);
            graphCanvasPane.setManaged(false);
        }
        if (btnFocusSelected != null) {
            btnFocusSelected.setVisible(true);
            btnFocusSelected.setManaged(true);
        }
        if (btnAutoLayout != null) {
            btnAutoLayout.setVisible(false);
            btnAutoLayout.setManaged(false);
        }
        if (lblZoomLevel != null) {
            lblZoomLevel.setText("3D");
        }
    }

    @FXML
    private void handleSwitchTo2D() {
        is3DMode = false;
        if (btn2DView != null) btn2DView.setSelected(true);
        if (btn3DView != null) btn3DView.setSelected(false);
        if (space3DContainer != null) {
            space3DContainer.setVisible(false);
            space3DContainer.setManaged(false);
        }
        if (graphCanvasPane != null) {
            graphCanvasPane.setVisible(true);
            graphCanvasPane.setManaged(true);
        }
        if (btnFocusSelected != null) {
            btnFocusSelected.setVisible(false);
            btnFocusSelected.setManaged(false);
        }
        if (btnAutoLayout != null) {
            btnAutoLayout.setVisible(true);
            btnAutoLayout.setManaged(true);
        }
        if (lblZoomLevel != null) {
            lblZoomLevel.setText((int) Math.round(zoomFactor * 100) + "%");
        }
        runAutoLayout();
    }

    /**
     * Programmatically selects a note and focuses the 2D canvas or 3D camera on it.
     *
     * @param note The note to select and focus.
     */
    public void focusNote(Note note) {
        if (note != null) {
            selectNode(note);
            handleFocusSelected();
        }
    }

    @FXML
    public void handleFocusSelected() {
        if (is3DMode && knowledgeSpace3D != null) {
            knowledgeSpace3D.focusSelected();
        } else if (!is3DMode && selectedNote != null) {
            NodeCardView card = nodeViews.get(selectedNote.getId());
            if (card != null) {
                double canvasW = graphCanvasPane.getWidth() > 0 ? graphCanvasPane.getWidth() : 750;
                double canvasH = graphCanvasPane.getHeight() > 0 ? graphCanvasPane.getHeight() : 550;
                panX = (canvasW / 2.0) - (card.getLayoutX() + 80) * zoomFactor;
                panY = (canvasH / 2.0) - (card.getLayoutY() + 35) * zoomFactor;
                applyTransform();
            }
        }
    }

    private void applySearchFilter(String query) {
        if (knowledgeSpace3D != null) {
            knowledgeSpace3D.search(query);
        }

        if (query == null || query.trim().isEmpty()) {
            for (NodeCardView card : nodeViews.values()) {
                card.setDimmed(false);
            }
            for (EdgeView edge : edgeViews) {
                edge.setDimmed(false);
            }
            return;
        }

        String lowerQuery = query.trim().toLowerCase();

        for (Map.Entry<Integer, NodeCardView> entry : nodeViews.entrySet()) {
            Note note = entry.getValue().note;
            boolean matches = (note.getTitle() != null && note.getTitle().toLowerCase().contains(lowerQuery))
                    || (note.getSubject() != null && note.getSubject().toLowerCase().contains(lowerQuery))
                    || (note.getContent() != null && note.getContent().toLowerCase().contains(lowerQuery));

            entry.getValue().setDimmed(!matches);
        }

        // Dim edges if either endpoint is dimmed
        for (EdgeView edge : edgeViews) {
            boolean fromDim = edge.fromNode.isDimmed();
            boolean toDim = edge.toNode.isDimmed();
            edge.setDimmed(fromDim || toDim);
        }
    }

    // ==================================================
    // Zoom Handlers
    // ==================================================

    @FXML
    private void handleZoomIn() {
        if (is3DMode && knowledgeSpace3D != null) {
            knowledgeSpace3D.getCameraController().zoomBy(180.0);
        } else {
            zoomFactor = Math.min(2.5, zoomFactor * 1.15);
            applyTransform();
        }
    }

    @FXML
    private void handleZoomOut() {
        if (is3DMode && knowledgeSpace3D != null) {
            knowledgeSpace3D.getCameraController().zoomBy(-180.0);
        } else {
            zoomFactor = Math.max(0.4, zoomFactor / 1.15);
            applyTransform();
        }
    }

    @FXML
    private void handleResetView() {
        if (knowledgeSpace3D != null) {
            knowledgeSpace3D.resetView();
        }
        zoomFactor = 1.0;
        panX = 0;
        panY = 0;
        applyTransform();
        runAutoLayout();
        if (is3DMode && lblZoomLevel != null) {
            lblZoomLevel.setText("3D");
        }
    }

    private void showErrorAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // ==================================================
    // Visual Node Card Component
    // ==================================================

    private class NodeCardView extends VBox {
        private final Note note;
        private double mouseAnchorX;
        private double mouseAnchorY;
        private double nodeStartX;
        private double nodeStartY;
        private boolean isDragging = false;
        private boolean dimmed = false;

        public NodeCardView(Note note) {
            this.note = note;
            setPrefWidth(160);
            setMaxWidth(160);
            setSpacing(6);
            getStyleClass().add("graph-node");

            // Subject Badge Header
            if (note.getSubject() != null && !note.getSubject().trim().isEmpty()) {
                Label subjectBadge = new Label(note.getSubject().trim());
                subjectBadge.getStyleClass().add("graph-node-subject");
                subjectBadge.setMaxWidth(140);
                getChildren().add(subjectBadge);
            }

            // Title Label
            Label titleLabel = new Label(note.getTitle());
            titleLabel.getStyleClass().add("graph-node-title");
            titleLabel.setWrapText(true);
            titleLabel.setMaxHeight(40);
            getChildren().add(titleLabel);

            setupInteractions();
        }

        private void setupInteractions() {
            setCursor(Cursor.HAND);

            setOnMouseEntered(e -> {
                if (!isDragging && !dimmed) {
                    setScaleX(1.03);
                    setScaleY(1.03);
                }
            });

            setOnMouseExited(e -> {
                if (!isDragging) {
                    setScaleX(1.0);
                    setScaleY(1.0);
                }
            });

            setOnMousePressed(event -> {
                if (event.getButton() == MouseButton.PRIMARY) {
                    isDragging = false;
                    mouseAnchorX = event.getSceneX();
                    mouseAnchorY = event.getSceneY();
                    nodeStartX = getLayoutX();
                    nodeStartY = getLayoutY();
                    toFront();
                    event.consume();
                }
            });

            setOnMouseDragged(event -> {
                if (event.getButton() == MouseButton.PRIMARY) {
                    double dx = (event.getSceneX() - mouseAnchorX) / zoomFactor;
                    double dy = (event.getSceneY() - mouseAnchorY) / zoomFactor;

                    if (Math.hypot(dx, dy) > 4) {
                        isDragging = true;
                    }

                    double newX = Math.max(10, nodeStartX + dx);
                    double newY = Math.max(10, nodeStartY + dy);

                    setLayoutX(newX);
                    setLayoutY(newY);

                    updateConnectedEdges(note.getId());
                    event.consume();
                }
            });

            setOnMouseReleased(event -> {
                if (!isDragging) {
                    selectNode(note);
                }
                isDragging = false;
                event.consume();
            });
        }

        public void setDimmed(boolean dimmed) {
            this.dimmed = dimmed;
            if (dimmed) {
                getStyleClass().add("graph-node-dimmed");
            } else {
                getStyleClass().remove("graph-node-dimmed");
            }
        }

        public boolean isDimmed() {
            return dimmed;
        }
    }

    private void updateConnectedEdges(int noteId) {
        for (EdgeView edge : edgeViews) {
            if (edge.connection.getFromNoteId() == noteId || edge.connection.getToNoteId() == noteId) {
                edge.updateGeometry();
            }
        }
    }

    // ==================================================
    // Visual Edge Component
    // ==================================================

    private class EdgeView {
        private final Connection connection;
        private final NodeCardView fromNode;
        private final NodeCardView toNode;

        private final Line line = new Line();
        private final Polygon arrow = new Polygon();
        private final Label badge = new Label();

        public EdgeView(Connection connection, NodeCardView fromNode, NodeCardView toNode) {
            this.connection = connection;
            this.fromNode = fromNode;
            this.toNode = toNode;

            line.getStyleClass().add("graph-edge-line");
            arrow.getStyleClass().add("graph-edge-arrow");

            badge.setText(connection.getRelation() != null ? connection.getRelation() : "Related");
            badge.getStyleClass().add("graph-edge-label");

            // Click edge to select connection
            line.setOnMouseClicked(e -> {
                selectConnection(connection);
                e.consume();
            });
            badge.setOnMouseClicked(e -> {
                selectConnection(connection);
                e.consume();
            });
            arrow.setOnMouseClicked(e -> {
                selectConnection(connection);
                e.consume();
            });

            updateGeometry();
        }

        public void updateGeometry() {
            double w1 = fromNode.getWidth() > 0 ? fromNode.getWidth() : 160.0;
            double h1 = fromNode.getHeight() > 0 ? fromNode.getHeight() : 70.0;
            double w2 = toNode.getWidth() > 0 ? toNode.getWidth() : 160.0;
            double h2 = toNode.getHeight() > 0 ? toNode.getHeight() : 70.0;

            double x1 = fromNode.getLayoutX() + w1 / 2.0;
            double y1 = fromNode.getLayoutY() + h1 / 2.0;

            double x2 = toNode.getLayoutX() + w2 / 2.0;
            double y2 = toNode.getLayoutY() + h2 / 2.0;

            line.setStartX(x1);
            line.setStartY(y1);
            line.setEndX(x2);
            line.setEndY(y2);

            double dx = x2 - x1;
            double dy = y2 - y1;
            double len = Math.hypot(dx, dy);

            if (len > 0) {
                double ux = dx / len;
                double uy = dy / len;

                // Offset tip of arrowhead just outside target card edge (~50px)
                double offset = 50.0;
                double tipX = x2 - ux * offset;
                double tipY = y2 - uy * offset;

                double arrowLen = 10.0;
                double arrowHalfWidth = 5.0;

                double baseX = tipX - ux * arrowLen;
                double baseY = tipY - uy * arrowLen;

                double p1X = baseX - uy * arrowHalfWidth;
                double p1Y = baseY + ux * arrowHalfWidth;

                double p2X = baseX + uy * arrowHalfWidth;
                double p2Y = baseY - ux * arrowHalfWidth;

                arrow.getPoints().setAll(
                        tipX, tipY,
                        p1X, p1Y,
                        p2X, p2Y
                );

                // Position badge at midpoint
                double midX = (x1 + x2) / 2.0;
                double midY = (y1 + y2) / 2.0;

                double badgeW = badge.getWidth() > 0 ? badge.getWidth() : 50.0;
                double badgeH = badge.getHeight() > 0 ? badge.getHeight() : 18.0;

                badge.setLayoutX(midX - badgeW / 2.0);
                badge.setLayoutY(midY - badgeH / 2.0);
            }
        }

        public void setSelected(boolean selected) {
            if (selected) {
                if (!line.getStyleClass().contains("graph-edge-line-selected")) {
                    line.getStyleClass().add("graph-edge-line-selected");
                }
                if (!arrow.getStyleClass().contains("graph-edge-arrow-selected")) {
                    arrow.getStyleClass().add("graph-edge-arrow-selected");
                }
                if (!badge.getStyleClass().contains("graph-edge-label-selected")) {
                    badge.getStyleClass().add("graph-edge-label-selected");
                }
            } else {
                line.getStyleClass().remove("graph-edge-line-selected");
                arrow.getStyleClass().remove("graph-edge-arrow-selected");
                badge.getStyleClass().remove("graph-edge-label-selected");
            }
        }

        public void setDimmed(boolean dimmed) {
            double op = dimmed ? 0.15 : 1.0;
            line.setOpacity(op);
            arrow.setOpacity(op);
            badge.setOpacity(op);
        }
    }
}
