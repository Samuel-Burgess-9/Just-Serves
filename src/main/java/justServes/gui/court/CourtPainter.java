package justServes.gui.court;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import justServes.AppConstants;
import justServes.models.Serve;
import justServes.services.Zone;
import justServes.util.CourtBounds;
import justServes.util.CourtCoordinates;
import justServes.util.CourtPoint;

import java.util.List;

/**
 * Draws the court. Each method sets its own paint state, so drawing order never matters,
 * and every size scales with the court so it looks right at any window size.
 */
final class CourtPainter {

    private static justServes.gui.court.OutcomeColours OutcomeColours;

    private CourtPainter() { }

    static void drawSurface(GraphicsContext g, CourtBounds b, CourtTheme theme) {
        if (theme.surfaceImage() != null) {
            g.drawImage(theme.surfaceImage(), b.left(), b.top(), b.width(), b.height());
        } else {
            g.setFill(theme.surface());
            g.fillRect(b.left(), b.top(), b.width(), b.height());
        }
    }

    static void drawZones(GraphicsContext g, CourtBounds b, CourtTheme theme, List<Zone> zones) {
        double inset = Math.max(1.5, b.width() * 0.004);   // gaps between tiles read as an overlay, not lines
        double arc = Math.max(4, b.width() * 0.012);
        g.setFill(theme.zoneFill());
        g.setStroke(theme.zoneEdge());
        g.setLineWidth(1);
        for (Zone z : zones) {
            double x = CourtCoordinates.pixelX(z.xMin(), b) + inset;
            double y = CourtCoordinates.pixelY(z.yMax(), b) + inset;   // a zone's top edge on screen is its yMax
            double w = (z.xMax() - z.xMin()) * b.width() - 2 * inset;
            double h = (z.yMax() - z.yMin()) * b.height() - 2 * inset;
            g.fillRoundRect(x, y, w, h, arc, arc);
            g.strokeRoundRect(x, y, w, h, arc, arc);
        }
    }

    static void drawLines(GraphicsContext g, CourtBounds b, CourtTheme theme) {
        g.setStroke(theme.lines());
        g.setLineWidth(Math.max(1.5, b.width() * 0.004));
        g.strokeRect(b.left(), b.top(), b.width(), b.height());

        double attackY = CourtCoordinates.pixelY(AppConstants.COURT_ATTACK_LINE_Y, b);
        g.strokeLine(b.left(), attackY, b.left() + b.width(), attackY);
    }

    /** y = 0 is the net, so it runs along the bottom edge. */
    static void drawNet(GraphicsContext g, CourtBounds b, CourtTheme theme) {
        double thickness = Math.max(4, b.height() * 0.012);
        double netY = b.top() + b.height();

        g.setFill(theme.net());
        g.fillRect(b.left(), netY - thickness, b.width(), thickness);

        g.setFill(theme.netLabel());
        g.setFont(Font.font(clamp(b.height() * 0.028, 10, 16)));
        g.setTextAlign(TextAlignment.CENTER);
        g.fillText("NET", b.left() + b.width() / 2, netY - thickness - 4);
    }

    static void drawServes(GraphicsContext g, CourtBounds b, CourtTheme theme, List<Serve> serves) {
        double r = markerRadius(b);
        g.setLineWidth(Math.max(1, r * 0.25));
        for (Serve s : serves) {
            double px = CourtCoordinates.pixelX(s.getAimX(), b);
            double py = CourtCoordinates.pixelY(s.getAimY(), b);
            g.setFill(OutcomeColours.of(s.getOutcome()));
            g.fillOval(px - r, py - r, 2 * r, 2 * r);
            g.setStroke(theme.markerOutline());
            g.strokeOval(px - r, py - r, 2 * r, 2 * r);
        }
    }

    static void drawPending(GraphicsContext g, CourtBounds b, CourtTheme theme, CourtPoint aim) {
        if (aim == null) {
            return;
        }
        double r = markerRadius(b) * 1.8;
        double px = CourtCoordinates.pixelX(aim.x(), b);
        double py = CourtCoordinates.pixelY(aim.y(), b);

        g.setStroke(theme.pendingRing());
        g.setLineWidth(Math.max(2, r * 0.18));
        g.strokeOval(px - r, py - r, 2 * r, 2 * r);

        double dot = r * 0.25;
        g.setFill(theme.pendingRing());
        g.fillOval(px - dot, py - dot, 2 * dot, 2 * dot);
    }

    private static double markerRadius(CourtBounds b) {
        return clamp(b.width() * 0.014, 4, 9);
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}