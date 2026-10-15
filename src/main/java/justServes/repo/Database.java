package justServes.repo;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Creates the connection to the local database
 */
public class Database implements AutoCloseable {

    private final Connection connection;

    private Database(String url) {
        try {
            connection = DriverManager.getConnection(url);
            try (Statement st = connection.createStatement()) {
                st.execute("PRAGMA foreign_keys = ON");
            }
            runSchema();
        } catch (SQLException | IOException e) {
            throw new IllegalStateException("Could not open database: " + url, e);
        }
    }

    /** The real database, stored in the user's own folder. */
    public static Database forUser() {
        Path dir = Path.of(System.getProperty("user.home"), ".justServes");
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return new Database("jdbc:sqlite:" + dir.resolve("serves.db"));
    }

    /** A throwaway database that lives in RAM, for tests. */
    public static Database inMemory() {
        return new Database("jdbc:sqlite::memory:");
    }

    public Connection getConnection() {
        return connection;
    }

    private void runSchema() throws IOException, SQLException {
        try (InputStream in = Database.class.getResourceAsStream("/schema.sql")) {
            if (in == null) {
                throw new IllegalStateException("schema.sql not found on classpath");
            }
            String sql = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            for (String statement : sql.split(";")) {
                if (!statement.isBlank()) {
                    try (Statement st = connection.createStatement()) {
                        st.execute(statement);
                    }
                }
            }
        }
    }

    @Override
    public void close() {
        try {
            connection.close();
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }
}