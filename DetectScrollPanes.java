import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import java.net.URL;

public class DetectScrollPanes extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        com.mindmap.database.DatabaseInitializer.initialize();
        URL fxmlUrl = getClass().getResource("/fxml/main.fxml");
        Parent root = FXMLLoader.load(fxmlUrl);
        Scene scene = new Scene(root, 1100, 720);
        stage.setScene(scene);
        stage.show();

        javafx.scene.control.Button btnQuiz = (javafx.scene.control.Button) scene.lookup("#btnQuiz");
        btnQuiz.fire();

        Platform.runLater(() -> {
            try {
                Thread.sleep(1000);
                Platform.runLater(() -> {
                    System.out.println("--- SCROLLPANES ---");
                    findAndPrint(root, ScrollPane.class);
                    System.out.println("--- LISTVIEWS ---");
                    findAndPrint(root, ListView.class);
                    Platform.exit();
                });
            } catch (Exception e) {}
        });
    }

    private void findAndPrint(Node node, Class<?> clazz) {
        if (clazz.isInstance(node)) {
            System.out.println("Found: " + node.getClass().getName() + " id=" + node.getId() + " bounds=" + node.getBoundsInParent());
        }
        if (node instanceof Parent) {
            for (Node child : ((Parent)node).getChildrenUnmodifiable()) {
                findAndPrint(child, clazz);
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
