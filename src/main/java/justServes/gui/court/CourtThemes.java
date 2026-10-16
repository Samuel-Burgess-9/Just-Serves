package justServes.gui.court;

import javafx.scene.paint.Color;

public final class CourtThemes {

    private CourtThemes() { }

    public static final CourtTheme DEFAULT = new CourtTheme(
            "Default",
            Color.web("#f4925a"),          // surface
            null,                          // no image
            Color.WHITE,                   // border
            Color.color(1, 1, 1, 0.55),    // zone lines
            Color.web("#222222"),          // net
            Color.web("#222222"),          // net label
            Color.WHITE,                   // marker outline
            Color.web("#1565c0"));         // pending ring
}