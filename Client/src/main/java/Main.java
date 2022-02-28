import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class Main extends Application {
    @Override public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/sample.fxml"));
        primaryStage.setTitle("Chat");
        primaryStage.setScene(new Scene(root,450,275));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
