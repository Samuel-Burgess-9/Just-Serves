package justServes.gui;

import javafx.scene.paint.Color;
import justServes.models.Outcome;
import justServes.gui.court.OutcomeColours;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class OutcomeColoursTest {

    @Test
    void everyOutcomeHasItsOwnColour() {
        Set<Color> seen = new HashSet<>();
        for (Outcome outcome : Outcome.values()) {
            Color colour = OutcomeColours.of(outcome);
            assertNotNull(colour);
            assertTrue(seen.add(colour), "two outcomes share a colour: " + outcome);
        }
    }
}
