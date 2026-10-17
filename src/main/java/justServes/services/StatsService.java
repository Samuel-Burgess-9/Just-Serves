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

    /**
     * Heatmap data: serves per sector, counting only serves of the given type (null = all types).
     * The zone filter is deliberately not applied, so every zone stays visible. Every sector is present.
     */
    public Map<Sector, Integer> sectorCounts(List<Serve> serves, ServeType serveType) {
        Map<Sector, Integer> counts = new EnumMap<>(Sector.class);
        for (Sector sector : Sector.values()) {
            counts.put(sector, 0);
        }
        for (Serve serve : serves) {
            if (serveType == null || serve.getType() == serveType) {
                counts.merge(sectorOf(serve), 1, Integer::sum);
            }
        }
        return Collections.unmodifiableMap(counts);
    }

    /** Outcome breakdown for the serves that match both the zone and the serve type of the filter. */
    public OutcomeStats outcomes(List<Serve> serves, StatsFilter filter) {
        Map<Outcome, Integer> counts = new EnumMap<>(Outcome.class);
        for (Serve serve : serves) {
            if (matches(serve, filter)) {
                counts.merge(serve.getOutcome(), 1, Integer::sum);
            }
        }
        return new OutcomeStats(counts);
    }

    /** The serves that match the filter, in their original order. */
    public List<Serve> matching(List<Serve> serves, StatsFilter filter) {
        return serves.stream().filter(serve -> matches(serve, filter)).toList();
    }

    private boolean matches(Serve serve, StatsFilter filter) {
        if (filter.serveType() != null && serve.getType() != filter.serveType()) {
            return false;
        }
        return filter.sector() == null || sectorOf(serve) == filter.sector();
    }

    private Sector sectorOf(Serve serve) {
        return sectorService.sectorOf(serve.getAimX(), serve.getAimY(), serve.getReceiverCount());
    }
}