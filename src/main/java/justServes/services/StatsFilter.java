package justServes.services;

import justServes.models.ServeType;

/** What the analysis screen is focused on. A null field means "no restriction". */
public record StatsFilter(Sector sector, ServeType serveType) {

    public static final StatsFilter NONE = new StatsFilter(null, null);

    public StatsFilter withSector(Sector sector) {
        return new StatsFilter(sector, serveType);
    }

    public StatsFilter withServeType(ServeType serveType) {
        return new StatsFilter(sector, serveType);
    }
}