package justServes.gui.controllers;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import justServes.AppConstants;
import justServes.AppState;
import justServes.gui.court.*;
import justServes.gui.theme.Palette;
import justServes.models.Game;
import justServes.models.Outcome;
import justServes.models.Serve;
import justServes.models.ServeType;
import justServes.services.*;
import justServes.util.CourtPoint;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.OptionalDouble;

public class AnalyzeScreenController implements ScreenController {

    @FXML private HBox receiverBox;
    private Integer selectedReceiverCount;
    @FXML private ListView<GameSummary> gameList;
    @FXML private Label gameTitleLabel, hintLabel, scopeTitleLabel, scopeDetailLabel;
    @FXML private StackPane courtHolder;
    @FXML private Button clearZoneButton;
    @FXML private ToggleButton allToggle, topspinToggle, floatToggle,
            leftSpinToggle, rightSpinToggle;
    @FXML private ProgressBar aceBar, shakyBar, returnedBar, outBar;
    @FXML private Label aceValueLabel, shakyValueLabel,
            returnedValueLabel, outValueLabel;
    @FXML private Region legendBar;

    /** Every receiver count shares one layout today. If layouts ever differ, analysis needs a receiver-count filter. */
    private static final int LAYOUT_RECEIVER_COUNT = AppConstants.DEFAULT_RECEIVER_COUNT;

    private static final DateTimeFormatter CARD_DATE_FORMAT =
            DateTimeFormatter.ofPattern("d MMM, HH:mm").withZone(ZoneId.systemDefault());

    private static final DateTimeFormatter GAME_DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm").withZone(ZoneId.systemDefault());

    private final StatsService statsService = new StatsService();
    private final SectorService sectorService = new SectorService();

    private AppState state;
    private CourtView courtView;
    private Game selectedGame;
    private List<Serve> selectedServes = List.of();
    private Sector selectedSector;
    private ServeType selectedServeType;
    private boolean updatingSelection;
    private boolean reloading;

    // ---------- setup ----------

    @FXML
    private void initialize() {
        configureServeTypeToggles();
        configureGameList();

        colour(aceBar, Outcome.ACE);
        colour(shakyBar, Outcome.SHAKY);
        colour(returnedBar, Outcome.RETURNED);
        colour(outBar, Outcome.OUT);
        configureReceiverToggles();

        clearZoneButton.setOnAction(event -> {
            selectedSector = null;
            refreshAnalysis();
        });
        clearZoneButton.setDisable(true);

        setEmptyState();
    }

    @Override
    public void init(AppState state) {
        this.state = state;

        courtView = new CourtView();
        courtView.setDisplayMode(DisplayMode.ANALYZE);
        courtView.setTheme(state.getTheme().court());
        courtView.setOnCourtClicked(this::handleCourtClick);
        courtHolder.getChildren().setAll(courtView);

        applyLegend(state.getTheme().court());
        state.themeProperty().addListener((obs, oldTheme, newTheme) -> {
            courtView.setTheme(newTheme.court());
            applyLegend(newTheme.court());
        });
    }

    @Override
    public void onShow() {
        reloadGames();
    }

