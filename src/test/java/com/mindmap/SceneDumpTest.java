package com.mindmap;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import java.net.URL;
import javafx.fxml.FXMLLoader;

@ExtendWith(ApplicationExtension.class)
public class SceneDumpTest {

    private Parent root;

    @Start
    public void start(Stage stage) throws Exception {
        com.mindmap.database.DatabaseInitializer.initialize();
        URL fxmlUrl = getClass().getResource("/fxml/main.fxml");
        root = FXMLLoader.load(fxmlUrl);
        Scene scene = new Scene(root, 1100, 720);
        stage.setScene(scene);
        stage.show();
    }

    @Test
    public void dumpScene(FxRobot robot) {
        robot.clickOn("#btnQuiz");
        try { Thread.sleep(1000); } catch (Exception e) {}
        printNode(root, "");
    }

    private void printNode(Node node, String indent) {
        System.out.println(indent + node.getClass().getSimpleName() + " (id=" + node.getId() + ")");
        if (node instanceof Parent) {
            for (Node child : ((Parent)node).getChildrenUnmodifiable()) {
                printNode(child, indent + "  ");
            }
        }
    }
}
