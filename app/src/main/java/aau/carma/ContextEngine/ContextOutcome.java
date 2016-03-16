package aau.carma.ContextEngine;

/**
 * Represents a possible outcome recognized of running the context engine.
 */
public class ContextOutcome {
    /**
     * ID of the represented outcome.
     */
    private int id;

    /**
     * Probability of the outcome.
     */
    private double probability;

    /**
     * Initialies a representation of a possible outcome.
     * @param id ID of the represented outcome.
     * @param probability Probability of the outcome.
     */
    public ContextOutcome(int id, double probability) {

    }

    /**
     * Identifies the event represented by the identity.
     * This ID should be the same for context entities
     * that represent the same event.
     * @return ID of the represented outcome.
     */
    int getId() {
        return id;
    }

    /**
     * Probability of some event occurring in the context from
     * which this entity was created, i.e. the context provider.
     * @return Probability of the represented outcome.
     */
    double getProbability() {
        return probability;
    }
}
