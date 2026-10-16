package justServes; /**
 * justServes.App entry point
 */

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import justServes.repo.Database;

import java.io.IOException;

public class App extends Application {
    private Database database;
    @Override
    public void start(Stage stage) throws IOException {
        database = Database.forUser();
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/shell.fxml")
        );
        Parent root = loader.load();

        stage.setScene(new Scene(root));
        stage.setTitle("Just Serves");
        stage.show();
    }

    @Override
    public void stop() {
        database.close();
    }
}