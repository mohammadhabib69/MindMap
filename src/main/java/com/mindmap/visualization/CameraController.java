package com.mindmap.visualization;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Point3D;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SubScene;
import javafx.scene.input.MouseButton;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.util.Duration;

/**
 * Manages 3D camera navigation for the Knowledge Space.
 * Provides intuitive orbit, pan, zoom, smooth reset, and node focusing.
 */
public class CameraController {

    public static final double DEFAULT_CAMERA_DISTANCE = -1000.0;
    public static final double MIN_CAMERA_DISTANCE = -3500.0;
    public static final double MAX_CAMERA_DISTANCE = -350.0;

    private final PerspectiveCamera camera;
    private final Rotate rotateX = new Rotate(0, Rotate.X_AXIS);
    private final Rotate rotateY = new Rotate(0, Rotate.Y_AXIS);
    private final Translate panTranslate = new Translate(0, 0, 0);
    private final Translate zoomTranslate = new Translate(0, 0, DEFAULT_CAMERA_DISTANCE);

    private double mouseAnchorX;
    private double mouseAnchorY;
    private double anchorRotateX;
    private double anchorRotateY;
    private double anchorPanX;
    private double anchorPanY;

    public CameraController(PerspectiveCamera camera) {
        this.camera = camera;
        this.camera.setNearClip(0.1);
        this.camera.setFarClip(10000.0);
        this.camera.setFieldOfView(42.0);

        // Transformation order: pan -> orbit Y -> orbit X -> zoom along Z
        this.camera.getTransforms().setAll(panTranslate, rotateY, rotateX, zoomTranslate);
    }

    /**
     * Attaches mouse event listeners to the SubScene for orbit, pan, and zoom.
     */
    public void attachTo(SubScene subScene) {
        subScene.setOnMousePressed(event -> {
            mouseAnchorX = event.getSceneX();
            mouseAnchorY = event.getSceneY();
            anchorRotateX = rotateX.getAngle();
            anchorRotateY = rotateY.getAngle();
            anchorPanX = panTranslate.getX();
            anchorPanY = panTranslate.getY();
        });

        subScene.setOnMouseDragged(event -> {
            double dx = event.getSceneX() - mouseAnchorX;
            double dy = event.getSceneY() - mouseAnchorY;

            if (event.getButton() == MouseButton.PRIMARY) {
                // Orbit rotation (inverted Y for intuitive natural drag)
                rotateY.setAngle(anchorRotateY + dx * 0.35);
                double newRotX = anchorRotateX - dy * 0.35;
                // Clamp X rotation to avoid camera flipping over poles
                rotateX.setAngle(Math.max(-85.0, Math.min(85.0, newRotX)));
            } else if (event.getButton() == MouseButton.SECONDARY || event.getButton() == MouseButton.MIDDLE) {
                // Pan translation along screen plane
                panTranslate.setX(anchorPanX + dx * 0.9);
                panTranslate.setY(anchorPanY + dy * 0.9);
            }
        });

        subScene.setOnScroll(event -> {
            double delta = event.getDeltaY();
            zoomBy(delta * 2.2);
            event.consume();
        });
    }

    /**
     * Increments or decrements zoom distance along the camera Z axis, clamped to boundaries.
     */
    public void zoomBy(double delta) {
        double currentZ = zoomTranslate.getZ();
        double newZ = Math.max(MIN_CAMERA_DISTANCE, Math.min(MAX_CAMERA_DISTANCE, currentZ + delta));
        zoomTranslate.setZ(newZ);
    }

    /**
     * Smoothly restores camera orbit, pan, and zoom to initial defaults.
     */
    public void resetView() {
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.millis(350),
                        new KeyValue(rotateX.angleProperty(), 0.0),
                        new KeyValue(rotateY.angleProperty(), 0.0),
                        new KeyValue(panTranslate.xProperty(), 0.0),
                        new KeyValue(panTranslate.yProperty(), 0.0),
                        new KeyValue(zoomTranslate.zProperty(), DEFAULT_CAMERA_DISTANCE)
                )
        );
        timeline.play();
    }

    /**
     * Smoothly animates camera to focus on the selected 3D node.
     */
    public void focusOnNode(Node3D node) {
        if (node == null) return;

        Point3D pos = node.getPosition();
        // Shift pan so node position is centered at camera look-at
        double targetPanX = -pos.getX();
        double targetPanY = -pos.getY();
        double targetZoomZ = Math.max(MIN_CAMERA_DISTANCE, Math.min(-550.0, -Math.abs(pos.getZ()) - 500.0));

        Timeline timeline = new Timeline(
                new KeyFrame(Duration.millis(420),
                        new KeyValue(panTranslate.xProperty(), targetPanX),
                        new KeyValue(panTranslate.yProperty(), targetPanY),
                        new KeyValue(zoomTranslate.zProperty(), targetZoomZ)
                )
        );
        timeline.play();
    }

    public PerspectiveCamera getCamera() {
        return camera;
    }

    public Rotate getRotateX() {
        return rotateX;
    }

    public Rotate getRotateY() {
        return rotateY;
    }

    public Translate getPanTranslate() {
        return panTranslate;
    }

    public Translate getZoomTranslate() {
        return zoomTranslate;
    }
}
