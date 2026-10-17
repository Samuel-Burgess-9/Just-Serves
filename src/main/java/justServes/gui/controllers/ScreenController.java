package justServes.gui.controllers;

import justServes.AppState;

/** What the shell needs from every screen it can show. */
public interface ScreenController {

    /** Called once, right after the screen's FXML has loaded. */
    void init(AppState state);

    /** Called every time the screen becomes visible, so it can refresh stale data. */
    void onShow();
}