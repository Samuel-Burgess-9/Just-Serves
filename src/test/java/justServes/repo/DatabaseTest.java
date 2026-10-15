package justServes.repo;

import org.junit.jupiter.api.Test;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
}