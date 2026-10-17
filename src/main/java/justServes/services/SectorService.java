package justServes.services;

import java.util.List;

public class SectorService {

    public Sector sectorOf(double aimX, double aimY, int receiverCount) {
        return sectorOf(aimX, aimY, ReceiverLayouts.layoutFor(receiverCount));
    }

    public Sector sectorOf(double aimX, double aimY, List<Zone> layout) {
        if (!Double.isFinite(aimX) || !Double.isFinite(aimY)
                || aimX < 0.0 || aimX > 1.0 || aimY < 0.0 || aimY > 1.0) {
            throw new IllegalArgumentException("Coordinates must be finite and within [0, 1]");
        }
        return layout.stream()
                .filter(zone -> zone.contains(aimX, aimY))
                .map(Zone::sector)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Court layout has a gap at (" + aimX + ", " + aimY + ")"));
    }
}
