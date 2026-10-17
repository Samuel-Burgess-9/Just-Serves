package justServes.gui.controllers;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import justServes.AppConstants;
import justServes.AppState;
import justServes.gui.court.CourtView;
import justServes.gui.court.OutcomeColours;
import justServes.models.Game;
import justServes.models.Outcome;
import justServes.models.Serve;
import justServes.models.ServeType;
import justServes.repo.RepositoryException;
import justServes.services.GameService;
import justServes.services.ReceiverLayouts;
import justServes.services.SectorService;
import justServes.util.CourtPoint;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static justServes.gui.controllers.TextHelper.pretty;

public class RecordScreenController implements ScreenController {
    public CheckBox showZonesCheckBox;
    @FXML private HBox receiverBox;
    @FXML private Button recordButton;
    @FXML private Label hintLabel, gameStatsLabel, lastServeOutcomeLabel, lastServeDetailLabel;
    @FXML private Button newGameButton, undoButton;
    @FXML private StackPane courtHolder;
    @FXML private ToggleButton topspinToggle, floatToggle, leftSpinToggle, rightSpinToggle;
    @FXML private ToggleButton aceToggle, shakyToggle, returnedToggle, outToggle;
    private record RecentRow(int number, Outcome outcome, String sector) { }

    @FXML private ListView<RecentRow> recentList;

    private final ToggleGroup serveTypeGroup = new ToggleGroup();
    private final ToggleGroup receiverGroup = new ToggleGroup();
    private final ToggleGroup outcomeGroup = new ToggleGroup();

    private final Map<ToggleButton, ServeType> serveTypes = new LinkedHashMap<>();
    private final Map<ToggleButton, Outcome> outcomes = new LinkedHashMap<>();

    private CourtView courtView;
    private GameService gameService;
    private final SectorService sectorService = new SectorService();
    private CourtPoint pendingAim;

    @FXML
    private void initialize() {
        serveTypes.put(topspinToggle, ServeType.TOPSPIN);
        serveTypes.put(floatToggle, ServeType.FLOAT);
        serveTypes.put(leftSpinToggle, ServeType.LEFT_SPIN);
        serveTypes.put(rightSpinToggle, ServeType.RIGHT_SPIN);
        serveTypes.keySet().forEach(t -> t.setToggleGroup(serveTypeGroup));

        outcomes.put(aceToggle, Outcome.ACE);
        outcomes.put(shakyToggle, Outcome.SHAKY);
        outcomes.put(returnedToggle, Outcome.RETURNED);
        outcomes.put(outToggle, Outcome.OUT);
        outcomes.keySet().forEach(t -> t.setToggleGroup(outcomeGroup));

        for (Node n : receiverBox.getChildren()) {
            ((ToggleButton) n).setToggleGroup(receiverGroup);
        }

        requireSelection(serveTypeGroup);
        requireSelection(outcomeGroup);
        requireSelection(receiverGroup);

        courtView = new CourtView();
        courtView.setOnCourtClicked(this::onCourtClicked);
        courtHolder.getChildren().add(courtView);

        outcomeGroup.selectedToggleProperty().addListener((obs, oldT, newT) -> refreshControls());
        receiverGroup.selectedToggleProperty().addListener((obs, oldT, newT) -> {
            if (gameService != null) {
                refreshView();   // zone lines depend on the receiver count
            }
        });

        showZonesCheckBox.selectedProperty().addListener(
                (obs, wasSelected, isSelected) ->
                        courtView.setShowZones(isSelected)
        );

        recentList.setPlaceholder(new Label());
        recentList.setCellFactory(lv -> new ListCell<>() {
            {
                setMouseTransparent(true);   // display only: no selection, wheel scrolling still works
            }

            @Override
            protected void updateItem(RecentRow row, boolean empty) {
                super.updateItem(row, empty);
                if (empty || row == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText("#" + row.number() + "  " + pretty(row.outcome()) + " · " + row.sector());
                    setGraphic(new Circle(5, OutcomeColours.of(row.outcome())));   // javafx.scene.shape.Circle
                }
            }
        });

        recordButton.setOnAction(e -> recordServe());
        undoButton.setOnAction(e -> undoLastServe());
        newGameButton.setOnAction(e -> startNewGame());
    }

    public void init(AppState state) {
        this.gameService = state.getGameService();
        courtView.setTheme(state.getTheme().court());
        state.themeProperty().addListener((obs, old, t) -> courtView.setTheme(t.court()));
        refreshView();
    }

    private void requireSelection(ToggleGroup group) {
        group.selectedToggleProperty().addListener((obs, oldT, newT) -> {
            if (newT == null && oldT != null) {
                group.selectToggle(oldT);
            }
        });
    }

