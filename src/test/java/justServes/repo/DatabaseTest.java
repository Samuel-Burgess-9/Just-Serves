package justServes.repo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;

class DatabaseTest {

    @Test
    void createsBothTables() throws Exception {
        try (Database db = Database.inMemory();
             Statement st = db.getConnection().createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT name FROM sqlite_master WHERE type = 'table' AND name IN ('game', 'serve')")) {
            int count = 0;
            while (rs.next()) {
                count++;
            }
            assertEquals(2, count);
        }
    }

    @Test
    void rejectsServeForMissingGame() {
        try (Database db = Database.inMemory()) {
            assertThrows(SQLException.class, () -> {
                try (Statement st = db.getConnection().createStatement()) {
                    st.execute("INSERT INTO serve (game_id, receiver_count, aim_x, aim_y, outcome) "
                            + "VALUES (999, 2, 0.5, 0.5, 'ace')");
                }
            });
        }
    }

    @Test
    void freshDatabaseIsAtTheLatestVersion() {
        try (Database db = Database.inMemory()) {
            assertEquals(Database.LATEST_VERSION, db.schemaVersion());
        }
    }

    @Test
    void reopeningAFileKeepsDataAndVersion(@TempDir Path dir) {
        Path file = dir.resolve("test.db");
        int gameId;
        try (Database db = Database.forFile(file)) {
            gameId = new GameRepository(db).create().getId();
        }
        try (Database db = Database.forFile(file)) {
            assertEquals(Database.LATEST_VERSION, db.schemaVersion());
            assertTrue(new GameRepository(db).findById(gameId).isPresent());
        }
    }

    @Test
    void unversionedDatabaseIsBroughtUpToDate(@TempDir Path dir) throws Exception {
        Path file = dir.resolve("old.db");
        int gameId;
        try (Database db = Database.forFile(file)) {
            gameId = new GameRepository(db).create().getId();
        }
        setUserVersion(file, 0);   // pretend this file pre-dates versioning, like your dev database
        try (Database db = Database.forFile(file)) {
            assertEquals(Database.LATEST_VERSION, db.schemaVersion());
            assertTrue(new GameRepository(db).findById(gameId).isPresent());
        }
    }

    @Test
    void databaseFromANewerAppIsRejected(@TempDir Path dir) throws Exception {
        Path file = dir.resolve("future.db");
        try (Database db = Database.forFile(file)) {
            // just create it
        }
        setUserVersion(file, Database.LATEST_VERSION + 1);
        assertThrows(IllegalStateException.class, () -> Database.forFile(file));
    }

    private static void setUserVersion(Path file, int version) throws SQLException {
        try (Connection c = DriverManager.getConnection("jdbc:sqlite:" + file);
             Statement st = c.createStatement()) {
            st.execute("PRAGMA user_version = " + version);
        }
    }
}