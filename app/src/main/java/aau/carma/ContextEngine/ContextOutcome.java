package aau.carma.ContextEngine;

import java.util.ArrayList;

/**
 * Represents a possible outcome recognized of running the context engine.
 */
public class ContextOutcome {
    /**
     * ID of the represented outcome.
     */
    public final String id;

    /**
     * Probability of the outcome.
     */
    public final double probability;

    /**
     * Initialies a representation of a possible outcome.
     * @param id ID of the represented outcome.
     * @param probability Probability of the outcome.
     */
    public ContextOutcome(String id, double probability) {
        this.id = id;
        this.probability = probability;
    }

    /**
     * Normalizes a set of outcomes.
     * @param outcomes
     * @return
     */
    public static ArrayList<ContextOutcome> normalizeOutcomes(ArrayList<ContextOutcome> outcomes) {
        if (outcomes.size() == 0) {
            return new ArrayList<>();
        }

        double totalProbability = 0;
        for (ContextOutcome outcome : outcomes) {
            totalProbability += outcome.probability;
        }

        ArrayList<ContextOutcome> result = new ArrayList<>();
        for (ContextOutcome outcome : outcomes) {
            result.add(new ContextOutcome(outcome.id, outcome.probability / totalProbability));
        }

        return result;
    }
}