    private void startNewGame() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Start a new game? The current game stays saved.",
                ButtonType.OK, ButtonType.CANCEL);
        confirm.setHeaderText(null);
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }
        try {
            gameService.startGame();
        } catch (RepositoryException e) {
            showError("Could not start a new game", e.getMessage());
            return;
        }
        clearPending();
        refreshView();
    }

    private void clearPending() {
        pendingAim = null;
        courtView.setPendingAim(null);
        outcomeGroup.selectToggle(null);   // fires the listener, which refreshes the controls
        refreshControls();
    }

    private void undoLastServe() {
        try {
            gameService.undoLastServe();
        } catch (RepositoryException e) {
            showError("Could not undo the serve", e.getMessage());
            return;
        }
        refreshView();
    }

    private void recordServe() {
        Outcome outcome = selectedOutcome();
        if (pendingAim == null || outcome == null) {
            return;   // the button is disabled, but the Enter key must not get past this
        }
        try {
            gameService.logServe(pendingAim.x(), pendingAim.y(), outcome,
                    selectedReceiverCount(), selectedServeType());
        } catch (RepositoryException | IllegalArgumentException e) {
            showError("Could not record the serve", e.getMessage());
            return;   // keep the aim and outcome so nothing has to be re-entered
        }
        clearPending();
        refreshView();
    }

    private int selectedReceiverCount() {
        return Integer.parseInt(((ToggleButton) receiverGroup.getSelectedToggle()).getText());
    }

    private Outcome selectedOutcome() {
        Toggle selected = outcomeGroup.getSelectedToggle();
        return selected == null ? null : outcomes.get((ToggleButton) selected);
    }

    private ServeType selectedServeType() {
        return serveTypes.get((ToggleButton) serveTypeGroup.getSelectedToggle());
    }

    private void selectServeType(ServeType type) {
        serveTypes.forEach((button, t) -> {
            if (t == type) {
                serveTypeGroup.selectToggle(button);
            }
        });
    }

    private void selectReceiverCount(int count) {
        for (Node n : receiverBox.getChildren()) {
            ToggleButton button = (ToggleButton) n;
            if (Integer.parseInt(button.getText()) == count) {
                receiverGroup.selectToggle(button);
            }
        }
    }

    private void refreshView() {
        Game game = gameService.getCurrentGame();
        List<Serve> serves = game.getServes();

        int receivers = selectedReceiverCount();
        courtView.setZones(ReceiverLayouts.layoutFor(receivers));
        courtView.setServes(serves.stream().filter(s -> s.getReceiverCount() == receivers).toList());

        StringBuilder stats = new StringBuilder(
                serves.size() + (serves.size() == 1 ? " serve" : " serves"));
        for (Outcome o : Outcome.values()) {
            long count = serves.stream().filter(s -> s.getOutcome() == o).count();
            stats.append(" · ").append(pretty(o)).append(' ').append(count);
        }
        gameStatsLabel.setText(stats.toString());

        undoButton.setDisable(serves.isEmpty());
        if (serves.isEmpty()) {
            lastServeOutcomeLabel.setText("No serves yet");
            lastServeDetailLabel.setText("");
        } else {
            Serve last = serves.get(serves.size() - 1);
            int lastReceivers = last.getReceiverCount();
            lastServeOutcomeLabel.setText(pretty(last.getOutcome()));
            lastServeDetailLabel.setText(pretty(last.getType()) + " · "
                    + (lastReceivers == 1 ? "1 receiver" : lastReceivers + " receivers") + " · "
                    + pretty(sectorService.sectorOf(last.getAimX(), last.getAimY(), lastReceivers)));
        }

        List<RecentRow> rows = new ArrayList<>();
        int oldest = Math.max(0, serves.size() - AppConstants.RECENT_SERVES_SHOWN);
        for (int i = serves.size() - 1; i >= oldest; i--) {
            Serve s = serves.get(i);
            rows.add(new RecentRow(i + 1, s.getOutcome(),
                    pretty(sectorService.sectorOf(s.getAimX(), s.getAimY(), s.getReceiverCount()))));
        }
        recentList.getItems().setAll(rows);

        newGameButton.setDisable(serves.isEmpty());
    }

    private void refreshControls() {
        boolean hasAim = pendingAim != null;
        boolean hasOutcome = selectedOutcome() != null;
        recordButton.setDisable(!(hasAim && hasOutcome));
        hintLabel.setText(!hasAim ? "Click the court to set the aim"
                : !hasOutcome ? "Pick an outcome"
                  : "Press Record serve");
    }

    private void onCourtClicked(CourtPoint point) {
        pendingAim = point;
        refreshControls();
    }

    private void showError(String header, String detail) {
        Alert alert = new Alert(Alert.AlertType.ERROR, detail);
        alert.setHeaderText(header);
        alert.showAndWait();
    }

    @Override
    public void onShow() {
        refreshView();
    }
}
