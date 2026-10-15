package justServes.repo;

import justServes.models.Outcome;
import justServes.models.Serve;
import justServes.models.ServeType;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ServeRepository {

    private static final String COLUMNS =
            "id, game_id, receiver_count, aim_x, aim_y, outcome, serve_type";

    private final Database database;

    public ServeRepository(Database database) {
        this.database = database;
    }

    /** Saves the serve and sets its generated id on the object you pass in. */
    public void save(Serve serve) {
        String sql = "INSERT INTO serve "
                + "(game_id, receiver_count, aim_x, aim_y, outcome, serve_type) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = database.getConnection()
                .prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, serve.getGameId());
            ps.setInt(2, serve.getReceiverCount());
            ps.setDouble(3, serve.getAimX());
            ps.setDouble(4, serve.getAimY());
            ps.setString(5, serve.getOutcome().name());
            ps.setString(6, serve.getType().name());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    serve.setId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RepositoryException("Could not save serve", e);
        }
    }

    public List<Serve> findByGame(int gameId) {
        String sql = "SELECT " + COLUMNS + " FROM serve WHERE game_id = ? ORDER BY id";
        try (PreparedStatement ps = database.getConnection().prepareStatement(sql)) {
            ps.setInt(1, gameId);
            try (ResultSet rs = ps.executeQuery()) {
                return mapAll(rs);
            }
        } catch (SQLException e) {
            throw new RepositoryException("Could not load serves for game " + gameId, e);
        }
    }

    public List<Serve> findAll() {
        String sql = "SELECT " + COLUMNS + " FROM serve ORDER BY id";
        try (PreparedStatement ps = database.getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return mapAll(rs);
        } catch (SQLException e) {
            throw new RepositoryException("Could not load serves", e);
        }
    }

    /** The most recently saved serve, e.g. to seed the sticky settings on startup. */
    public Optional<Serve> findLatest() {
        String sql = "SELECT " + COLUMNS + " FROM serve ORDER BY id DESC LIMIT 1";
        try (PreparedStatement ps = database.getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? Optional.of(map(rs)) : Optional.empty();
        } catch (SQLException e) {
            throw new RepositoryException("Could not load latest serve", e);
        }
    }

    /** Returns true if a row was deleted (for an undo button). */
    public boolean deleteById(int id) {
        String sql = "DELETE FROM serve WHERE id = ?";
        try (PreparedStatement ps = database.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RepositoryException("Could not delete serve " + id, e);
        }
    }

    private List<Serve> mapAll(ResultSet rs) throws SQLException {
        List<Serve> serves = new ArrayList<>();
        while (rs.next()) {
            serves.add(map(rs));
        }
        return serves;
    }

    private Serve map(ResultSet rs) throws SQLException {
        Serve serve = new Serve();
        serve.setId(rs.getInt("id"));
        serve.setGameId(rs.getInt("game_id"));
        serve.setReceiverCount(rs.getInt("receiver_count"));
        serve.setAimX(rs.getDouble("aim_x"));
        serve.setAimY(rs.getDouble("aim_y"));
        serve.setOutcome(Outcome.valueOf(rs.getString("outcome")));
        serve.setType(ServeType.valueOf(rs.getString("serve_type")));
        return serve;
    }
}