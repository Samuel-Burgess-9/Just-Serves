package justServes.gui.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;

public class RecordScreenController {
    @FXML private Label hintLabel, gameStatsLabel, lastServeOutcomeLabel, lastServeDetailLabel;
    @FXML private Button newGameButton, undoButton;
    @FXML private StackPane courtHolder;
    @FXML private ToggleGroup serveTypeGroup, receiverGroup, outcomeGroup;
    @FXML private ToggleButton topspinToggle, floatToggle, leftSpinToggle, rightSpinToggle;
    @FXML private ToggleButton aceToggle, shakyToggle, returnedToggle, outToggle;

    @FXML
    private void initialize() { }
}
