package justServes.gui.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;

public class ShellController {
    @FXML private StackPane contentHolder;
    @FXML private ToggleGroup navGroup;
    @FXML private ToggleButton recordNav, analyzeNav, statsNav;
    @FXML private Button exitButton;

    @FXML
    private void initialize() { }
}
