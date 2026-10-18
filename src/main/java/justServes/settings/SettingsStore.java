package justServes.settings;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;

/** Small preferences kept in a file next to the database. Losing a preference must never stop the app. */
public final class SettingsStore {

    private final Path file;   // null = in memory only (tests)
    private final Properties properties = new Properties();

    private SettingsStore(Path file) {
        this.file = file;
        if (file != null && Files.isRegularFile(file)) {
            try (Reader reader = Files.newBufferedReader(file)) {
                properties.load(reader);
            } catch (IOException | IllegalArgumentException e) {
                properties.clear();   // an unreadable settings file just means defaults
            }
        }
    }

    public static SettingsStore forUser() {
        Path dir = Path.of(System.getProperty("user.home"), ".justServes");
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return new SettingsStore(dir.resolve("settings.properties"));
    }

    public static SettingsStore forFile(Path file) {
        return new SettingsStore(Objects.requireNonNull(file, "file"));
    }

    /** Remembers values for this run only and never touches the disk. */
    public static SettingsStore inMemory() {
        return new SettingsStore(null);
    }

    public Optional<String> get(String key) {
        return Optional.ofNullable(properties.getProperty(key));
    }

    public void put(String key, String value) {
        properties.setProperty(key, value);
        if (file == null) {
            return;
        }
        try (Writer writer = Files.newBufferedWriter(file)) {
            properties.store(writer, "Just Serves settings");
        } catch (IOException e) {
            // The preference lasts for this session but isn't saved. Not worth interrupting the user.
        }
    }
}