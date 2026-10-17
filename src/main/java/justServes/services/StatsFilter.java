package justServes.services;

import justServes.AppConstants;
import justServes.models.ServeType;

import java.util.List;

/** What the analysis screen is focused on. A null field means "no restriction". */
public record StatsFilter(Sector sector, ServeType serveType, Integer receiverCount) {

    public static final StatsFilter NONE = new StatsFilter(null, null, null);

    public StatsFilter {
        if (receiverCount != null
                && (receiverCount < AppConstants.RECEIVER_MIN || receiverCount > AppConstants.RECEIVER_MAX)) {
            throw new IllegalArgumentException("receiverCount must be between "
                    + AppConstants.RECEIVER_MIN + " and " + AppConstants.RECEIVER_MAX);
        }
    }

    public StatsFilter withSector(Sector sector) {
        return new StatsFilter(sector, serveType, receiverCount);
    }

    public StatsFilter withServeType(ServeType serveType) {
        return new StatsFilter(sector, serveType, receiverCount);
    }

    /** Zones differ between layouts, so changing the receiver count drops the zone focus. */
    public StatsFilter withReceiverCount(Integer receiverCount) {
        return new StatsFilter(null, serveType, receiverCount);
    }

    /** The zoning this filter is viewed on: that count's own layout, or the default zoning for "all". */
    public List<Zone> layout() {
        return receiverCount == null
                ? ReceiverLayouts.defaultLayout()
                : ReceiverLayouts.layoutFor(receiverCount);
    }
}