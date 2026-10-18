package justServes.settings;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SettingsStoreTest {

    @Test
    void aValueSurvivesARestart(@TempDir Path dir) {
        Path file = dir.resolve("settings.properties");

        SettingsStore.forFile(file).put("theme", "Nationals");

        assertEquals(Optional.of("Nationals"), SettingsStore.forFile(file).get("theme"));
    }

    @Test
    void aMissingFileMeansNoSettings(@TempDir Path dir) {
        assertTrue(SettingsStore.forFile(dir.resolve("nope.properties")).get("theme").isEmpty());
    }

    @Test
    void anUnreadableFileMeansNoSettingsAndNoCrash(@TempDir Path dir) {
        // a folder where the file should be can't be read as a file
        assertTrue(SettingsStore.forFile(dir).get("theme").isEmpty());
    }

    @Test
    void anInMemoryStoreRemembersButNeverTouchesTheDisk() {
        SettingsStore store = SettingsStore.inMemory();

        store.put("theme", "Grass");

        assertEquals(Optional.of("Grass"), store.get("theme"));
    }
}