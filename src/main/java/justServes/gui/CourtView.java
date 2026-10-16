package justServes.gui;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import justServes.models.Outcome;
import justServes.models.Serve;
import justServes.services.CourtPoint;
import justServes.services.ReceiveLayouts;
import justServes.services.Zone;
import justServes.util.CourtBounds;
import justServes.util.CourtCoordinates;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

import static justServes.AppConstants.COURT_ASPECT_RATIO;

public class CourtView extends Pane {

    private static final double ASPECT_RATIO = COURT_ASPECT_RATIO;
    private final Canvas canvas = new Canvas();
    private List<Serve> serves = List.of();
    private int receiverCount = 1;
    private Consumer<CourtPoint> onCourtClicked = p -> { };
    private CourtPoint pendingAim;

    public CourtView() {
        setPrefSize(400, 400);
        setMinSize(200, 200);
        getChildren().add(canvas);
        canvas.setOnMouseClicked(e -> handleClick(e.getX(), e.getY()));
    }

    @Override
    protected void layoutChildren() {
        super.layoutChildren();
        canvas.setWidth(getWidth());
        canvas.setHeight(getHeight());
        redraw();
    }

    public void setOnCourtClicked(Consumer<CourtPoint> handler) {
        this.onCourtClicked = handler;
    }

    public void setServes(List<Serve> serves, int receiverCount) {
        this.serves = serves;
        this.receiverCount = receiverCount;
        redraw();
    }

    public void setPendingAim(CourtPoint pendingAim) {
        this.pendingAim = pendingAim;
        redraw();
    }

    private CourtBounds bounds() {
        return CourtCoordinates.fit(getWidth(), getHeight(), ASPECT_RATIO);   // never stored
    }

    private void handleClick(double px, double py) {
        CourtBounds b = bounds();
        if (!b.contains(px, py)) {
            return;                                        // clicked a margin: ignore
        }
        CourtPoint point = CourtCoordinates.toCourt(px, py, b);
        setPendingAim(point);                              // draws the ring straight away
        onCourtClicked.accept(point);
    }

    private void redraw() {
        if (getWidth() <= 0 || getHeight() <= 0) {
            return;                                        // not laid out yet
        }
        GraphicsContext g = canvas.getGraphicsContext2D();
        g.clearRect(0, 0, getWidth(), getHeight());
        CourtBounds b = bounds();

        g.setFill(Color.web("#f4925a"));
        g.fillRect(b.left(), b.top(), b.width(), b.height());

        // Zone outlines, from the same layout SectorService uses
        g.setStroke(Color.WHITE);
        for (Zone z : ReceiveLayouts.layoutFor(receiverCount)) {
            double x = CourtCoordinates.pixelX(z.xMin(), b);
            double y = CourtCoordinates.pixelY(z.yMax(), b);   // on screen, a zone's top edge is its yMax
            double w = (z.xMax() - z.xMin()) * b.width();
            double h = (z.yMax() - z.yMin()) * b.height();
            g.strokeRect(x, y, w, h);
        }

        double netY = b.top() + b.height();
        g.setStroke(Color.web("#222222"));
        g.setLineWidth(4);
        g.strokeLine(b.left(), netY, b.left() + b.width(), netY);

        // Markers: stored court coordinates converted with the CURRENT bounds
        for (Serve s : serves) {
            double px = CourtCoordinates.pixelX(s.getAimX(), b);
            double py = CourtCoordinates.pixelY(s.getAimY(), b);
            g.setFill(colorFor(s.getOutcome()));
            g.setFill(colorFor(s.getOutcome()));
            g.fillOval(px - 5, py - 5, 10, 10);
            g.setStroke(Color.WHITE);
            g.setLineWidth(1.5);
            g.strokeOval(px - 5, py - 5, 10, 10);
        }

        if (pendingAim != null) {
            double px = CourtCoordinates.pixelX(pendingAim.x(), b);
            double py = CourtCoordinates.pixelY(pendingAim.y(), b);
            g.setStroke(Color.web("#1565c0"));
            g.setLineWidth(3);
            g.strokeOval(px - 9, py - 9, 18, 18);
            g.setFill(Color.web("#1565c0"));
            g.fillOval(px - 2.5, py - 2.5, 5, 5);
        }
    }

    private Color colorFor(Outcome outcome) {              // placeholder colours
        return switch (outcome) {
            case ACE -> Color.GOLD;
            case SHAKY -> Color.MEDIUMPURPLE;
            case RETURNED -> Color.DODGERBLUE;
            case OUT -> Color.CRIMSON;
        };
    }
}