package com.mindmap.visualization;

import com.mindmap.model.Connection;
import javafx.geometry.Point3D;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.control.Tooltip;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;

import java.util.function.Consumer;

/**
 * Visual 3D cylinder edge representing a Connection between two notes in Knowledge Space.
 */
public class Connection3D {

    public static final double DEFAULT_RADIUS = 2.2;
    public static final double SELECTED_RADIUS = 3.8;

    private final Connection connection;
    private final Node3D fromNode;
    private final Node3D toNode;

    private final Group edgeGroup;
    private final Cylinder cylinder;
    private final Sphere directionalMarker;

    private PhongMaterial normalMaterial;
    private PhongMaterial selectedMaterial;
    private PhongMaterial dimmedMaterial;

    private boolean selected = false;
    private Consumer<Connection3D> onSelectHandler;

    public Connection3D(Connection connection, Node3D fromNode, Node3D toNode) {
        this.connection = connection;
        this.fromNode = fromNode;
        this.toNode = toNode;

        this.edgeGroup = new Group();
        this.cylinder = new Cylinder(DEFAULT_RADIUS, 1.0);
        this.directionalMarker = new Sphere(4.5);

        initMaterials();
        updateGeometry();
        setupInteractions();

        this.edgeGroup.getChildren().addAll(cylinder, directionalMarker);
    }

    private void initMaterials() {
        // Normal metallic slate/silver
        normalMaterial = new PhongMaterial();
        normalMaterial.setDiffuseColor(Color.rgb(148, 163, 184));
        normalMaterial.setSpecularColor(Color.rgb(203, 213, 225));
        normalMaterial.setSpecularPower(16.0);

        // Selected vibrant azure/blue
        selectedMaterial = new PhongMaterial();
        selectedMaterial.setDiffuseColor(Color.rgb(37, 99, 235));
        selectedMaterial.setSpecularColor(Color.rgb(255, 255, 255));
        selectedMaterial.setSpecularPower(48.0);

        // Dimmed muted slate
        dimmedMaterial = new PhongMaterial();
        dimmedMaterial.setDiffuseColor(Color.rgb(51, 65, 85, 0.35));
        dimmedMaterial.setSpecularColor(Color.rgb(30, 41, 59));
        dimmedMaterial.setSpecularPower(2.0);

        cylinder.setMaterial(normalMaterial);
        directionalMarker.setMaterial(normalMaterial);
    }

    public void updateGeometry() {
        Point3D p1 = fromNode.getPosition();
        Point3D p2 = toNode.getPosition();
        Point3D diff = p2.subtract(p1);
        double length = diff.magnitude();
        Point3D mid = p1.add(p2).multiply(0.5);

        cylinder.setHeight(length);
        cylinder.setTranslateX(mid.getX());
        cylinder.setTranslateY(mid.getY());
        cylinder.setTranslateZ(mid.getZ());

        // Align cylinder (default along Y-axis) with the 3D direction vector
        Point3D yAxis = new Point3D(0, 1, 0);
        Point3D axis = diff.crossProduct(yAxis);
        double angle = Math.acos(diff.normalize().dotProduct(yAxis));

        if (axis.magnitude() > 1e-6) {
            Rotate rotate = new Rotate(-Math.toDegrees(angle), axis);
            cylinder.getTransforms().setAll(rotate);
        } else if (diff.getY() < 0) {
            Rotate rotate = new Rotate(180, Rotate.Z_AXIS);
            cylinder.getTransforms().setAll(rotate);
        } else {
            cylinder.getTransforms().clear();
        }

        // Place directional marker at 70% toward target node
        Point3D markerPos = p1.add(diff.multiply(0.70));
        directionalMarker.setTranslateX(markerPos.getX());
        directionalMarker.setTranslateY(markerPos.getY());
        directionalMarker.setTranslateZ(markerPos.getZ());
    }

    private void setupInteractions() {
        cylinder.setCursor(Cursor.HAND);
        directionalMarker.setCursor(Cursor.HAND);

        String relationText = connection.getRelation() != null ? connection.getRelation() : "Related";
        String tipText = "Link: " + relationText + "\nFrom: " + fromNode.getNote().getTitle() + "\nTo: " + toNode.getNote().getTitle();

        Tooltip tooltip = new Tooltip(tipText);
        tooltip.setStyle("-fx-font-size: 11px; -fx-background-color: rgba(15, 23, 42, 0.9); -fx-text-fill: white; -fx-padding: 6 10; -fx-background-radius: 6px;");
        Tooltip.install(cylinder, tooltip);
        Tooltip.install(directionalMarker, tooltip);

        cylinder.setOnMouseClicked(e -> {
            if (onSelectHandler != null) {
                onSelectHandler.accept(this);
            }
            e.consume();
        });

        directionalMarker.setOnMouseClicked(e -> {
            if (onSelectHandler != null) {
                onSelectHandler.accept(this);
            }
            e.consume();
        });
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
        if (selected) {
            cylinder.setRadius(SELECTED_RADIUS);
            directionalMarker.setRadius(6.0);
            cylinder.setMaterial(selectedMaterial);
            directionalMarker.setMaterial(selectedMaterial);
        } else {
            cylinder.setRadius(DEFAULT_RADIUS);
            directionalMarker.setRadius(4.5);
            cylinder.setMaterial(normalMaterial);
            directionalMarker.setMaterial(normalMaterial);
        }
    }

    private boolean dimmed = false;

    public void setDimmed(boolean dimmed) {
        this.dimmed = dimmed;
        if (!selected) {
            cylinder.setMaterial(dimmed ? dimmedMaterial : normalMaterial);
            directionalMarker.setMaterial(dimmed ? dimmedMaterial : normalMaterial);
        }
    }

    public boolean isDimmed() {
        return dimmed;
    }

    public void setOnSelectHandler(Consumer<Connection3D> onSelectHandler) {
        this.onSelectHandler = onSelectHandler;
    }

    public Connection getConnection() {
        return connection;
    }

    public Node3D getFromNode() {
        return fromNode;
    }

    public Node3D getToNode() {
        return toNode;
    }

    public Cylinder getCylinder() {
        return cylinder;
    }

    public Sphere getDirectionalMarker() {
        return directionalMarker;
    }

    public Group getEdgeGroup() {
        return edgeGroup;
    }

    public boolean isSelected() {
        return selected;
    }
}
