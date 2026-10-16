package justServes.services;

import java.util.List;

import static justServes.AppConstants.RECEIVER_MAX;
import static justServes.AppConstants.RECEIVER_MIN;

/**
 * class to get co-ordinate zones from sectors. Uses reciever numbers to compute zones.
 * Coordinates are from the server's perspective,
 * looking towards the opponent's court.
 * x = 0: left sideline
 * x = 1: right sideline
 * y = 0: net
 * y = 1: opponent's back/end line
 * Each rectangle is half-open at its maximum boundary,
 * except at the court's outer edge (1.0).
 **/
public class ReceiveLayouts {

    private static final List<Zone> DEFAULT_LAYOUT = List.of(

            // Front corners, next to the net (y = 0)
            new Zone(Sector.FRONT_LEFT,
                    0.0, 0.1, 0.0, 0.1),

            new Zone(Sector.FRONT_RIGHT,
                    0.9, 1.0, 0.0, 0.1),

            // Net, excluding the corners
            new Zone(Sector.NET,
                    0.1, 0.9, 0.0, 0.1),

            // Front side-line sections
            new Zone(Sector.LEFT_LINE_FRONT,
                    0.0, 0.1, 0.1, 0.5),

            new Zone(Sector.RIGHT_LINE_FRONT,
                    0.9, 1.0, 0.1, 0.5),

            // Main centre zone
            new Zone(Sector.CENTRE,
                    0.1, 0.9, 0.1, 0.9),

            // Back side-line sections
            new Zone(Sector.LEFT_LINE_BACK,
                    0.0, 0.1, 0.5, 0.9),

            new Zone(Sector.RIGHT_LINE_BACK,
                    0.9, 1.0, 0.5, 0.9),

            // Back corners, next to the end line (y = 1)
            new Zone(Sector.BACK_LEFT,
                    0.0, 0.1, 0.9, 1.0),

            new Zone(Sector.BACK_RIGHT,
                    0.9, 1.0, 0.9, 1.0),

            // End line, excluding the corners
            new Zone(Sector.END_LINE,
                    0.1, 0.9, 0.9, 1.0)
    );

    /**
     * Returns the zones for the layout of given number of receivers
     * @param receiverCount number of opposition receiving players
     * @return a list of zones.
     */
    public static List<Zone> layoutFor(int receiverCount) {
        if (receiverCount < RECEIVER_MIN || receiverCount > RECEIVER_MAX) {
            throw new IllegalArgumentException(
                    "receiverCount must be between 1 and 6"
            );
        }
        return DEFAULT_LAYOUT;
    }
}
