package justServes.gui.theme;

import javafx.scene.paint.Color;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ThemeTest {

    @Test
    void filledButtonsAreReadableInEveryTheme() {
        for (AppTheme t : AppThemes.ALL) {
            double ratio = contrast(t.palette().accentStrong(), t.palette().onAccent());
            assertTrue(ratio >= 4.5, t.name() + " button contrast is only " + ratio);
        }
    }

    @Test
    void paletteBecomesInlineCss() {
        Palette p = new Palette(Color.web("#e0662f"), Color.web("#b84a20"), Color.WHITE, Color.web("#fde8dc"));
        assertTrue(p.toStyle().contains("-app-accent: #e0662f;"));
        assertTrue(p.toStyle().contains("-app-on-accent: #ffffff;"));
    }

    private static double contrast(Color a, Color b) {
        double l1 = luminance(a), l2 = luminance(b);
        return (Math.max(l1, l2) + 0.05) / (Math.min(l1, l2) + 0.05);
    }

    private static double luminance(Color c) {
        return 0.2126 * linear(c.getRed()) + 0.7152 * linear(c.getGreen()) + 0.0722 * linear(c.getBlue());
    }

    private static double linear(double v) {
        return v <= 0.03928 ? v / 12.92 : Math.pow((v + 0.055) / 1.055, 2.4);
    }
}