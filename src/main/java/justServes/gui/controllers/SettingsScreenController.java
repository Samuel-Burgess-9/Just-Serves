package justServes.gui.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import justServes.AppState;
import justServes.gui.theme.AppTheme;
import justServes.gui.theme.AppThemes;

public class SettingsScreenController implements ScreenController {

    @FXML private FlowPane themeBox;
    @FXML private Button exitButton;

    private final ToggleGroup themeGroup = new ToggleGroup();
    private AppState state;
    private boolean syncing;   // true while the screen is just reflecting the current theme

    @FXML
    private void initialize() {
        for (AppTheme theme : AppThemes.ALL) {
            Circle swatch = new Circle(7, theme.court().surface());
            swatch.setStroke(Color.color(0, 0, 0, 0.2));

            ToggleButton button = new ToggleButton(theme.name(), swatch);
            button.setUserData(theme);
            button.setToggleGroup(themeGroup);
            themeBox.getChildren().add(button);
        }

        themeGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (syncing) {
                return;
            }
            if (newToggle == null) {                  // clicking the selected one must not leave it empty
                if (oldToggle != null) {
                    themeGroup.selectToggle(oldToggle);
                }
                return;
            }
            state.setTheme((AppTheme) newToggle.getUserData());
        });

        exitButton.setOnAction(event -> Platform.exit());   // App.stop() then closes the database
    }

    @Override
    public void init(AppState state) {
        this.state = state;
        showCurrentTheme();
    }

    @Override
    public void onShow() {
        showCurrentTheme();
    }

    private void showCurrentTheme() {
        syncing = true;
        try {
            for (Toggle toggle : themeGroup.getToggles()) {
                if (toggle.getUserData().equals(state.getTheme())) {
                    themeGroup.selectToggle(toggle);
                }
            }
        } finally {
            syncing = false;
        }
    }
}