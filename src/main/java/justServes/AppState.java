package justServes;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import justServes.gui.theme.AppTheme;
import justServes.gui.theme.AppThemes;
import justServes.models.ServeType;
import justServes.repo.Database;
import justServes.repo.GameRepository;
import justServes.repo.ServeRepository;
import justServes.services.GameService;

import java.util.List;
import java.util.Objects;

import static justServes.AppConstants.*;
import static justServes.gui.theme.AppThemes.NATIONALS;
import static justServes.gui.theme.AppThemes.PRACTICE;

public class AppState {
    private final Database database;
    private final GameService gameService;
    private int currentReceiverCount = DEFAULT_RECEIVER_COUNT;
    private ServeType currentServeType = DEFAULT_SERVE_TYPE;
    private final ObjectProperty<AppTheme> theme = new SimpleObjectProperty<>(AppThemes.DEFAULT);

    public AppState(Database database) {
        this.database = database;
        GameRepository games = new GameRepository(database);
        ServeRepository serves = new ServeRepository(database);
        this.gameService = new GameService(games, serves);
    }

    public GameService getGameService() {
        return this.gameService;
    }

    public int getCurrentReceiverCount() {
        return this.currentReceiverCount;
    }

    public void setCurrentReceiverCount(int currentReceiverCount) {
        if (currentReceiverCount < RECEIVER_MIN || currentReceiverCount > RECEIVER_MAX) {
            throw new IllegalArgumentException("receiverCount must be between " + RECEIVER_MIN + " and " + RECEIVER_MAX);
        }
        this.currentReceiverCount = currentReceiverCount;
    }

    public void setTheme(AppTheme theme) {
        this.theme.set(Objects.requireNonNull(theme, "theme must not be null"));
    }

    public ObjectProperty<AppTheme> themeProperty() {
        return theme;
    }

    public AppTheme getTheme() {
        return this.theme.get();
    }

    public ServeType getCurrentServeType() {
        return currentServeType;
    }

    public void setCurrentServeType(ServeType currentServeType) {
        this.currentServeType = Objects.requireNonNull(currentServeType, "Serve type must not be null");
    }

    public void close() {
        this.database.close();
    }

}
