package justServes.gui.court;

import javafx.scene.paint.Color;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CourtThemeTest {

    @Test
    void defaultThemeIsComplete() {
        assertNotNull(CourtThemes.DEFAULT);
        assertEquals("Default", CourtThemes.DEFAULT.name());
    }

    @Test
    void aThemeMissingAColourIsRejected() {
        assertThrows(NullPointerException.class, () -> new CourtTheme(
                "Broken", null, null, Color.WHITE, Color.WHITE,
                Color.BLACK, Color.BLACK, Color.WHITE, Color.BLUE, Color.WHITE, true));
    }
}