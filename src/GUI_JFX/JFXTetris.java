package GUI_JFX;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class JFXTetris extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("Tetris.fxml"));
        Scene scene = new Scene(root);

        primaryStage.setTitle("Tetris");
        primaryStage.setScene(scene);
        primaryStage.show();

        primaryStage.setOnCloseRequest(event -> {
            Platform.exit();
            System.exit(0);
        });

    }

    public static void main(String[] args) {
        try {
            launch(args);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
