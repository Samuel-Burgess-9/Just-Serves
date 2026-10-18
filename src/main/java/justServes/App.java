package justServes; /**
 * justServes.App entry point
 */

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import justServes.gui.controllers.ShellController;
import justServes.gui.theme.AppThemes;
import justServes.repo.Database;
import justServes.settings.SettingsStore;

import java.io.IOException;
import java.util.Objects;

import static justServes.AppConstants.*;

public class App extends Application {
    private AppState state;
    @Override
    public void start(Stage stage) throws IOException {
        state = new AppState(Database.forUser(), SettingsStore.forUser());
        state.getGameService().resumeOrStartGame();
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/shell.fxml")
        );
        Parent root = loader.load();
        ShellController shell = loader.getController();
        shell.init(state);

        Image logo = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/logo.png")));
        stage.getIcons().add(logo);
        stage.setScene(new Scene(root));
        stage.setMinHeight(MIN_WINDOW_HEIGHT);
        stage.setMinWidth(MIN_WINDOW_WIDTH);
        stage.setTitle("Just Serves");
        stage.show();
    }

    @Override
    public void stop() {
        if (state != null) {
            state.close();
        }
    }
}