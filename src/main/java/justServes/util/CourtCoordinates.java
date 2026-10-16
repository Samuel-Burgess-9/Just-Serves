package justServes.util;

public final class CourtCoordinates {

    private CourtCoordinates() { }

    /**
     * Largest court rectangle with the given shape that fits inside the canvas, centred.
     * @param aspectRatio court width divided by court height
     */
    public static CourtBounds fit(double canvasWidth, double canvasHeight, double aspectRatio) {
        if (!(canvasWidth > 0) || !(canvasHeight > 0) || !(aspectRatio > 0)
                || !Double.isFinite(canvasWidth) || !Double.isFinite(canvasHeight)) {
            throw new IllegalArgumentException("Canvas size and aspect ratio must be positive");
        }
        double width = Math.min(canvasWidth, canvasHeight * aspectRatio);
        double height = width / aspectRatio;
        return new CourtBounds(
                (canvasWidth - width) / 2,
                (canvasHeight - height) / 2,
                width, height);
    }

    /** Pixel position to court coordinates. y is flipped: pixels grow downward, court y grows upward from the net. */
    public static CourtPoint toCourt(double px, double py, CourtBounds b) {
        if (!b.contains(px, py)) {
            throw new IllegalArgumentException("Point is outside the court");
        }
        double x = (px - b.left()) / b.width();
        double y = 1.0 - (py - b.top()) / b.height();
        // Floating-point rounding can land at 1.0000000000000002 on the far edge,
        // which Serve would reject, so clamp.
        return new CourtPoint(clamp01(x), clamp01(y));
    }

    public static double pixelX(double courtX, CourtBounds b) {
        return b.left() + courtX * b.width();
    }

    public static double pixelY(double courtY, CourtBounds b) {
        return b.top() + (1.0 - courtY) * b.height();
    }

    private static double clamp01(double v) {
        return Math.max(0.0, Math.min(1.0, v));
    }
}