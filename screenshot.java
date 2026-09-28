import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.WritableImage;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.io.File;
import java.net.URL;

public class screenshot extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        com.mindmap.database.DatabaseInitializer.initialize();
        URL fxmlUrl = getClass().getResource("/fxml/main.fxml");
        Parent root = FXMLLoader.load(fxmlUrl);
        Scene scene = new Scene(root, 1100, 720);
        scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
        stage.setScene(scene);
        stage.show();

        // navigate to quiz
        com.mindmap.controller.MainController controller = (com.mindmap.controller.MainController) scene.lookup("#contentArea").getParent().getUserData(); // wait, no userData
        // let's just trigger it via button
        javafx.scene.control.Button btnQuiz = (javafx.scene.control.Button) scene.lookup("#btnQuiz");
        btnQuiz.fire();

        Platform.runLater(() -> {
            try {
                Thread.sleep(1000); // let it render
                Platform.runLater(() -> {
                    WritableImage img = new WritableImage(1100, 720);
                    scene.snapshot(img);
                    try {
                        ImageIO.write(SwingFXUtils.fromFXImage(img, null), "png", new File("screenshot_out.png"));
                    } catch (Exception e) {}
                    Platform.exit();
                });
            } catch (Exception e) {}
        });
    }
    public static void main(String[] args) {
        launch(args);
    }
}
