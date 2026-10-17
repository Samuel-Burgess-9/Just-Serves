package justServes.services;

import justServes.models.Game;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GameSummaryTest {

    @Test
    void accuracyIsTheShareOfServesThatWereNotOut() {
        GameSummary summary = new GameSummary(new Game(), 10, 4, 2);

        assertEquals(0.8, summary.accuracy().getAsDouble(), 1e-9);
    }

    @Test
    void aceRateIsAcesOverAllServes() {
        GameSummary summary = new GameSummary(new Game(), 10, 4, 2);

        assertEquals(0.4, summary.aceRate().getAsDouble(), 1e-9);
    }

    @Test
    void aGameWithNoServesHasNoRates() {
        GameSummary summary = new GameSummary(new Game(), 0, 0, 0);

        assertTrue(summary.accuracy().isEmpty());
        assertTrue(summary.aceRate().isEmpty());
    }

    @Test
    void impossibleCountsAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> new GameSummary(new Game(), 3, 2, 2));
        assertThrows(IllegalArgumentException.class, () -> new GameSummary(new Game(), -1, 0, 0));
    }
}