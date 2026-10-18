
package justServes.gui.controllers;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import justServes.AppState;
import justServes.gui.court.*;
import justServes.gui.theme.Palette;
import justServes.models.Game;
import justServes.models.Outcome;
import justServes.models.Serve;
import justServes.models.ServeType;
import justServes.services.*;
import justServes.util.CourtPoint;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.OptionalDouble;

public class ServingStatsScreenController implements ScreenController {

    // FXML controls
    @FXML private Label matchesAnalysedLabel;
    @FXML private Label totalServesLabel;
    @FXML private Label accuracyLabel;
    @FXML private Label aceRateLabel;
    @FXML private Label outRateLabel;

    @FXML private Label gameTitleLabel;
    @FXML private Label hintLabel;
    @FXML private StackPane courtHolder;

    @FXML private GridPane datasetBox;
    @FXML private ToggleButton allToggle, topspinToggle, floatToggle, leftSpinToggle, rightSpinToggle;
    @FXML private HBox receiverBox;
    @FXML private FlowPane outcomeFilterBox;
    @FXML private Region legendBar;

    @FXML private ProgressBar aceBar;
    @FXML private ProgressBar shakyBar;
    @FXML private ProgressBar returnedBar;
    @FXML private ProgressBar outBar;

    @FXML private Label aceBarLabel;
    @FXML private Label shakyBarLabel;
    @FXML private Label returnedBarLabel;
    @FXML private Label outBarLabel;

    @FXML private Button clearFiltersButton;

    // Services and application state
    private final StatsService statsService = new StatsService();
    private final SectorService sectorService = new SectorService();

    private AppState state;
    private CourtView courtView;

    // Current dataset
    private List<Game> selectedGames = List.of();
    private List<Serve> datasetServes = List.of();

    // Current filters
    private ServeType selectedServeType;
    private Integer selectedReceiverCount;
    private Outcome selectedOutcome;
    private Sector selectedSector;

    private boolean updatingSelection;
    private boolean initialised;

    // --------------------------------------------------
    // Initialisation
    // --------------------------------------------------

    @FXML
    private void initialize() {
        configureServeTypeToggles();
        configureDatasetSelector();
        configureReceiverSelector();
        configureOutcomeSelector();

        colour(aceBar, Outcome.ACE);
        colour(shakyBar, Outcome.SHAKY);
        colour(returnedBar, Outcome.RETURNED);
        colour(outBar, Outcome.OUT);

        clearFiltersButton.setOnAction(event -> clearFilters());

        initialised = true;
        refreshAnalysis();
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

        reloadData();
    }

    @Override
    public void onShow() {
        reloadData();
    }

    // --------------------------------------------------
    // Filter setup
    // --------------------------------------------------

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

        group.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
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

