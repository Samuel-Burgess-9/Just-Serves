package justServes.gui.court;

import justServes.gui.theme.AppTheme;
import justServes.gui.theme.AppThemes;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HeatScaleTest {

    @Test
    void heatGetsDarkerAsCountsRiseInEveryTheme() {
        for (AppTheme t : AppThemes.ALL) {
            assertTrue(HeatScale.low(t.court()).getBrightness() > HeatScale.high(t.court()).getBrightness(),
                    t.name() + " heat scale doesn't darken");
        }
    }

    @Test
    void scaleRunsFromLowToHigh() {
        CourtTheme court = CourtThemes.DEFAULT;

        assertEquals(HeatScale.low(court), HeatScale.colourAt(court, 0));
        assertEquals(HeatScale.high(court), HeatScale.colourAt(court, 1));
    }
}