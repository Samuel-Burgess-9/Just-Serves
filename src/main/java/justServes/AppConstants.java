package justServes;

import justServes.models.ServeType;

public final class AppConstants {

    private AppConstants() {}

    public static final String APP_NAME = "Just Serves";

    public static final int RECEIVER_MIN = 1;
    public static final int RECEIVER_MAX = 6;
    public static final int DEFAULT_RECEIVER_COUNT = 1;
    public static final ServeType DEFAULT_SERVE_TYPE = ServeType.TOPSPIN;
    public static final double COURT_ASPECT_RATIO = 1.0;
    public static final int MIN_WINDOW_WIDTH = 900;
    public static final int MIN_WINDOW_HEIGHT = 650;
}
