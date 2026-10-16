package justServes.gui.theme;

import javafx.scene.paint.Color;

import java.util.Objects;

public record Palette(Color accent, Color accentStrong, Color onAccent, Color accentSoft) {

    public Palette {
        Objects.requireNonNull(accent, "accent");
        Objects.requireNonNull(accentStrong, "accentStrong");
        Objects.requireNonNull(onAccent, "onAccent");
        Objects.requireNonNull(accentSoft, "accentSoft");
    }

    /** Inline CSS that overrides the palette variables declared on .root in app.css. */
    public String toStyle() {
        return "-app-accent: " + hex(accent) + "; "
                + "-app-accent-strong: " + hex(accentStrong) + "; "
                + "-app-on-accent: " + hex(onAccent) + "; "
                + "-app-accent-soft: " + hex(accentSoft) + ";";
    }

    static String hex(Color c) {
        return String.format("#%02x%02x%02x",
                Math.round(c.getRed() * 255),
                Math.round(c.getGreen() * 255),
                Math.round(c.getBlue() * 255));
    }
}