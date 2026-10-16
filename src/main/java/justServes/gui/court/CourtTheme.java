package justServes.gui.court;

import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import java.util.Objects;

/** Every colour the court uses. surfaceImage is optional and replaces the flat surface colour when set. */
public record CourtTheme(
        String name,
        Color surface,
        Image surfaceImage,
        Color border,
        Color zoneLine,
        Color net,
        Color netLabel,
        Color markerOutline,
        Color pendingRing) {

    public CourtTheme {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(surface, "surface");
        Objects.requireNonNull(border, "border");
        Objects.requireNonNull(zoneLine, "zoneLine");
        Objects.requireNonNull(net, "net");
        Objects.requireNonNull(netLabel, "netLabel");
        Objects.requireNonNull(markerOutline, "markerOutline");
        Objects.requireNonNull(pendingRing, "pendingRing");
    }
}