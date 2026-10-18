
package justServes.gui.controllers;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

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
    @FXML private ComboBox<String> datasetBox;
    @FXML private Label matchesAnalysedLabel;
    @FXML private Label totalServesLabel;
    @FXML private Label accuracyLabel;
    @FXML private Label aceRateLabel;
    @FXML private Label outRateLabel;

    @FXML private Label acesSummaryLabel;
    @FXML private Label shakySummaryLabel;
    @FXML private Label returnedSummaryLabel;
    @FXML private Label outSummaryLabel;

    @FXML private Label gameTitleLabel;
    @FXML private Label hintLabel;
    @FXML private StackPane courtHolder;
    @FXML private HBox legendBar;

    @FXML private RadioButton allToggle;
    @FXML private RadioButton topspinToggle;
    @FXML private RadioButton floatToggle;
    @FXML private RadioButton leftSpinToggle;
    @FXML private RadioButton rightSpinToggle;

    @FXML private ComboBox<String> receiverBox;
    @FXML private ComboBox<String> outcomeFilterBox;

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

    private static final String ALL_TIME = "All time";
    private static final String LAST_10 = "Last 10 matches";
    private static final String LAST_5 = "Last 5 matches";
    private static final String LAST_MATCH = "Last match";

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

    private void configureDatasetSelector() {
        datasetBox.getItems().setAll(
                ALL_TIME,
                LAST_10,
                LAST_5,
                LAST_MATCH
        );

        datasetBox.setValue(ALL_TIME);

        datasetBox.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null) {
                selectedSector = null;
                updateDataset();
                refreshAnalysis();
            }
        });
    }

    private void configureReceiverSelector() {
        receiverBox.getItems().setAll(
                "All", "1", "2", "3", "4", "5", "6"
        );
        receiverBox.setValue("All");

        receiverBox.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue == null) {
                return;
            }

            selectedReceiverCount = "All".equals(newValue)
                    ? null
                    : Integer.valueOf(newValue);

            // The selected zone may not exist in the new layout.
            selectedSector = null;
            refreshAnalysis();
        });
    }

    private void configureOutcomeSelector() {
        outcomeFilterBox.getItems().setAll(
                "All outcomes",
                "Ace",
                "Shaky",
                "Returned",
                "Out"
        );
        outcomeFilterBox.setValue("All outcomes");

        outcomeFilterBox.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue == null) {
                return;
            }

            selectedOutcome = switch (newValue) {
                case "Ace" -> Outcome.ACE;
                case "Shaky" -> Outcome.SHAKY;
                case "Returned" -> Outcome.RETURNED;
                case "Out" -> Outcome.OUT;
                default -> null;
            };

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

        List<Game> games = new ArrayList<>(
                state.getGameService().listGames()
        );

        // Newest matches first. Fall back to ID when dates are equal.
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

        String selection = datasetBox.getValue();
        if (selection == null) {
            selection = ALL_TIME;
        }

        int limit = switch (selection) {
            case LAST_10 -> 10;
            case LAST_5 -> 5;
            case LAST_MATCH -> 1;
            default -> Integer.MAX_VALUE;
        };

        selectedGames = games.stream()
                .limit(limit)
                .toList();

        List<Serve> serves = new ArrayList<>();

        for (Game game : selectedGames) {
            serves.addAll(state.getGameService().servesFor(game));
        }

        datasetServes = List.copyOf(serves);

        updateOverview();
    }

    // --------------------------------------------------
    // Left-hand statistics
    // --------------------------------------------------

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

        OutcomeStats outcomes = statsService.outcomes(
                datasetServes, StatsFilter.NONE
        );

        acesSummaryLabel.setText(String.valueOf(outcomes.count(Outcome.ACE)));
        shakySummaryLabel.setText(String.valueOf(outcomes.count(Outcome.SHAKY)));
        returnedSummaryLabel.setText(String.valueOf(outcomes.count(Outcome.RETURNED)));
        outSummaryLabel.setText(String.valueOf(outcomes.count(Outcome.OUT)));
    }

    private int countOutcome(List<Serve> serves, Outcome outcome) {
        return (int) serves.stream()
                .filter(serve -> serve.getOutcome() == outcome)
                .count();
    }

    // --------------------------------------------------
    // Court interaction and refreshing
    // --------------------------------------------------

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

        // The outcome filter affects the heatmap only.
        // The outcome bars continue to show the outcome distribution
        // for the selected serve type, receiver count, and court zone.
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

        String datasetName = datasetBox.getValue() == null
                ? ALL_TIME
                : datasetBox.getValue();

        gameTitleLabel.setText("Serving Stats · " + datasetName);

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

    // --------------------------------------------------
    // Clearing filters
    // --------------------------------------------------

    private void clearFilters() {
        updatingSelection = true;

        allToggle.setSelected(true);
        receiverBox.setValue("All");
        outcomeFilterBox.setValue("All outcomes");

        updatingSelection = false;

        selectedServeType = null;
        selectedReceiverCount = null;
        selectedOutcome = null;
        selectedSector = null;

        refreshAnalysis();
    }

    // --------------------------------------------------
    // Theme and formatting
    // --------------------------------------------------

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
