package justServes.models;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GameTest {

    @Test
    void newGameHasEmptyServeList() {
        Game game = new Game();
        assertNotNull(game.getServes());
        assertTrue(game.getServes().isEmpty());
    }

    @Test
    void setServesStoresTheList() {
        Game game = new Game();
        Serve serve = new Serve();
        game.setServes(new ArrayList<>(List.of(serve)));
        assertEquals(1, game.getServes().size());
        assertSame(serve, game.getServes().get(0));
    }

    @Test
    void startedAtRoundTrips() {
        Game game = new Game();
        Instant now = Instant.now();
        game.setStartedAt(now);
        assertEquals(now, game.getStartedAt());
    }
}