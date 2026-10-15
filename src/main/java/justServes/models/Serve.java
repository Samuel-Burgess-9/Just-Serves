package justServes.models;

import java.util.Objects;

/**
 * Stores the serve data
 * @author Samuel Burgess
 */
public class Serve {
    private int id;
    private int gameId;
    private int receiverCount;
    private double aimX;
    private double aimY;
    private Outcome outcome;
    private ServeType type;

    public void setId(int id) {
        this.id = id;
    }

    public void setGameId(int gameId) {
        this.gameId = gameId;
    }

    public void setReceiverCount(int receiverCount) {
        if (receiverCount < 1 || receiverCount > 6) {
            throw new IllegalArgumentException("receiverCount must be between 1 and 6");
        }
        this.receiverCount = receiverCount;
    }

    public void setAimX(double aimX) {
        if (!(aimX >= 0.0 && aimX <= 1.0)) {
            throw new IllegalArgumentException("aimX must be between 0 and 1");
        }
        this.aimX = aimX;
    }

    public void setAimY(double aimY) {
        if (!(aimY >= 0.0 && aimY <= 1.0)) {
            throw new IllegalArgumentException("aimY must be between 0 and 1");
        }
        this.aimY = aimY;
    }

    public void setOutcome(Outcome outcome) {
        this.outcome = Objects.requireNonNull(outcome, "Outcome is required");
    }

    public void setType(ServeType type) {
        this.type = Objects.requireNonNull(type, "Serve type is required");
    }

    public int getId() {
        return id;
    }

    public int getGameId() {
        return gameId;
    }

    public int getReceiverCount() {
        return receiverCount;
    }

    public double getAimX() {
        return aimX;
    }

    public double getAimY() {
        return aimY;
    }

    public Outcome getOutcome() {
        return outcome;
    }

    public ServeType getType() {
        return type;
    }
}
