package justServes.repo;

import justServes.models.Game;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GameRepository {

    private final Database database;

    public GameRepository(Database database) {
        this.database = database;
    }

    /** Starts a new game and returns it with its generated id. */
    public Game create() {
        Instant now = Instant.now();
        String sql = "INSERT INTO game (started_at) VALUES (?)";
        try (PreparedStatement ps = database.getConnection()
                .prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, now.toString());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new RepositoryException("No id returned for new game", null);
                }
                Game game = new Game();
                game.setId(keys.getInt(1));
                game.setStartedAt(now);
                return game;
            }
        } catch (SQLException e) {
            throw new RepositoryException("Could not create game", e);
        }
    }

    public Optional<Game> findById(int id) {
        String sql = "SELECT id, started_at FROM game WHERE id = ?";
        try (PreparedStatement ps = database.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new RepositoryException("Could not load game " + id, e);
        }
    }

    public Optional<Game> findLatest() {
        String sql = "SELECT id, started_at FROM game ORDER BY id DESC LIMIT 1";
        try (PreparedStatement ps = database.getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? Optional.of(map(rs)) : Optional.empty();
        } catch (SQLException e) {
            throw new RepositoryException("Could not load latest game", e);
        }
    }

    public List<Game> findAll() {
        String sql = "SELECT id, started_at FROM game ORDER BY id";
        List<Game> games = new ArrayList<>();
        try (PreparedStatement ps = database.getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                games.add(map(rs));
            }
            return games;
        } catch (SQLException e) {
            throw new RepositoryException("Could not load games", e);
        }
    }

    private Game map(ResultSet rs) throws SQLException {
        Game game = new Game();
        game.setId(rs.getInt("id"));
        game.setStartedAt(Instant.parse(rs.getString("started_at")));
        return game;
    }
}