package justServes.gui.theme;

import justServes.gui.court.CourtTheme;

import java.util.Objects;

public record AppTheme(String name, CourtTheme court, Palette palette) {
    public AppTheme {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(court, "court");
        Objects.requireNonNull(palette, "palette");
    }
}