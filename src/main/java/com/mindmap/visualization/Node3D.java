package com.mindmap.visualization;

import com.mindmap.model.Note;
import javafx.animation.ScaleTransition;
import javafx.geometry.Point3D;
import javafx.scene.Cursor;
import javafx.scene.control.Tooltip;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;
import javafx.util.Duration;

import java.util.function.Consumer;

/**
 * Visual 3D object representing a Note in the Knowledge Space.
 * Rendered as an interactive Phong-shaded Sphere with subject/difficulty color coding.
 */
public class Node3D {

    public static final double DEFAULT_RADIUS = 18.0;
    private static final Duration HOVER_DURATION = Duration.millis(120);

    private final Note note;
    private final Point3D position;
    private final Sphere sphere;

    private PhongMaterial normalMaterial;
    private PhongMaterial selectedMaterial;
    private PhongMaterial dimmedMaterial;

    private boolean selected = false;
    private boolean dimmed = false;
    private Consumer<Node3D> onSelectHandler;

    public Node3D(Note note, Point3D position) {
        this.note = note;
        this.position = position;

        this.sphere = new Sphere(DEFAULT_RADIUS);
        this.sphere.setTranslateX(position.getX());
        this.sphere.setTranslateY(position.getY());
        this.sphere.setTranslateZ(position.getZ());

        initMaterials();
        setupInteractions();
    }

    private void initMaterials() {
        Color baseColor = resolveBaseColor(note.getSubject(), note.getDifficulty());

        // Normal material
        normalMaterial = new PhongMaterial();
        normalMaterial.setDiffuseColor(baseColor);
        normalMaterial.setSpecularColor(Color.rgb(240, 245, 255));
        normalMaterial.setSpecularPower(30.0);

        // Selected material (glowing electric cyan)
        selectedMaterial = new PhongMaterial();
        selectedMaterial.setDiffuseColor(Color.rgb(14, 165, 233));
        selectedMaterial.setSpecularColor(Color.rgb(255, 255, 255));
        selectedMaterial.setSpecularPower(64.0);

        // Dimmed material (subdued dark slate for search filtering)
        dimmedMaterial = new PhongMaterial();
        dimmedMaterial.setDiffuseColor(Color.rgb(71, 85, 105, 0.45));
        dimmedMaterial.setSpecularColor(Color.rgb(30, 41, 59));
        dimmedMaterial.setSpecularPower(4.0);

        sphere.setMaterial(normalMaterial);
    }

    private Color resolveBaseColor(String subject, String difficulty) {
        if (subject != null) {
            String sub = subject.toLowerCase().trim();
            if (sub.contains("program") || sub.contains("code") || sub.contains("java") || sub.contains("cs")) {
                return Color.rgb(37, 99, 235); // Sapphire Blue
            }
            if (sub.contains("math") || sub.contains("algo") || sub.contains("logic")) {
                return Color.rgb(124, 58, 237); // Royal Violet
            }
            if (sub.contains("data") || sub.contains("sql") || sub.contains("db")) {
                return Color.rgb(8, 145, 178); // Cyan
            }
            if (sub.contains("physic") || sub.contains("science") || sub.contains("bio")) {
                return Color.rgb(5, 150, 105); // Emerald
            }
            if (sub.contains("history") || sub.contains("art") || sub.contains("lit")) {
                return Color.rgb(217, 119, 6); // Amber
            }
        }

        if (difficulty != null) {
            switch (difficulty.toUpperCase().trim()) {
                case "HARD" -> {
                    return Color.rgb(225, 29, 72); // Crimson
                }
                case "EASY" -> {
                    return Color.rgb(16, 185, 129); // Green
                }
                default -> {
                    return Color.rgb(79, 70, 229); // Indigo
                }
            }
        }

        return Color.rgb(79, 70, 229); // Default Indigo
    }

    private void setupInteractions() {
        sphere.setCursor(Cursor.HAND);

        // Tooltip showing title and subject
        StringBuilder tip = new StringBuilder(note.getTitle());
        if (note.getSubject() != null && !note.getSubject().trim().isEmpty()) {
            tip.append("  •  ").append(note.getSubject().trim());
        }
        if (note.getDifficulty() != null && !note.getDifficulty().trim().isEmpty()) {
            tip.append(" [").append(note.getDifficulty().trim().toUpperCase()).append("]");
        }
        Tooltip tooltip = new Tooltip(tip.toString());
        tooltip.setStyle("-fx-font-size: 12px; -fx-background-color: rgba(15, 23, 42, 0.9); -fx-text-fill: white; -fx-padding: 6 10; -fx-background-radius: 6px;");
        Tooltip.install(sphere, tooltip);

        // Hover scale animation
        sphere.setOnMouseEntered(e -> {
            if (!selected) {
                playScaleAnimation(1.18);
            }
        });

        sphere.setOnMouseExited(e -> {
            if (!selected) {
                playScaleAnimation(1.0);
            }
        });

        // Click selection
        sphere.setOnMouseClicked(e -> {
            if (onSelectHandler != null) {
                onSelectHandler.accept(this);
            }
            e.consume();
        });
    }

    private void playScaleAnimation(double targetScale) {
        ScaleTransition st = new ScaleTransition(HOVER_DURATION, sphere);
        st.setToX(targetScale);
        st.setToY(targetScale);
        st.setToZ(targetScale);
        st.play();
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
        if (selected) {
            sphere.setMaterial(selectedMaterial);
            playScaleAnimation(1.25);
        } else {
            sphere.setMaterial(dimmed ? dimmedMaterial : normalMaterial);
            playScaleAnimation(1.0);
        }
    }

    public void setDimmed(boolean dimmed) {
        this.dimmed = dimmed;
        if (!selected) {
            sphere.setMaterial(dimmed ? dimmedMaterial : normalMaterial);
        }
    }

    public void setOnSelectHandler(Consumer<Node3D> onSelectHandler) {
        this.onSelectHandler = onSelectHandler;
    }

    public Note getNote() {
        return note;
    }

    public Point3D getPosition() {
        return position;
    }

    public Sphere getSphere() {
        return sphere;
    }

    public boolean isSelected() {
        return selected;
    }

    public boolean isDimmed() {
        return dimmed;
    }
}
