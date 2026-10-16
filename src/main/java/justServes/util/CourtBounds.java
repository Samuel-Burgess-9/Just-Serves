package justServes.util;

public record CourtBounds(double left, double top, double width, double height) {

    public CourtBounds {
        if (!Double.isFinite(left) || !Double.isFinite(top)
                || !Double.isFinite(width) || !Double.isFinite(height)
                || width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Invalid court bounds");
        }
    }

    /** True if the pixel position is on the court (not in a margin). NaN gives false. */
    public boolean contains(double px, double py) {
        return px >= left && px <= left + width
                && py >= top && py <= top + height;
    }
}