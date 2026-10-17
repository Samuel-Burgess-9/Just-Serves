package justServes.gui.court;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import justServes.AppConstants;
import justServes.models.Serve;
import justServes.services.ReceiverLayouts;
import justServes.services.Sector;
import justServes.services.Zone;
import justServes.util.CourtBounds;
import justServes.util.CourtCoordinates;
import justServes.util.CourtPoint;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

public class CourtView extends Pane {

    private final Canvas canvas = new Canvas();
    private CourtTheme theme = CourtThemes.DEFAULT;
    private boolean showZones = true;
    private List<Serve> serves = List.of();
    private int receiverCount = AppConstants.DEFAULT_RECEIVER_COUNT;
    private CourtPoint pendingAim;
    private Consumer<CourtPoint> onCourtClicked = p -> { };
    private Map<Sector, Integer> heatmapCounts;

    private DisplayMode displayMode = DisplayMode.RECORD;

    public CourtView() {
        setPrefSize(400, 400);
        setMinSize(200, 200);
        getChildren().add(canvas);
        canvas.setOnMouseClicked(e -> handleClick(e.getX(), e.getY()));
    }

    // ---------- what the controller can change ----------

    public void setOnCourtClicked(Consumer<CourtPoint> handler) {
        this.onCourtClicked = handler;
    }

    public void setServes(List<Serve> serves, int receiverCount) {
        this.serves = List.copyOf(serves);
        this.receiverCount = receiverCount;
        redraw();
    }

    public void setDisplayMode(DisplayMode displayMode) {
        this.displayMode = displayMode;
    }

    /** Shows the chosen aim before the serve is recorded; null clears it. */
    public void setPendingAim(CourtPoint pendingAim) {
        this.pendingAim = pendingAim;
        redraw();
    }

    /** Sets heatmap counts for analysis. Null disables the heatmap. */
    public void setHeatmapCounts(Map<Sector, Integer> counts) {
        this.heatmapCounts = counts == null
                ? null
                : Map.copyOf(counts);
        redraw();
    }

    public void setTheme(CourtTheme theme) {
        this.theme = Objects.requireNonNull(theme, "theme");
        redraw();
    }

    public void setShowZones(boolean showZones) {
        this.showZones = showZones;
        redraw();
    }

    // ---------- layout and input ----------

    @Override
    protected void layoutChildren() {
        super.layoutChildren();
        canvas.setWidth(getWidth());
        canvas.setHeight(getHeight());
        redraw();
    }

    private CourtBounds bounds() {
        return CourtCoordinates.fit(getWidth(), getHeight(), AppConstants.COURT_ASPECT_RATIO);   // never stored
    }

    private void handleClick(double px, double py) {
        CourtBounds b = bounds();
        if (!b.contains(px, py)) {
            return;                                        // clicked a margin
        }
        CourtPoint point = CourtCoordinates.toCourt(px, py, b);
        setPendingAim(point);
        onCourtClicked.accept(point);
    }

    // ---------- drawing ----------

    private void redraw() {
        if (getWidth() <= 0 || getHeight() <= 0) {
            return;
        }

        GraphicsContext g = canvas.getGraphicsContext2D();
        g.clearRect(0, 0, getWidth(), getHeight());

        CourtBounds b = bounds();

        CourtPainter.drawSurface(g, b, theme);

        if (displayMode == DisplayMode.RECORD) {
            redrawRecord(g, b);
        } else {
            redrawAnalysis(g, b);
        }
    }

    private void redrawRecord(GraphicsContext g, CourtBounds b) {
        if (showZones) {
            CourtPainter.drawZones(
                    g, b, theme,
                    ReceiverLayouts.layoutFor(receiverCount)
            );
        }

        CourtPainter.drawLines(g, b, theme);
        CourtPainter.drawNet(g, b, theme);
        CourtPainter.drawServes(g, b, theme, serves);
        CourtPainter.drawPending(g, b, theme, pendingAim);
    }

    private void redrawAnalysis(GraphicsContext g, CourtBounds b) {
        List<Zone> zones = ReceiverLayouts.layoutFor(receiverCount);

        if (heatmapCounts != null) {
            CourtPainter.drawHeatmap(g, b, zones, heatmapCounts);
        }

        CourtPainter.drawLines(g, b, theme);
        CourtPainter.drawNet(g, b, theme);
    }
}