package justServes.gui.court;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import justServes.AppConstants;
import justServes.models.Serve;
import justServes.services.ReceiverLayouts;
import justServes.util.CourtBounds;
import justServes.util.CourtCoordinates;
import justServes.util.CourtPoint;

import java.util.List;
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

    /** Shows the chosen aim before the serve is recorded; null clears it. */
    public void setPendingAim(CourtPoint pendingAim) {
        this.pendingAim = pendingAim;
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
            return;                                        // not laid out yet
        }
        GraphicsContext g = canvas.getGraphicsContext2D();
        g.clearRect(0, 0, getWidth(), getHeight());
        CourtBounds b = bounds();

        CourtPainter.drawSurface(g, b, theme);
        if (showZones) {
            CourtPainter.drawZones(g, b, theme, ReceiverLayouts.layoutFor(receiverCount));
        }
        CourtPainter.drawLines(g, b, theme);     // on top of the overlay
        CourtPainter.drawNet(g, b, theme);
        CourtPainter.drawServes(g, b, theme, serves);
        CourtPainter.drawPending(g, b, theme, pendingAim);
    }
}