package justServes.gui.court;

import javafx.scene.paint.Color;

/** Turns a serve count into a colour that belongs to the court theme: a pale tint of the surface up to a deep shade of it. */
public final class HeatScale {

    private HeatScale() { }

    public static Color low(CourtTheme theme) {
        return theme.surface().deriveColor(0, 0.35, 1.3, 1);
    }

    public static Color high(CourtTheme theme) {
        return theme.surface().deriveColor(0, 1.2, 0.5, 1);
    }

    /** t runs from 0 (no serves) to 1 (the busiest zone). */
    public static Color colourAt(CourtTheme theme, double t) {
        return low(theme).interpolate(high(theme), Math.max(0, Math.min(1, t)));
    }
}