
package justServes.gui.theme;

import javafx.scene.paint.Color;
import justServes.gui.court.CourtTheme;
import justServes.gui.court.CourtThemes;

import java.util.List;

public final class AppThemes {

    private AppThemes() { }

    // Practice: warm hardwood and burnt orange.
    public static final AppTheme PRACTICE = new AppTheme(
            "Practice court",
            CourtThemes.DEFAULT,
            new Palette(
                    Color.web("#D97936"),
                    Color.web("#A94D24"),
                    Color.WHITE,
                    Color.web("#FBE8D7")
            )
    );

    // Nationals: orange playing surface with blue UI accents.
    public static final AppTheme NATIONALS = new AppTheme(
            "Nationals",
            new CourtTheme(
                    "Nationals",
                    Color.web("#D96A32"),
                    null,
                    Color.WHITE,
                    Color.color(1, 1, 1, 0.16),
                    Color.color(1, 1, 1, 0.28),
                    Color.web("#14202B"),
                    Color.web("#14202B"),
                    Color.WHITE,
                    Color.web("#2469C9")
            ),
            new Palette(
                    Color.web("#2469C9"),
                    Color.web("#16468C"),
                    Color.WHITE,
                    Color.web("#E3EDFC")
            )
    );

    // Grass: natural greens with white court markings.
    public static final AppTheme GRASS = new AppTheme(
            "Grass",
            new CourtTheme(
                    "Grass",
                    Color.web("#347B43"),
                    null,
                    Color.WHITE,
                    Color.color(1, 1, 1, 0.16),
                    Color.color(1, 1, 1, 0.28),
                    Color.web("#183D26"),
                    Color.web("#183D26"),
                    Color.WHITE,
                    Color.web("#B8ED83")
            ),
            new Palette(
                    Color.web("#3E9B59"),
                    Color.web("#23683A"),
                    Color.WHITE,
                    Color.web("#E1F3E4")
            )
    );

    // Beach: golden sand with turquoise ocean accents.
    public static final AppTheme BEACH = new AppTheme(
            "Beach",
            new CourtTheme(
                    "Beach",
                    Color.web("#D9B77A"),
                    null,
                    Color.WHITE,
                    Color.color(1, 1, 1, 0.20),
                    Color.color(1, 1, 1, 0.34),
                    Color.web("#614729"),
                    Color.web("#614729"),
                    Color.WHITE,
                    Color.web("#148F91")
            ),
            new Palette(
                    Color.web("#148F91"),
                    Color.web("#086568"),
                    Color.WHITE,
                    Color.web("#DDF4EF")
            )
    );

    // Sky: bright blue with cloud-white details.
    public static final AppTheme SKY = new AppTheme(
            "Sky",
            new CourtTheme(
                    "Sky",
                    Color.web("#69B8EE"),
                    null,
                    Color.WHITE,
                    Color.color(1, 1, 1, 0.24),
                    Color.color(1, 1, 1, 0.38),
                    Color.web("#17436A"),
                    Color.web("#17436A"),
                    Color.WHITE,
                    Color.web("#2166B0")
            ),
            new Palette(
                    Color.web("#3998E8"),
                    Color.web("#2166B0"),
                    Color.WHITE,
                    Color.web("#E1F1FF")
            )
    );

    public static final List<AppTheme> ALL = List.of(
            PRACTICE,
            NATIONALS,
            GRASS,
            BEACH,
            SKY
    );

    public static final AppTheme DEFAULT = PRACTICE;
}
