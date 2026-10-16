package justServes.services;

public class SectorService {

    public Sector sectorOf(double aimX, double aimY, int receiverCount) {
        if (!Double.isFinite(aimX) || !Double.isFinite(aimY)
                || aimX < 0.0 || aimX > 1.0
                || aimY < 0.0 || aimY > 1.0) {
            throw new IllegalArgumentException(
                    "Coordinates must be finite and within [0, 1]"
            );
        } else {
            return ReceiverLayouts.layoutFor(receiverCount).stream().filter(zone -> zone.contains(aimX, aimY)).map(Zone::sector).findFirst().orElseThrow(() -> new IllegalStateException("Court layout has a gap at (" + aimX + ", " + aimY + ")"));
        }
    }
}
