package justServes.gui.court;

import javafx.scene.paint.Color;
import justServes.models.Outcome;

public final class OutcomeColours {

    private OutcomeColours() { }

    /** No default branch on purpose: a new Outcome won't compile until it gets a colour. */
    public static Color of(Outcome outcome) {
        return switch (outcome) {
            case ACE -> Color.GOLD;
            case SHAKY -> Color.MEDIUMPURPLE;
            case RETURNED -> Color.DODGERBLUE;
            case OUT -> Color.CRIMSON;
        };
    }
}
