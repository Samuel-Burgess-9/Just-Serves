package justServes.services;

import justServes.models.Game;
import justServes.models.Outcome;
import justServes.models.Serve;

import java.util.List;
import java.util.OptionalDouble;

/** What a game card shows: the game plus its headline numbers. */
public record GameSummary(Game game, int serves, int aces, int outs) {

    public GameSummary {
        if (serves < 0 || aces < 0 || outs < 0 || aces + outs > serves) {
            throw new IllegalArgumentException("Inconsistent serve counts");
        }
    }

    public static GameSummary of(Game game, List<Serve> serves) {
        int aces = (int) serves.stream().filter(s -> s.getOutcome() == Outcome.ACE).count();
        int outs = (int) serves.stream().filter(s -> s.getOutcome() == Outcome.OUT).count();
        return new GameSummary(game, serves.size(), aces, outs);
    }

    /** Share of serves that weren't OUT; empty when there are no serves. */
    public OptionalDouble accuracy() {
        return serves == 0 ? OptionalDouble.empty() : OptionalDouble.of((serves - outs) / (double) serves);
    }

    /** Share of serves that were aces; empty when there are no serves. */
    public OptionalDouble aceRate() {
        return serves == 0 ? OptionalDouble.empty() : OptionalDouble.of(aces / (double) serves);
    }
}