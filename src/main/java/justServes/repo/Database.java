package justServes.repo;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.util.List;

/**
 * Creates the connection to the local database
 */
public class Database implements AutoCloseable {

    /**
     * Applied in order; entry N brings the database to version N.
     * Once a version has been released, never edit or reorder an entry: add a new file instead.
     */
    private static final List<String> MIGRATIONS = List.of(
            "/db/migrations/001_initial.sql"
    );

    public static final int LATEST_VERSION = MIGRATIONS.size();

    private final Connection connection;

    private Database(Connection connection) {
        this.connection = connection;
    }

    private static Database open(String url) {
        Connection connection;
        try {
            connection = DriverManager.getConnection(url);
        } catch (SQLException e) {
            throw new IllegalStateException("Could not open database: " + url, e);
        }
        try {
            try (Statement st = connection.createStatement()) {
                st.execute("PRAGMA foreign_keys = ON");
            }
            Database db = new Database(connection);
            db.migrate();
            return db;
        } catch (IllegalStateException e) {
            closeQuietly(connection);
            throw e;
        } catch (SQLException | IOException e) {
            closeQuietly(connection);
            throw new IllegalStateException("Could not set up database: " + url, e);
        }
    }

    private static void closeQuietly(Connection connection) {
        try {
            connection.close();
        } catch (SQLException ignored) {
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
        return forFile(dir.resolve("serves.db"));
    }

    public static Database forFile(Path file) {
        return open("jdbc:sqlite:" + file);
    }

    public int schemaVersion() {
        try {
            return readVersion();
        } catch (SQLException e) {
            throw new IllegalStateException("Could not read schema version", e);
        }
    }

    private int readVersion() throws SQLException {
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery("PRAGMA user_version")) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private void migrate() throws SQLException, IOException {
        int current = readVersion();
        if (current > LATEST_VERSION) {
            throw new IllegalStateException("This database was created by a newer version of Just Serves "
                    + "(schema v" + current + "; this app understands up to v" + LATEST_VERSION + ")");
        }
        for (int version = current + 1; version <= LATEST_VERSION; version++) {
            applyMigration(version, MIGRATIONS.get(version - 1));
        }
    }

    /** Runs one migration and its version bump as a single all-or-nothing transaction. */
    private void applyMigration(int version, String resource) throws SQLException, IOException {
        String sql = readResource(resource);
        connection.setAutoCommit(false);
        try {
            for (String statement : sql.split(";")) {
                if (!statement.isBlank()) {
                    try (Statement st = connection.createStatement()) {
                        st.execute(statement);
                    }
                }
            }
            try (Statement st = connection.createStatement()) {
                st.execute("PRAGMA user_version = " + version);   // a number we control, so concatenating is safe
            }
            connection.commit();
        } catch (SQLException | RuntimeException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    private static String readResource(String path) throws IOException {
        try (InputStream in = Database.class.getResourceAsStream(path)) {
            if (in == null) {
                throw new IllegalStateException("Migration not found on classpath: " + path);
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    /** A throwaway database that lives in RAM, for tests. */
    public static Database inMemory() {
        return open("jdbc:sqlite::memory:");
    }

    public Connection getConnection() {
        return connection;
    }

    private void runSchema() throws IOException, SQLException {
        try (InputStream in = Database.class.getResourceAsStream("/db/migrations/001_initial.sql")) {
            if (in == null) {
                throw new IllegalStateException("001_initial.sql not found on classpath");
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