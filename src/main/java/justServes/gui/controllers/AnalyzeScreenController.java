
package justServes.gui.controllers;

import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import justServes.AppState;
import justServes.gui.court.CourtView;
import justServes.gui.court.DisplayMode;
import justServes.models.Game;
import justServes.models.Serve;
import justServes.models.ServeType;
import justServes.services.*;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;

public class AnalyzeScreenController implements ScreenController {

    @FXML private ListView<GameSummary> gameList;
    @FXML private Label gameTitleLabel, hintLabel, scopeTitleLabel, scopeDetailLabel;
    @FXML private StackPane courtHolder;
    @FXML private Button clearZoneButton;
    @FXML private ToggleButton allToggle, topspinToggle, floatToggle,
            leftSpinToggle, rightSpinToggle;
    @FXML private ProgressBar aceBar, shakyBar, returnedBar, outBar;
    @FXML private Label aceValueLabel, shakyValueLabel,
            returnedValueLabel, outValueLabel;

    private final StatsService statsService = new StatsService();
    private final SectorService sectorService = new SectorService();

    private AppState state;
    private CourtView courtView;
    private Game selectedGame;
    private List<Serve> selectedServes = List.of();
    private Sector selectedSector;
    private ServeType selectedServeType;

    private boolean updatingSelection;

    private static final DateTimeFormatter GAME_DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")
                    .withZone(ZoneId.systemDefault());

    @FXML
    private void initialize() {
        configureServeTypeToggles();
        configureGameList();

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

        state.themeProperty().addListener((observable, oldTheme, newTheme) -> {
            if (courtView != null) {
                courtView.setTheme(newTheme.court());
            }
        });
    }

    @Override
    public void onShow() {
        reloadGames();
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

    private String formatPercentage(OptionalDouble value) {
        if (value.isEmpty()) {
            return "-";
        }
        return String.format("%.0f%%", value.getAsDouble() * 100);
    }

    private void configureGameList() {
        gameList.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(GameSummary summary, boolean empty) {
                super.updateItem(summary, empty);

                if (empty || summary == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                String date = summary.game().getStartedAt() == null
                        ? "Unknown date"
                        : GAME_DATE_FORMAT.format(summary.game().getStartedAt());

                setText(date
                        + "\n" + "Accuracy: " + formatPercentage(summary.accuracy()) + " | Ace rate: " + formatPercentage(summary.aceRate()));
            }
        });

        gameList.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldSummary, newSummary) -> {
                    if (newSummary == null) {
                        selectedGame = null;
                        selectedServes = List.of();
                        selectedSector = null;
                        refreshAnalysis();
                        return;
                    }

                    selectedGame = newSummary.game();
                    selectedServes = state.getGameService().servesFor(selectedGame);
                    selectedSector = null;

                    refreshAnalysis();
                });
    }

    private void reloadGames() {
        if (state == null) {
            return;
        }

        Game previouslySelected = selectedGame;

        List<GameSummary> summaries = state.getGameService().listGames()
                .stream()
                .map(game -> GameSummary.of(
                        game,
                        state.getGameService().servesFor(game)))
                .toList();

        gameList.getItems().setAll(summaries);

        GameSummary toSelect = summaries.stream()
                .filter(summary -> previouslySelected != null
                        && summary.game().getId() == previouslySelected.getId())
                .findFirst()
                .orElse(summaries.isEmpty() ? null : summaries.getFirst());

        if (toSelect == null) {
            gameList.getSelectionModel().clearSelection();
            selectedGame = null;
            selectedServes = List.of();
            selectedSector = null;
            refreshAnalysis();
        } else {
            gameList.getSelectionModel().select(toSelect);
        }
    }

    private void handleCourtClick(justServes.util.CourtPoint point) {
        if (selectedGame == null) {
            return;
        }

        try {
            selectedSector = sectorService.sectorOf(
                    point.x(),
                    point.y(),
                    justServes.AppConstants.DEFAULT_RECEIVER_COUNT
            );
            refreshAnalysis();
        } catch (IllegalArgumentException exception) {
            hintLabel.setText("That point is outside the court.");
        }
    }

    private void refreshAnalysis() {
        if (courtView == null) {
            return;
        }
        boolean hasGame = selectedGame != null;
        clearZoneButton.setDisable(!hasGame || selectedSector == null);
        if (!hasGame) {
            courtView.setHeatmapCounts(null);
            courtView.setServes(List.of(), justServes.AppConstants.DEFAULT_RECEIVER_COUNT);
            gameTitleLabel.setText("No game selected");
            setEmptyState();
            return;
        }

        String date = selectedGame.getStartedAt() == null
                ? "Unknown date"
                : GAME_DATE_FORMAT.format(selectedGame.getStartedAt());
        gameTitleLabel.setText("Game · " + date);
        Map<Sector, Integer> counts =
                statsService.sectorCounts(selectedServes, selectedServeType);
        courtView.setHeatmapCounts(counts);

        int receiverCount = selectedServes.isEmpty()
                ? justServes.AppConstants.DEFAULT_RECEIVER_COUNT
                : selectedServes.getLast().getReceiverCount();

        courtView.setServes(List.of(), receiverCount);

        StatsFilter filter = new StatsFilter(selectedSector, selectedServeType);
        OutcomeStats outcomes = statsService.outcomes(selectedServes, filter);

        updateOutcome(aceBar, aceValueLabel, outcomes, justServes.models.Outcome.ACE);
        updateOutcome(shakyBar, shakyValueLabel, outcomes, justServes.models.Outcome.SHAKY);
        updateOutcome(returnedBar, returnedValueLabel, outcomes, justServes.models.Outcome.RETURNED);
        updateOutcome(outBar, outValueLabel, outcomes, justServes.models.Outcome.OUT);

        scopeTitleLabel.setText(selectedSector == null
                ? "Whole court"
                : formatSector(selectedSector));

        scopeDetailLabel.setText(outcomes.total() + " matching serves");

        hintLabel.setText(selectedSector == null
                ? "Shading = number of serves · click a zone to focus it"
                : "Focused on " + formatSector(selectedSector)
                  + " · click another zone to change focus");
    }

    private void updateOutcome(
            ProgressBar bar,
            Label valueLabel,
            OutcomeStats stats,
            justServes.models.Outcome outcome) {

        bar.setProgress(stats.share(outcome));
        valueLabel.setText(stats.count(outcome) + "  ·  "
                + Math.round(stats.share(outcome) * 100) + "%");
    }

    private void setEmptyState() {
        gameTitleLabel.setText("No game selected");
        hintLabel.setText("Select a game to explore serve placement");
        scopeTitleLabel.setText("Whole court");
        scopeDetailLabel.setText("No serves");

        for (ProgressBar bar : List.of(aceBar, shakyBar, returnedBar, outBar)) {
            bar.setProgress(0);
        }

        for (Label label : List.of(
                aceValueLabel, shakyValueLabel, returnedValueLabel, outValueLabel)) {
            label.setText("0  ·  0%");
        }
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
}

