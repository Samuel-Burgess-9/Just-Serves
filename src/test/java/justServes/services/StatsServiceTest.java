package justServes.services;

import justServes.models.Outcome;
import justServes.models.Serve;
import justServes.models.ServeType;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static justServes.models.Outcome.*;
import static justServes.models.ServeType.*;
import static org.junit.jupiter.api.Assertions.*;

class StatsServiceTest {

    private static Serve serve(double x, double y, Outcome outcome, ServeType type) {
        Serve s = new Serve();
        s.setGameId(1);
        s.setReceiverCount(2);
        s.setAimX(x);
        s.setAimY(y);
        s.setOutcome(outcome);
        s.setType(type);
        return s;
    }

    private final Serve ace1 = serve(0.5, 0.5, ACE, TOPSPIN);      // CENTRE
    private final Serve ace2 = serve(0.95, 0.7, ACE, FLOAT);       // RIGHT_LINE_BACK
    private final Serve shaky = serve(0.95, 0.7, SHAKY, TOPSPIN);  // RIGHT_LINE_BACK
    private final Serve out = serve(0.5, 0.95, OUT, TOPSPIN);      // END_LINE
    private final List<Serve> serves = List.of(ace1, ace2, shaky, out);

    private final StatsService stats = new StatsService();

    @Test
    void sectorCountsListEveryZoneIncludingEmptyOnes() {
        Map<Sector, Integer> counts = stats.sectorCounts(serves, null);

        assertEquals(Sector.values().length, counts.size());
        assertEquals(1, counts.get(Sector.CENTRE));
        assertEquals(2, counts.get(Sector.RIGHT_LINE_BACK));
        assertEquals(1, counts.get(Sector.END_LINE));
        assertEquals(0, counts.get(Sector.FRONT_LEFT));
    }

    @Test
    void sectorCountsCanBeNarrowedToOneServeType() {
        Map<Sector, Integer> floats = stats.sectorCounts(serves, FLOAT);

        assertEquals(1, floats.get(Sector.RIGHT_LINE_BACK));
        assertEquals(0, floats.get(Sector.CENTRE));
        assertEquals(0, floats.get(Sector.END_LINE));
    }

    @Test
    void outcomesWithNoFilterCoverTheWholeGame() {
        OutcomeStats result = stats.outcomes(serves, StatsFilter.NONE);

        assertEquals(4, result.total());
        assertEquals(2, result.count(ACE));
        assertEquals(1, result.count(SHAKY));
        assertEquals(0, result.count(RETURNED));
        assertEquals(1, result.count(OUT));
        assertEquals(0.5, result.share(ACE), 1e-9);
    }

    @Test
    void outcomesCanBeNarrowedToOneZone() {
        OutcomeStats result = stats.outcomes(serves, StatsFilter.NONE.withSector(Sector.RIGHT_LINE_BACK));

        assertEquals(2, result.total());
        assertEquals(1, result.count(ACE));
        assertEquals(1, result.count(SHAKY));
    }

    @Test
    void zoneAndServeTypeNarrowTheResultsTogether() {
        StatsFilter filter = StatsFilter.NONE.withSector(Sector.RIGHT_LINE_BACK).withServeType(TOPSPIN);

        OutcomeStats result = stats.outcomes(serves, filter);

        assertEquals(1, result.total());
        assertEquals(1, result.count(SHAKY));
    }

    @Test
    void noServesGivesZeroesAndNoErrors() {
        OutcomeStats result = stats.outcomes(List.of(), StatsFilter.NONE);

        assertEquals(0, result.total());
        assertEquals(0.0, result.share(ACE));
        assertEquals(0, stats.sectorCounts(List.of(), null).get(Sector.CENTRE));
    }

    @Test
    void matchingKeepsTheOriginalOrder() {
        List<Serve> topspins = stats.matching(serves, StatsFilter.NONE.withServeType(TOPSPIN));

        assertEquals(List.of(ace1, shaky, out), topspins);
    }

    @Test
    void changingAFilterLeavesTheOriginalUntouched() {
        StatsFilter narrowed = StatsFilter.NONE.withSector(Sector.CENTRE);

        assertNull(StatsFilter.NONE.sector());
        assertEquals(Sector.CENTRE, narrowed.sector());
    }
}