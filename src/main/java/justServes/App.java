package justServes; /**
 * justServes.App entry point
 */

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import justServes.repo.Database;

public class App extends Application {
    private Database database;
    @Override
    public void start(Stage stage) {
        database = Database.forUser();
        stage.setScene(new Scene(new Label("Just Serves"), 400, 300));
        stage.setTitle("Just Serves");
        stage.show();
    }

    @Override
    public void stop() {
        database.close();
    }
}