package justServes.gui.court;

import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import justServes.AppConstants;
import justServes.models.Serve;
import justServes.services.Sector;
import justServes.services.Zone;
import justServes.util.CourtBounds;
import justServes.util.CourtCoordinates;
import justServes.util.CourtPoint;

import java.util.List;
import java.util.Map;

/**
 * Draws the court. Each method sets its own paint state, so drawing order never matters,
 * and every size scales with the court so it looks right at any window size.
 */
final class CourtPainter {

    private CourtPainter() { }

    static void drawSurface(GraphicsContext g, CourtBounds b, CourtTheme theme) {
        if (theme.surfaceImage() != null) {
            g.drawImage(theme.surfaceImage(), b.left(), b.top(), b.width(), b.height());
        } else {
            g.setFill(theme.surface());
            g.fillRect(b.left(), b.top(), b.width(), b.height());
        }
    }

    private record Tile(double x, double y, double w, double h, double arc) { }

    private static Tile tile(Zone z, CourtBounds b) {
        double inset = Math.max(1.5, b.width() * 0.004);   // gaps between tiles read as an overlay, not lines
        return new Tile(
                CourtCoordinates.pixelX(z.xMin(), b) + inset,
                CourtCoordinates.pixelY(z.yMax(), b) + inset,   // a zone's top edge on screen is its yMax
                (z.xMax() - z.xMin()) * b.width() - 2 * inset,
                (z.yMax() - z.yMin()) * b.height() - 2 * inset,
                Math.max(4, b.width() * 0.012));
    }

    static void drawZones(GraphicsContext g, CourtBounds b, CourtTheme theme, List<Zone> zones) {
        g.setFill(theme.zoneFill());
        g.setStroke(theme.zoneEdge());
        g.setLineWidth(1);
        for (Zone z : zones) {
            Tile t = tile(z, b);
            g.fillRoundRect(t.x(), t.y(), t.w(), t.h(), t.arc(), t.arc());
            g.strokeRoundRect(t.x(), t.y(), t.w(), t.h(), t.arc(), t.arc());
        }
    }

    /** The same tiles as drawZones, filled by serve count on the theme's heat scale, with the count in each. */
    static void drawHeatmap(GraphicsContext g, CourtBounds b, CourtTheme theme,
                            List<Zone> zones, Map<Sector, Integer> counts) {
        int max = counts.values().stream().mapToInt(Integer::intValue).max().orElse(0);

        g.setTextAlign(TextAlignment.CENTER);
        g.setTextBaseline(VPos.CENTER);
        g.setFont(Font.font(clamp(b.width() * 0.032, 10, 18)));
        g.setLineWidth(1);

        for (Zone z : zones) {
            int count = counts.getOrDefault(z.sector(), 0);
            Tile t = tile(z, b);
            boolean empty = count == 0 || max == 0;

            Color fill = empty
                    ? theme.zoneFill()                                   // empty zones look like the record overlay
                    : HeatScale.colourAt(theme, Math.sqrt(count / (double) max));   // sqrt keeps quiet zones visible
            g.setFill(fill);
            g.fillRoundRect(t.x(), t.y(), t.w(), t.h(), t.arc(), t.arc());
            g.setStroke(theme.zoneEdge());
            g.strokeRoundRect(t.x(), t.y(), t.w(), t.h(), t.arc(), t.arc());

            Color text = readableOn(empty ? theme.surface() : fill);
            g.setFill(empty ? text.deriveColor(0, 1, 1, 0.55) : text);
            g.fillText(String.valueOf(count), t.x() + t.w() / 2, t.y() + t.h() / 2);
        }
    }

    static void drawSelectedZone(GraphicsContext g, CourtBounds b, CourtTheme theme,
                                 List<Zone> zones, Sector selected) {
        if (selected == null) {
            return;
        }
        g.setStroke(theme.pendingRing());   // each theme already picked this to be visible on its surface
        g.setLineWidth(Math.max(2.5, b.width() * 0.007));
        for (Zone z : zones) {
            if (z.sector() == selected) {
                Tile t = tile(z, b);
                g.strokeRoundRect(t.x(), t.y(), t.w(), t.h(), t.arc(), t.arc());
            }
        }
    }

    private static Color readableOn(Color background) {
        double luminance = 0.2126 * background.getRed() + 0.7152 * background.getGreen() + 0.0722 * background.getBlue();
        return luminance > 0.55 ? Color.web("#1f2933") : Color.WHITE;
    }


    static void drawLines(GraphicsContext g, CourtBounds b, CourtTheme theme) {
        g.setStroke(theme.lines());
        g.setLineWidth(Math.max(1.5, b.width() * 0.004));
        g.strokeRect(b.left(), b.top(), b.width(), b.height());

        // Attack line is optional per court theme.
        if (theme.showAttackLine()) {
            double attackY = CourtCoordinates.pixelY(
                    AppConstants.COURT_ATTACK_LINE_Y, b);
            g.strokeLine(b.left(), attackY, b.left() + b.width(), attackY);
        }
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