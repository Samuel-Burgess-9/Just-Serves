package justServes.gui.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import justServes.models.Outcome;
import justServes.models.ServeType;

import java.util.LinkedHashMap;
import java.util.Map;

public class RecordScreenController {
    public HBox receiverBox;
    @FXML private Label hintLabel, gameStatsLabel, lastServeOutcomeLabel, lastServeDetailLabel;
    @FXML private Button newGameButton, undoButton;
    @FXML private StackPane courtHolder;
    @FXML private ToggleButton topspinToggle, floatToggle, leftSpinToggle, rightSpinToggle;
    @FXML private ToggleButton aceToggle, shakyToggle, returnedToggle, outToggle;

    private final ToggleGroup serveTypeGroup = new ToggleGroup();
    private final ToggleGroup receiverGroup = new ToggleGroup();
    private final ToggleGroup outcomeGroup = new ToggleGroup();

    private final Map<ToggleButton, ServeType> serveTypes = new LinkedHashMap<>();
    private final Map<ToggleButton, Outcome> outcomes = new LinkedHashMap<>();

    @FXML
    private void initialize() { }
}
