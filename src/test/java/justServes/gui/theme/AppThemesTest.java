package justServes.gui.theme;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AppThemesTest {

    @Test
    void themeNamesAreUnique() {
        Set<String> names = new HashSet<>();
        for (AppTheme theme : AppThemes.ALL) {
            assertTrue(names.add(theme.name()), "duplicate theme name: " + theme.name());
        }
    }

    @Test
    void aThemeCanBeFoundByItsName() {
        for (AppTheme theme : AppThemes.ALL) {
            assertEquals(Optional.of(theme), AppThemes.byName(theme.name()));
        }
        assertTrue(AppThemes.byName("No such theme").isEmpty());
    }
}