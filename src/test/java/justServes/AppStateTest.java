package justServes;

import justServes.models.Game;
import justServes.models.ServeType;
import justServes.repo.Database;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AppStateTest {

    private AppState state;

    @BeforeEach
    void setUp() {
        state = new AppState(Database.inMemory());
    }

    @AfterEach
    void tearDown() {
        state.close();
    }

    @Test
    void startsWithTheDefaults() {
        assertEquals(AppConstants.DEFAULT_RECEIVER_COUNT, state.getCurrentReceiverCount());
        assertEquals(AppConstants.DEFAULT_SERVE_TYPE, state.getCurrentServeType());
    }

    @Test
    void receiverCountAcceptsBothEndsOfTheRange() {
        state.setCurrentReceiverCount(AppConstants.RECEIVER_MIN);
        assertEquals(AppConstants.RECEIVER_MIN, state.getCurrentReceiverCount());

        state.setCurrentReceiverCount(AppConstants.RECEIVER_MAX);
        assertEquals(AppConstants.RECEIVER_MAX, state.getCurrentReceiverCount());
    }

    @Test
    void receiverCountRejectsValuesOutsideTheRange() {
        assertThrows(IllegalArgumentException.class,
                () -> state.setCurrentReceiverCount(AppConstants.RECEIVER_MIN - 1));
        assertThrows(IllegalArgumentException.class,
                () -> state.setCurrentReceiverCount(AppConstants.RECEIVER_MAX + 1));
        assertThrows(IllegalArgumentException.class,
                () -> state.setCurrentReceiverCount(-5));
    }

    @Test
    void rejectedReceiverCountKeepsThePreviousValue() {
        state.setCurrentReceiverCount(4);

        assertThrows(IllegalArgumentException.class,
                () -> state.setCurrentReceiverCount(99));

        assertEquals(4, state.getCurrentReceiverCount());
    }

    @Test
    void serveTypeCanBeChanged() {
        state.setCurrentServeType(ServeType.FLOAT);

        assertEquals(ServeType.FLOAT, state.getCurrentServeType());
    }

    @Test
    void serveTypeRejectsNullAndKeepsThePreviousValue() {
        state.setCurrentServeType(ServeType.LEFT_SPIN);

        assertThrows(NullPointerException.class,
                () -> state.setCurrentServeType(null));

        assertEquals(ServeType.LEFT_SPIN, state.getCurrentServeType());
    }

    @Test
    void gameServiceIsWiredUpAndUsable() {
        Game game = state.getGameService().startGame();

        assertTrue(game.getId() > 0);
    }
}
