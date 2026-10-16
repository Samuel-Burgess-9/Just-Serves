package justServes.services;

public record Zone(Sector sector, double xMin, double xMax, double yMin, double yMax) {

    public boolean contains(double x, double y) {
        return x >= xMin
                && (x < xMax || (xMax == 1.0 && x <= 1.0))
                && y >=yMin
                && (y < yMax || (yMax == 1.0 && y <= 1.0));
    }
}
