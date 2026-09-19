package com.mindmap.util;

import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.util.Duration;

/**
 * Utility providing lightweight, smooth JavaFX animations and 3D hover/elevation effects.
 */
public final class AnimationUtil {

    private static final Duration CARD_ANIM_DURATION = Duration.millis(160);
    private static final Duration BUTTON_ANIM_DURATION = Duration.millis(120);
    private static final Duration PRESS_ANIM_DURATION = Duration.millis(75);

    private AnimationUtil() {
        // Prevent instantiation
    }

    /**
     * Adds an interactive 3D floating elevation and scale effect to a card Node.
     *
     * @param card The container node (e.g., VBox card).
     */
    public static void addCardHoverEffect(Node card) {
        if (card == null) return;

        DropShadow restShadow = new DropShadow(BlurType.GAUSSIAN, Color.rgb(15, 23, 42, 0.08), 12, 0.15, 0, 4);
        DropShadow hoverShadow = new DropShadow(BlurType.GAUSSIAN, Color.rgb(15, 23, 42, 0.16), 22, 0.2, 0, 8);

        card.setEffect(restShadow);

        card.setOnMouseEntered(event -> {
            card.setEffect(hoverShadow);
            playScaleAnimation(card, 1.02, 1.02, CARD_ANIM_DURATION);
        });

        card.setOnMouseExited(event -> {
            card.setEffect(restShadow);
            playScaleAnimation(card, 1.0, 1.0, CARD_ANIM_DURATION);
        });
    }

    /**
     * Adds a tactile 3D hover and pressed animation to a Button or interactive control.
     *
     * @param button The button to animate.
     */
    public static void addButtonHoverEffect(Node button) {
        if (button == null) return;

        button.setCursor(Cursor.HAND);

        button.setOnMouseEntered(event -> {
            playScaleAnimation(button, 1.025, 1.025, BUTTON_ANIM_DURATION);
        });

        button.setOnMouseExited(event -> {
            playScaleAnimation(button, 1.0, 1.0, BUTTON_ANIM_DURATION);
        });

        button.setOnMousePressed(event -> {
            playScaleAnimation(button, 0.975, 0.975, PRESS_ANIM_DURATION);
        });

        button.setOnMouseReleased(event -> {
            if (button.isHover()) {
                playScaleAnimation(button, 1.025, 1.025, PRESS_ANIM_DURATION);
            } else {
                playScaleAnimation(button, 1.0, 1.0, PRESS_ANIM_DURATION);
            }
        });
    }

    /**
     * Adds smooth subtle lateral nudge and scale to sidebar navigation buttons.
     *
     * @param navButton The sidebar navigation button.
     */
    public static void addSidebarNavHoverEffect(Button navButton) {
        if (navButton == null) return;

        navButton.setCursor(Cursor.HAND);

        navButton.setOnMouseEntered(event -> {
            if (!navButton.getStyleClass().contains("active")) {
                playTranslateXAnimation(navButton, 3.0, BUTTON_ANIM_DURATION);
                playScaleAnimation(navButton, 1.015, 1.015, BUTTON_ANIM_DURATION);
            }
        });

        navButton.setOnMouseExited(event -> {
            playTranslateXAnimation(navButton, 0.0, BUTTON_ANIM_DURATION);
            playScaleAnimation(navButton, 1.0, 1.0, BUTTON_ANIM_DURATION);
        });
    }

    private static void playScaleAnimation(Node node, double toX, double toY, Duration duration) {
        ScaleTransition st = (ScaleTransition) node.getProperties().get("scaleTransition");
        if (st != null) {
            st.stop();
        }
        st = new ScaleTransition(duration, node);
        st.setToX(toX);
        st.setToY(toY);
        node.getProperties().put("scaleTransition", st);
        st.play();
    }

    private static void playTranslateXAnimation(Node node, double toX, Duration duration) {
        TranslateTransition tt = (TranslateTransition) node.getProperties().get("translateTransition");
        if (tt != null) {
            tt.stop();
        }
        tt = new TranslateTransition(duration, node);
        tt.setToX(toX);
        node.getProperties().put("translateTransition", tt);
        tt.play();
    }
}
