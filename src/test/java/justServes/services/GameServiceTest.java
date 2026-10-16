package justServes.services;

import justServes.models.Game;
import justServes.models.Outcome;
import justServes.models.Serve;
import justServes.models.ServeType;
import justServes.repo.Database;
import justServes.repo.GameRepository;
import justServes.repo.ServeRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class GameServiceTest {

    private Database db;
    private GameRepository gameRepo;
    private ServeRepository serveRepo;
    private GameService service;

    @BeforeEach
    void setUp() {
        db = Database.inMemory();
        gameRepo = new GameRepository(db);
        serveRepo = new ServeRepository(db);
        service = new GameService(gameRepo, serveRepo);
    }

    @AfterEach
    void tearDown() {
        db.close();
    }

    /** Logs a valid serve; only aimX varies so serves can be told apart. */
    private Serve logServeAt(double aimX) {
        return service.logServe(aimX, 0.5, Outcome.ACE, 2, ServeType.TOPSPIN);
    }

    @Test
    void startGameSavesAGameAndMakesItCurrent() {
        Game game = service.startGame();

        assertTrue(game.getId() > 0);
        assertTrue(game.getServes().isEmpty());
        assertSame(game, service.getCurrentGame());
        assertTrue(gameRepo.findById(game.getId()).isPresent());
    }

    @Test
    void getCurrentGameBeforeStartThrows() {
        assertThrows(IllegalStateException.class, () -> service.getCurrentGame());
    }

    @Test
    void logServeIsStoredInDatabaseAndGameList() {
        Game game = service.startGame();

        Serve logged = service.logServe(0.25, 0.75, Outcome.SHAKY, 3, ServeType.FLOAT);

        assertTrue(logged.getId() > 0);
        assertEquals(1, game.getServes().size());
        assertSame(logged, game.getServes().get(0));

        List<Serve> saved = serveRepo.findByGame(game.getId());
        assertEquals(1, saved.size());
        assertEquals(0.25, saved.get(0).getAimX());
        assertEquals(0.75, saved.get(0).getAimY());
        assertEquals(Outcome.SHAKY, saved.get(0).getOutcome());
        assertEquals(3, saved.get(0).getReceiverCount());
        assertEquals(ServeType.FLOAT, saved.get(0).getType());
    }

    @Test
    void logServeBeforeStartGameThrows() {
        assertThrows(IllegalStateException.class, () -> logServeAt(0.5));
    }

    @Test
    void invalidServeChangesNothing() {
        Game game = service.startGame();

        assertThrows(IllegalArgumentException.class,
                () -> service.logServe(1.5, 0.5, Outcome.ACE, 2, ServeType.TOPSPIN));

        assertTrue(game.getServes().isEmpty());
        assertTrue(serveRepo.findByGame(game.getId()).isEmpty());
    }

    @Test
    void undoRemovesTheLastServeFromBothPlaces() {
        Game game = service.startGame();
        logServeAt(0.1);
        logServeAt(0.2);
        logServeAt(0.3);

        Optional<Serve> undone = service.undoLastServe();

        assertTrue(undone.isPresent());
        assertEquals(0.3, undone.get().getAimX());

        assertEquals(2, game.getServes().size());

        List<Serve> saved = serveRepo.findByGame(game.getId());
        assertEquals(2, saved.size());
        assertEquals(0.1, saved.get(0).getAimX());
        assertEquals(0.2, saved.get(1).getAimX());
    }

    @Test
    void undoWithNothingLoggedReturnsEmpty() {
        service.startGame();

        assertTrue(service.undoLastServe().isEmpty());
    }

    @Test
    void undoBeforeStartGameThrows() {
        assertThrows(IllegalStateException.class, () -> service.undoLastServe());
    }

    @Test
    void undoingEverythingLeavesAnEmptyGame() {
        Game game = service.startGame();
        logServeAt(0.1);
        logServeAt(0.2);

        service.undoLastServe();
        service.undoLastServe();

        assertTrue(game.getServes().isEmpty());
        assertTrue(serveRepo.findByGame(game.getId()).isEmpty());
        assertTrue(service.undoLastServe().isEmpty());
    }

    @Test
    void resumeStartsAGameWhenNoneExists() {
        Game game = service.resumeOrStartGame();

        assertTrue(game.getId() > 0);
        assertSame(game, service.getCurrentGame());
    }

    @Test
    void resumeCarriesOnWithTheLatestGameAndItsServes() {
        Game original = service.startGame();
        logServeAt(0.1);
        logServeAt(0.2);

        GameService relaunched = new GameService(gameRepo, serveRepo);   // as if the app restarted
        Game resumed = relaunched.resumeOrStartGame();

        assertEquals(original.getId(), resumed.getId());
        assertEquals(2, resumed.getServes().size());
        assertEquals(0.1, resumed.getServes().get(0).getAimX());
        assertEquals(0.2, resumed.getServes().get(1).getAimX());
        assertEquals(1, gameRepo.findAll().size());
    }

    @Test
    void resumeReusesAnEmptyLatestGame() {
        service.startGame();

        new GameService(gameRepo, serveRepo).resumeOrStartGame();

        assertEquals(1, gameRepo.findAll().size());
    }

    @Test
    void newGameKeepsTheCurrentGameWhileItIsEmpty() {
        Game first = service.startGame();

        assertSame(first, service.newGame());
        assertEquals(1, gameRepo.findAll().size());
    }

    @Test
    void newGameStartsAnotherOnceServesExist() {
        Game first = service.startGame();
        logServeAt(0.3);

        Game second = service.newGame();

        assertNotEquals(first.getId(), second.getId());
        assertSame(second, service.getCurrentGame());
        assertTrue(second.getServes().isEmpty());
        assertEquals(1, serveRepo.findByGame(first.getId()).size());
    }
}