            selectedSector = null;
            refreshAnalysis();
        });
    }

    private record Dataset(String name, int limit, boolean currentGameOnly) { }

    private Dataset dataset = new Dataset("All time", Integer.MAX_VALUE, false);
    private ToggleGroup datasetGroup, receiverGroup, outcomeGroup;

    private void configureDatasetSelector() {
        datasetGroup = new ToggleGroup();
        for (Node node : datasetBox.getChildren()) {
            ToggleButton button = (ToggleButton) node;
            button.setToggleGroup(datasetGroup);
            button.setUserData(switch (button.getText()) {
                case "Last 10" -> new Dataset("Last 10 games", 10, false);
                case "Last 5" -> new Dataset("Last 5 games", 5, false);
                case "Current Game" -> new Dataset("Current Game", 1, true);
                default -> new Dataset("All time", Integer.MAX_VALUE, false);
            });
        }
        datasetGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle == null) {                  // clicking the selected one must not leave it empty
                if (oldToggle != null) {
                    datasetGroup.selectToggle(oldToggle);
                }
                return;
            }
            dataset = (Dataset) newToggle.getUserData();
            selectedSector = null;
            updateDataset();
            refreshAnalysis();
        });
    }

    private void configureReceiverSelector() {
        receiverGroup = new ToggleGroup();
        for (Node node : receiverBox.getChildren()) {
            ToggleButton button = (ToggleButton) node;
            button.setToggleGroup(receiverGroup);
            button.setUserData("All".equals(button.getText()) ? null : Integer.valueOf(button.getText()));
        }
        receiverGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle == null) {
                if (oldToggle != null) {
                    receiverGroup.selectToggle(oldToggle);
                }
                return;
            }
            selectedReceiverCount = (Integer) newToggle.getUserData();
            selectedSector = null;                    // the zone may not exist in the new layout
            refreshAnalysis();
        });
    }

    private void configureOutcomeSelector() {
        outcomeGroup = new ToggleGroup();
        for (Node node : outcomeFilterBox.getChildren()) {
            ToggleButton button = (ToggleButton) node;
            button.setToggleGroup(outcomeGroup);
            button.setUserData(switch (button.getText()) {
                case "Ace" -> Outcome.ACE;
                case "Shaky" -> Outcome.SHAKY;
                case "Returned" -> Outcome.RETURNED;
                case "Out" -> Outcome.OUT;
                default -> null;                      // "All"
            });
        }
        outcomeGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle == null) {
                if (oldToggle != null) {
                    outcomeGroup.selectToggle(oldToggle);
                }
                return;
            }
            selectedOutcome = (Outcome) newToggle.getUserData();
            selectedSector = null;
            refreshAnalysis();
        });
    }

    // --------------------------------------------------
    // Dataset loading
    // --------------------------------------------------

    private void reloadData() {
        if (state == null) {
            return;
        }

        updateDataset();
        refreshAnalysis();
    }

    private void updateDataset() {
        if (state == null) {
            return;
        }

        selectedGames = dataset.currentGameOnly()
                ? List.of(state.getGameService().getCurrentGame())
                : newestGames(dataset.limit());

        List<Serve> serves = new ArrayList<>();
        for (Game game : selectedGames) {
            serves.addAll(state.getGameService().servesFor(game));
        }

        datasetServes = List.copyOf(serves);
        gameTitleLabel.setText("Serving Stats · " + dataset.name());

        updateOverview();
    }

    private List<Game> newestGames(int limit) {
        List<Game> games = new ArrayList<>(state.getGameService().listGames());

        // Newest games first. Fall back to ID when dates are equal.
        games.sort((a, b) -> {
            Instant aDate = a.getStartedAt();
            Instant bDate = b.getStartedAt();

            if (aDate == null && bDate != null) {
                return 1;
            }
            if (aDate != null && bDate == null) {
                return -1;
            }
            if (aDate != null) {
                int dateComparison = bDate.compareTo(aDate);
                if (dateComparison != 0) {
                    return dateComparison;
                }
            }

            return Integer.compare(b.getId(), a.getId());
        });

        return games.stream().limit(limit).toList();
    }

    private void updateOverview() {
        int total = datasetServes.size();

        int aces = countOutcome(datasetServes, Outcome.ACE);
        int outs = countOutcome(datasetServes, Outcome.OUT);

        matchesAnalysedLabel.setText(String.valueOf(selectedGames.size()));
        totalServesLabel.setText(String.valueOf(total));

        OptionalDouble accuracy = total == 0
                ? OptionalDouble.empty()
                : OptionalDouble.of((total - outs) / (double) total);

        OptionalDouble aceRate = total == 0
                ? OptionalDouble.empty()
                : OptionalDouble.of(aces / (double) total);

        OptionalDouble outRate = total == 0
                ? OptionalDouble.empty()
                : OptionalDouble.of(outs / (double) total);

        accuracyLabel.setText(formatPercentage(accuracy));
        aceRateLabel.setText(formatPercentage(aceRate));
        outRateLabel.setText(formatPercentage(outRate));

    }

    private int countOutcome(List<Serve> serves, Outcome outcome) {
        return (int) serves.stream()
                .filter(serve -> serve.getOutcome() == outcome)
                .count();
    }

    private void handleCourtClick(CourtPoint point) {
        if (courtView == null) {
            return;
        }

        StatsFilter filter = currentFilter();
        Sector clicked = sectorService.sectorOf(
                point.x(),
                point.y(),
                filter.layout()
        );

        // Clicking the focused zone again clears the focus.
        selectedSector = clicked == selectedSector ? null : clicked;

        refreshAnalysis();
    }

    private void refreshAnalysis() {
        if (!initialised) {
            return;
        }

        if (courtView == null) {
            setEmptyState();
            return;
        }

        StatsFilter filter = currentFilter();

        courtView.setZones(filter.layout());
        courtView.setSelectedSector(selectedSector);
        List<Serve> heatmapServes = datasetServes.stream()
                .filter(serve -> selectedOutcome == null
                        || serve.getOutcome() == selectedOutcome)
                .toList();

        courtView.setHeatmapCounts(
                statsService.sectorCounts(
                        heatmapServes,
                        filter.withSector(null)
                )
        );

        List<Serve> matchingServes =
                statsService.matching(datasetServes, filter);

        OutcomeStats outcomes = statsService.outcomes(
                datasetServes, filter
        );

        updateOutcome(aceBar, aceBarLabel, outcomes, Outcome.ACE);
        updateOutcome(shakyBar, shakyBarLabel, outcomes, Outcome.SHAKY);
        updateOutcome(returnedBar, returnedBarLabel, outcomes, Outcome.RETURNED);
        updateOutcome(outBar, outBarLabel, outcomes, Outcome.OUT);

        if (datasetServes.isEmpty()) {
            hintLabel.setText("No serves in this dataset yet.");
        } else if (selectedSector != null) {
            hintLabel.setText(
                    formatSector(selectedSector)
                            + " · " + matchingServes.size()
                            + " matching serves · click the zone again to clear"
            );
        } else if (selectedOutcome != null) {
            hintLabel.setText(
                    "Showing " + selectedOutcome.name().toLowerCase()
                            + " serve targeting · "
                            + heatmapServes.size() + " serves"
            );
        } else {
            hintLabel.setText(
                    "Click a zone to focus it · "
                            + datasetServes.size() + " serves in this dataset"
            );
        }
    }

    private void updateOutcome(
            ProgressBar bar,
            Label valueLabel,
            OutcomeStats stats,
            Outcome outcome
    ) {
        bar.setProgress(stats.share(outcome));

        if (stats.total() == 0) {
            valueLabel.setText("-");
        } else {
            valueLabel.setText(
                    stats.count(outcome)
                            + " · "
                            + Math.round(stats.share(outcome) * 100)
                            + "%"
            );
        }
    }

    private void setEmptyState() {
        gameTitleLabel.setText("Serving Stats");
        hintLabel.setText("Select a dataset to explore serve targeting.");

        for (ProgressBar bar : List.of(
                aceBar, shakyBar, returnedBar, outBar
        )) {
            bar.setProgress(0);
        }

        for (Label label : List.of(
                aceBarLabel, shakyBarLabel,
                returnedBarLabel, outBarLabel
        )) {
            label.setText("-");
        }
    }

    private void clearFilters() {
        allToggle.setSelected(true);
        receiverGroup.selectToggle((ToggleButton) receiverBox.getChildren().getFirst());
        outcomeGroup.selectToggle((ToggleButton) outcomeFilterBox.getChildren().getFirst());
        selectedSector = null;
        refreshAnalysis();
    }

    private static void colour(ProgressBar bar, Outcome outcome) {
        bar.setStyle(
                "-fx-accent: " + Palette.hex(OutcomeColours.of(outcome)) + ";"
        );
    }

    private void applyLegend(CourtTheme court) {
        legendBar.setStyle(
                "-fx-background-radius: 5; "
                        + "-fx-background-color: linear-gradient(to right, "
                        + Palette.hex(HeatScale.low(court)) + ", "
                        + Palette.hex(HeatScale.high(court)) + ");"
        );
    }

    private String formatPercentage(OptionalDouble value) {
        if (value.isEmpty()) {
            return "-";
        }

        return String.format(
                java.util.Locale.ROOT,
                "%.0f%%",
                value.getAsDouble() * 100
        );
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
        return new StatsFilter(
                selectedSector,
                selectedServeType,
                selectedReceiverCount
        );
    }
}
