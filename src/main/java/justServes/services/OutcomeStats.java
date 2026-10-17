package justServes.services;

import justServes.models.Outcome;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/** How many serves had each outcome. Every outcome is always present, even when zero. */
public record OutcomeStats(Map<Outcome, Integer> counts) {

    public OutcomeStats {
        Objects.requireNonNull(counts, "counts");
        Map<Outcome, Integer> complete = new EnumMap<>(Outcome.class);
        for (Outcome outcome : Outcome.values()) {
            complete.put(outcome, counts.getOrDefault(outcome, 0));
        }
        counts = Collections.unmodifiableMap(complete);
    }

    public int count(Outcome outcome) {
        return counts.get(outcome);
    }

    public int total() {
        return counts.values().stream().mapToInt(Integer::intValue).sum();
    }

    /** Share of the serves with this outcome, 0.0 when there are no serves (so a bar just sits empty). */
    public double share(Outcome outcome) {
        int total = total();
        return total == 0 ? 0.0 : count(outcome) / (double) total;
    }
}