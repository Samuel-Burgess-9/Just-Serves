package justServes.gui.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;

import java.io.IOException;

public class ShellController {
    @FXML private StackPane contentHolder;
    @FXML private ToggleGroup navGroup;
    @FXML private ToggleButton recordNav, analyzeNav, statsNav;
    @FXML private Button exitButton;

    @FXML
    private void initialize() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                            "/fxml/record-screen.fxml"
                    )
            );

            Parent recordScreen = loader.load();
            contentHolder.getChildren().setAll(recordScreen);

        } catch (IOException e) {
            throw new RuntimeException(
                    "Failed to load the record screen", e
            );
        }
    }
}