    private void configureReceiverToggles() {
        ToggleGroup group = new ToggleGroup();
        for (Node node : receiverBox.getChildren()) {
            ToggleButton button = (ToggleButton) node;
            button.setToggleGroup(group);
            button.setUserData("All".equals(button.getText()) ? null : Integer.valueOf(button.getText()));
        }
        group.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle == null) {                       // clicking the selected one must not leave it empty
                if (oldToggle != null) {
                    group.selectToggle(oldToggle);
                }
                return;
            }
            selectedReceiverCount = (Integer) newToggle.getUserData();
            selectedSector = null;                         // zones differ between layouts
            refreshAnalysis();
        });
    }

    private static void colour(ProgressBar bar, Outcome outcome) {
        bar.setStyle("-fx-accent: " + Palette.hex(OutcomeColours.of(outcome)) + ";");
    }

    private void applyLegend(CourtTheme court) {
        legendBar.setStyle("-fx-background-radius: 5; -fx-background-color: linear-gradient(to right, "
                + Palette.hex(HeatScale.low(court)) + ", " + Palette.hex(HeatScale.high(court)) + ");");
    }

    private void configureServeTypeToggles() {
        ToggleGroup group = new ToggleGroup();

        allToggle.setToggleGroup(group);
        topspinToggle.setToggleGroup(group);
        floatToggle.setToggleGroup(group);
        leftSpinToggle.setToggleGroup(group);
        rightSpinToggle.setToggleGroup(group);

        allToggle.setUserData(null);
        topspinToggle.setUserData(ServeType.TOPSPIN);
        floatToggle.setUserData(ServeType.FLOAT);
        leftSpinToggle.setUserData(ServeType.LEFT_SPIN);
        rightSpinToggle.setUserData(ServeType.RIGHT_SPIN);

        allToggle.setSelected(true);

        group.selectedToggleProperty().addListener((observable, oldToggle, newToggle) -> {
            if (updatingSelection) {
                return;
            }

            if (newToggle == null) {
                updatingSelection = true;
                allToggle.setSelected(true);
                updatingSelection = false;
                selectedServeType = null;
            } else {
                selectedServeType = (ServeType) newToggle.getUserData();
            }

            refreshAnalysis();
        });
    }

    private void configureGameList() {
        gameList.setPlaceholder(new Label("No games yet"));

        gameList.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(GameSummary summary, boolean empty) {
                super.updateItem(summary, empty);
                setText(null);
                if (empty || summary == null) {
                    setGraphic(null);
                    return;
                }

                Label title = new Label("Game " + summary.game().getId());
                title.getStyleClass().add("game-card-title");

                String date = summary.game().getStartedAt() == null
                        ? "Unknown date"
                        : CARD_DATE_FORMAT.format(summary.game().getStartedAt());
                Label detail = new Label(date + " · " + summary.serves()
                        + (summary.serves() == 1 ? " serve" : " serves"));
                detail.getStyleClass().add("game-card-detail");

                Label stats = new Label("Accuracy " + formatPercentage(summary.accuracy())
                        + " · Ace rate " + formatPercentage(summary.aceRate()));
                stats.getStyleClass().add("game-card-stats");

                VBox card = new VBox(2, title, detail, stats);
                card.getStyleClass().add("game-card");
                setGraphic(card);
            }
        });

        gameList.getSelectionModel().selectedItemProperty().addListener((obs, oldSummary, newSummary) -> {
            if (!reloading) {
                applySelection(newSummary, false);
            }
        });
    }

    // ---------- games and selection ----------

    private void reloadGames() {
        if (state == null) {
            return;
        }
        int previousId = selectedGame == null ? -1 : selectedGame.getId();
        GameService service = state.getGameService();

        List<GameSummary> summaries = service.listGames().stream()
                .map(game -> GameSummary.of(game, service.servesFor(game)))
                .toList();

        GameSummary toSelect = summaries.stream()
                .filter(s -> s.game().getId() == previousId)
                .findFirst()
                .orElse(summaries.isEmpty() ? null : summaries.getFirst());

        reloading = true;   // the selection listener stays quiet while the list is rebuilt
        try {
            gameList.getItems().setAll(summaries);
            if (toSelect == null) {
                gameList.getSelectionModel().clearSelection();
            } else {
                gameList.getSelectionModel().select(toSelect);
            }
        } finally {
            reloading = false;
        }
        applySelection(toSelect, toSelect != null && toSelect.game().getId() == previousId);
    }

    private void applySelection(GameSummary summary, boolean keepSector) {
        if (summary == null) {
            selectedGame = null;
            selectedServes = List.of();
            selectedSector = null;
        } else {
            selectedGame = summary.game();
            selectedServes = state.getGameService().servesFor(selectedGame);
            if (!keepSector) {
                selectedSector = null;
            }
        }
        refreshAnalysis();
    }

    private void handleCourtClick(CourtPoint point) {
        if (selectedGame == null) {
            return;
        }
        Sector clicked = sectorService.sectorOf(point.x(), point.y(), currentFilter().layout());
        selectedSector = clicked == selectedSector ? null : clicked;   // clicking the selected zone clears it
        refreshAnalysis();
    }

    // ---------- refreshing the screen ----------

    private void refreshAnalysis() {
        if (courtView == null) {
            return;
        }

        boolean hasGame = selectedGame != null;
        clearZoneButton.setDisable(!hasGame || selectedSector == null);

        if (!hasGame) {
            courtView.setHeatmapCounts(null);
            courtView.setSelectedSector(null);
            setEmptyState();
            return;
        }

        String date = selectedGame.getStartedAt() == null
                ? "Unknown date"
                : GAME_DATE_FORMAT.format(selectedGame.getStartedAt());
        gameTitleLabel.setText("Game " + selectedGame.getId() + " · " + date);

        StatsFilter filter = currentFilter();
        courtView.setZones(filter.layout());
        courtView.setHeatmapCounts(statsService.sectorCounts(selectedServes, filter));
        courtView.setSelectedSector(selectedSector);
        OutcomeStats outcomes = statsService.outcomes(selectedServes, filter);

        int inView = statsService.outcomes(selectedServes, filter.withSector(null)).total();
        String receivers = selectedReceiverCount == null ? ""
                : " · " + selectedReceiverCount + (selectedReceiverCount == 1 ? " receiver" : " receivers");
        scopeDetailLabel.setText((selectedSector == null
                ? inView + (inView == 1 ? " serve" : " serves")
                : outcomes.total() + " of " + inView + " serves") + receivers);

        updateOutcome(aceBar, aceValueLabel, outcomes, Outcome.ACE);
        updateOutcome(shakyBar, shakyValueLabel, outcomes, Outcome.SHAKY);
        updateOutcome(returnedBar, returnedValueLabel, outcomes, Outcome.RETURNED);
        updateOutcome(outBar, outValueLabel, outcomes, Outcome.OUT);

        scopeTitleLabel.setText(selectedSector == null ? "Whole court" : formatSector(selectedSector));

        hintLabel.setText(selectedSector == null
                ? "Click a zone to focus it"
                : "Focused on " + formatSector(selectedSector) + " · click it again to clear");
    }

    private void updateOutcome(ProgressBar bar, Label valueLabel, OutcomeStats stats, Outcome outcome) {
        bar.setProgress(stats.share(outcome));
        valueLabel.setText(stats.count(outcome) + "  ·  " + Math.round(stats.share(outcome) * 100) + "%");
    }

    private void setEmptyState() {
        gameTitleLabel.setText("No game selected");
        hintLabel.setText("Select a game to explore serve placement");
        scopeTitleLabel.setText("Whole court");
        scopeDetailLabel.setText("No serves");

        for (ProgressBar bar : List.of(aceBar, shakyBar, returnedBar, outBar)) {
            bar.setProgress(0);
        }
        for (Label label : List.of(aceValueLabel, shakyValueLabel, returnedValueLabel, outValueLabel)) {
            label.setText("0  ·  0%");
        }
    }

    // ---------- formatting ----------

    private String formatPercentage(OptionalDouble value) {
        if (value.isEmpty()) {
            return "-";
        }
        return String.format("%.0f%%", value.getAsDouble() * 100);
    }

    private String formatSector(Sector sector) {
        String[] words = sector.name().toLowerCase().split("_");
        StringBuilder result = new StringBuilder();

        for (String word : words) {
            if (!result.isEmpty()) {
                result.append(' ');
            }
            result.append(Character.toUpperCase(word.charAt(0)))
                    .append(word.substring(1));
        }

        return result.toString();
    }

    private StatsFilter currentFilter() {
        return new StatsFilter(selectedSector, selectedServeType, selectedReceiverCount);
    }
}