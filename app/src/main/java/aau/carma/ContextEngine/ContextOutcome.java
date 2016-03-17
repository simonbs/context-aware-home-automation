package aau.carma.ContextEngine;

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
}
