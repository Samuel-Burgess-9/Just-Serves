package justServes.gui.theme;

import javafx.scene.paint.Color;
import justServes.gui.court.CourtTheme;
import justServes.gui.court.CourtThemes;

import java.util.List;

public final class AppThemes {

    private AppThemes() { }

    public static final AppTheme PRACTICE = new AppTheme("Practice court",
            CourtThemes.DEFAULT,
            new Palette(Color.web("#e0662f"), Color.web("#b84a20"), Color.WHITE, Color.web("#fde8dc")));

    // Placeholder values: tune by eye
    public static final AppTheme NATIONALS = new AppTheme("Nationals",
            new CourtTheme("Nationals",
                    Color.web("#3d7ab8"), null,
                    Color.WHITE,
                    Color.color(1, 1, 1, 0.16), Color.color(1, 1, 1, 0.28),
                    Color.web("#14202b"), Color.web("#14202b"),
                    Color.WHITE,
                    Color.web("#ffeb3b")),            // a blue ring would vanish on a blue court
            new Palette(Color.web("#1e6fd9"), Color.web("#14509f"), Color.WHITE, Color.web("#e1ecfb")));

    public static final List<AppTheme> ALL = List.of(PRACTICE, NATIONALS);
    public static final AppTheme DEFAULT = PRACTICE;
}