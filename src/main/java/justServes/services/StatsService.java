package justServes.services;

import justServes.models.Outcome;
import justServes.models.Serve;
import justServes.models.ServeType;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class StatsService {

    private final SectorService sectorService;

    public StatsService(SectorService sectorService) {
        this.sectorService = Objects.requireNonNull(sectorService, "sectorService");
    }

    public StatsService() {
        this(new SectorService());
    }

    /** Heatmap data: serves per zone of the filter's layout. The zone focus is deliberately not applied. */
    public Map<Sector, Integer> sectorCounts(List<Serve> serves, StatsFilter filter) {
        List<Zone> layout = filter.layout();
        Map<Sector, Integer> counts = new EnumMap<>(Sector.class);
        for (Sector sector : Sector.values()) {
            counts.put(sector, 0);
        }
        for (Serve serve : serves) {
            if (matchesTypeAndReceivers(serve, filter)) {
                counts.merge(sectorOf(serve, layout), 1, Integer::sum);
            }
        }
        return Collections.unmodifiableMap(counts);
    }

    public OutcomeStats outcomes(List<Serve> serves, StatsFilter filter) {
        Map<Outcome, Integer> counts = new EnumMap<>(Outcome.class);
        for (Serve serve : serves) {
            if (matches(serve, filter)) {
                counts.merge(serve.getOutcome(), 1, Integer::sum);
            }
        }
        return new OutcomeStats(counts);
    }

    public List<Serve> matching(List<Serve> serves, StatsFilter filter) {
        return serves.stream().filter(serve -> matches(serve, filter)).toList();
    }

    private boolean matchesTypeAndReceivers(Serve serve, StatsFilter filter) {
        if (filter.serveType() != null && serve.getType() != filter.serveType()) {
            return false;
        }
        return filter.receiverCount() == null || serve.getReceiverCount() == filter.receiverCount();
    }

    private boolean matches(Serve serve, StatsFilter filter) {
        return matchesTypeAndReceivers(serve, filter)
                && (filter.sector() == null || sectorOf(serve, filter.layout()) == filter.sector());
    }

    private Sector sectorOf(Serve serve, List<Zone> layout) {
        return sectorService.sectorOf(serve.getAimX(), serve.getAimY(), layout);
    }
}