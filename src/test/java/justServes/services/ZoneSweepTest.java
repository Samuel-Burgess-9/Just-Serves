package justServes.services;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ZoneSweepTest {
    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4, 5, 6})
    void everyPointFallsInExactlyOneZone(int receivers) {
        ReceiveLayouts ReceiveLayouts = new ReceiveLayouts();
        List<Zone> zones = ReceiveLayouts.layoutFor(receivers);
        for (int i = 0; i <= 200; i++) {
            for (int j = 0; j <= 200; j++) {
                double x = i / 200.0;
                double y = j / 200.0;
                long matches = zones.stream().filter(z -> z.contains(x, y)).count();
                assertEquals(1, matches, "at (" + x + ", " + y + ")");
            }
        }
    }
}
