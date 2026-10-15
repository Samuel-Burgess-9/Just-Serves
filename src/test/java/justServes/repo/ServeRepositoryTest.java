package justServes.repo;

import justServes.models.Game;
import justServes.models.Outcome;
import justServes.models.Serve;
import justServes.models.ServeType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ServeRepositoryTest {

    private Serve newServe(int gameId) {
        Serve serve = new Serve();
        serve.setGameId(gameId);
        serve.setReceiverCount(2);
        serve.setAimX(0.25);
        serve.setAimY(0.75);
        serve.setOutcome(Outcome.ACE);
        serve.setType(ServeType.TOPSPIN);
        return serve;
    }

    @Test
    void savedServeCanBeLoadedBack() {
        try (Database db = Database.inMemory()) {
            Game game = new GameRepository(db).create();
            ServeRepository repo = new ServeRepository(db);

            Serve serve = newServe(game.getId());
            repo.save(serve);
            assertTrue(serve.getId() > 0);

            List<Serve> loaded = repo.findByGame(game.getId());
            assertEquals(1, loaded.size());
            assertEquals(0.25, loaded.get(0).getAimX());
            assertEquals(Outcome.ACE, loaded.get(0).getOutcome());
            assertEquals(ServeType.TOPSPIN, loaded.get(0).getType());
        }
    }

    @Test
    void deleteRemovesTheServe() {
        try (Database db = Database.inMemory()) {
            Game game = new GameRepository(db).create();
            ServeRepository repo = new ServeRepository(db);
            Serve serve = newServe(game.getId());
            repo.save(serve);

            assertTrue(repo.deleteById(serve.getId()));
            assertTrue(repo.findByGame(game.getId()).isEmpty());
        }
    }

    @Test
    void serveForMissingGameIsRejected() {
        try (Database db = Database.inMemory()) {
            assertThrows(RepositoryException.class,
                    () -> new ServeRepository(db).save(newServe(999)));
        }
    }
}