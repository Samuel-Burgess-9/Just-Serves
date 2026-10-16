package justServes.services;

import justServes.models.Game;
import justServes.models.Outcome;
import justServes.models.Serve;
import justServes.models.ServeType;
import justServes.repo.GameRepository;
import justServes.repo.ServeRepository;

import java.util.List;
import java.util.Optional;

public class GameService {

    private final GameRepository games;
    private final ServeRepository serves;
    private Game currentGame;

    public GameService(GameRepository games, ServeRepository serves) {
        this.games = games;
        this.serves = serves;
    }

    public Game startGame()                // creates a game, makes it current, returns it
    {
        this.currentGame = games.create();
        return this.currentGame;
    }

    public Game getCurrentGame()  { // throws IllegalStateException if no game started
        if (currentGame == null) {
            throw new IllegalStateException("No game has been started");
        }
        return this.currentGame;
    }
    public Serve logServe(double aimX, double aimY, Outcome outcome,
                          int receiverCount, ServeType serveType) {
        Serve currentServe = new Serve();
        currentServe.setGameId(getCurrentGame().getId());
        currentServe.setAimX(aimX);
        currentServe.setAimY(aimY);
        currentServe.setOutcome(outcome);
        currentServe.setReceiverCount(receiverCount);
        currentServe.setType(serveType);
        serves.save(currentServe);
        currentGame.log(currentServe);
        return currentServe;
    }

    public Optional<Serve> undoLastServe() {
        List<Serve> gameServes = getCurrentGame().getServes();
        if (gameServes.isEmpty()) {
            return Optional.empty();
        }
        Serve last = gameServes.getLast();
        serves.deleteById(last.getId());   // database first: if this throws, memory is unchanged
        gameServes.remove(last);
        return Optional.of(last);
    }
}

