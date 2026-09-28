import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.net.URL;

public class dump_scene extends Application {
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
                    printNode(root, "");
                    Platform.exit();
                });
            } catch (Exception e) {}
        });
    }
    
    private void printNode(javafx.scene.Node node, String indent) {
        System.out.println(indent + node.getClass().getSimpleName() + " (id=" + node.getId() + ")");
        if (node instanceof javafx.scene.Parent) {
            for (javafx.scene.Node child : ((javafx.scene.Parent)node).getChildrenUnmodifiable()) {
                printNode(child, indent + "  ");
            }
        }
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
