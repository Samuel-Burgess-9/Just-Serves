package justServes.models;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Class containing all the serves of one game.
 * @author Samuel Burgess
 */
public class Game {
    private int id;
    private Instant startedAt;
    private List<Serve> serves =  new ArrayList<>();

    public void setId(int id) {
        this.id = id;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public void setServes(ArrayList<Serve> serves) {
        this.serves = serves;
    }

    public List<Serve> getServes() {
        return serves;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public int getId() {
        return id;
    }
}
