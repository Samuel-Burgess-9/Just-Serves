package justServes.gui.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import justServes.AppState;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class ShellController {

    private enum ScreenId {
        RECORD("/fxml/record-screen.fxml"),
        ANALYZE("/fxml/analyze-screen.fxml"),
        STATS("/fxml/stats-screen.fxml");

        private final String fxml;

        ScreenId(String fxml) {
            this.fxml = fxml;
        }
    }

    private record LoadedScreen(Parent node, ScreenController controller) { }

    @FXML private BorderPane root;
    @FXML private StackPane contentHolder;
    @FXML private ToggleGroup navGroup;
    @FXML private ToggleButton recordNav, analyzeNav, statsNav;
    @FXML private Button exitButton;

    private final Map<ToggleButton, ScreenId> tabs = new LinkedHashMap<>();
    private final Map<ScreenId, LoadedScreen> loaded = new EnumMap<>(ScreenId.class);
    private AppState state;

    @FXML
    private void initialize() {
        exitButton.setOnAction(e -> Platform.exit());   // App.stop() then closes the database
    }

    public void init(AppState state) {
        this.state = state;
        state.themeProperty().addListener((obs, old, t) -> root.setStyle(t.palette().toStyle()));
        root.setStyle(state.getTheme().palette().toStyle());

        tabs.put(recordNav, ScreenId.RECORD);
        tabs.put(analyzeNav, ScreenId.ANALYZE);
        tabs.put(statsNav, ScreenId.STATS);

        navGroup.selectedToggleProperty().addListener((obs, oldT, newT) -> {
            if (newT == null) {
                navGroup.selectToggle(oldT);   // one tab must always be selected
                return;
            }
            ScreenId id = tabs.get((ToggleButton) newT);
            if (id != null) {
                show(id);
            }
        });

        show(ScreenId.RECORD);   // recordNav is already selected in the FXML, so no event fires for it
    }

    private void show(ScreenId id) {
        LoadedScreen screen = loaded.computeIfAbsent(id, this::load);   // loaded once, then kept
        contentHolder.getChildren().setAll(screen.node());
        screen.controller().onShow();
    }

    private LoadedScreen load(ScreenId id) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(id.fxml));
            Parent node = loader.load();
            ScreenController controller = loader.getController();
            controller.init(state);
            return new LoadedScreen(node, controller);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to load " + id.fxml, e);
        }
    }
}