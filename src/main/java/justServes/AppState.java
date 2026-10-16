package justServes;

import justServes.models.ServeType;
import justServes.repo.Database;
import justServes.repo.GameRepository;
import justServes.repo.ServeRepository;
import justServes.services.GameService;

import java.util.Objects;

import static justServes.AppConstants.*;

public class AppState {
    private final Database database;
    private final GameService gameService;
    private int currentReceiverCount = DEFAULT_RECEIVER_COUNT;
    private ServeType currentServeType = DEFAULT_SERVE_TYPE;

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
