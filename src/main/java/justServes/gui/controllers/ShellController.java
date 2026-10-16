package justServes.gui.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import justServes.AppState;

import java.io.IOException;
import java.io.UncheckedIOException;

public class ShellController {
    public BorderPane root;
    @FXML private StackPane contentHolder;
    @FXML private ToggleGroup navGroup;
    @FXML private ToggleButton recordNav, analyzeNav, statsNav;
    @FXML private Button exitButton;
    private AppState state;

    @FXML
    private void initialize() {

    }

    public void init(AppState state) {
        this.state = state;
        state.themeProperty().addListener((obs, old, t) -> root.setStyle(t.palette().toStyle()));
        root.setStyle(state.getTheme().palette().toStyle());
        showRecordScreen();
    }

    private void showRecordScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/record-screen.fxml"));
            Parent screen = loader.load();
            RecordScreenController controller = loader.getController();
            controller.init(state);
            contentHolder.getChildren().setAll(screen);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to load the record screen", e);
        }
    }
}
