package aau.carmakit.ContextEngine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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

        if (totalProbability == 0) {
            return new ArrayList<>();
        }

        ArrayList<ContextOutcome> result = new ArrayList<>();
        for (ContextOutcome outcome : outcomes) {
            result.add(new ContextOutcome(outcome.id, outcome.probability / totalProbability));
        }

        return result;
    }

    /**
     * Sums a set of outcomes, removing all duplicates and adding their probabilities together.
     * Please note that the outcomes are not normalized. Use the normalizeOutcomes function
     * to normalize the outcomes.
     * @param outcomes Outcomes to sum.
     * @return Summed outcomes.
     */
    public static ArrayList<ContextOutcome> sumOutcomes(ArrayList<ContextOutcome> outcomes) {
        HashMap<String, Double> probabilityMap = new HashMap<>();
        for (ContextOutcome outcome : outcomes) {
            if (probabilityMap.containsKey(outcome.id)) {
                Double newProbability = probabilityMap.get(outcome.id) + outcome.probability;
                probabilityMap.put(outcome.id, newProbability);
            } else {
                probabilityMap.put(outcome.id, outcome.probability);
            }
        }

        ArrayList<ContextOutcome> result = new ArrayList<>();
        for (Map.Entry<String, Double> entry : probabilityMap.entrySet()) {
            String id = entry.getKey();
            Double probability = entry.getValue();
            result.add(new ContextOutcome(id, probability));
        }

        return result;
    }
}
