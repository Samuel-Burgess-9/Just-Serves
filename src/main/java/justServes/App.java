package justServes; /**
 * justServes.App entry point
 */

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class App extends Application {
    @Override
    public void start(Stage stage) {
        stage.setScene(new Scene(new Label("Just Serves"), 400, 300));
        stage.setTitle("Just Serves");
        stage.show();
    }
}