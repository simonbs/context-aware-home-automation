package aau.carma.ContextProviders;

import android.content.Context;
import android.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import aau.carmakit.ContextEngine.ContextOutcome;
import aau.carmakit.ContextEngine.ContextProvider;
import aau.carmakit.ContextEngine.ContextProviderListener;
import aau.carmakit.Database.DatabaseHelper;
import aau.carmakit.GestureConfiguration;
import aau.carmakit.ThreeDOneCentGestureRecognizer.recognizer.ThreeDMatch;
import aau.carmakit.Utilities.Logger;

/**
 * Provides the gesture context.
 */
public class GestureContextProvider implements ContextProvider {
    /**
     * Current set of outcomes.
     */
    private ArrayList<ContextOutcome> outcomes = new ArrayList<>();

    private Context context;

    public GestureContextProvider(Context context) {
        this.context = context;
    }

    /**
     * Configures the provider
     */
    public void configure(){}

    /**
     * Calculates the probabilities of gesture-bound actions based on the latest input gesture.
     * @param matches List of comparisons between input gesture and training templates.
     */
    public void calculateProbabilities(ArrayList<ThreeDMatch> matches){
        // A gesture score must be lower than or equal to the threshold in order to be considered.
        Double gestureScoreThreshold = 70.0;

        // Group the scores as each gesture appears multiple times,
        // i.e. one per training template.
        // Hashmap where keys are the label of gesture templates.
        // Values are pairs of an integer and a double. The integer is the
        // number of gesture templates with the label (the key) and the double
        // is the total score for the gesture template.
        HashMap<String, Pair<Integer, Double>> groupedScores = new HashMap<>();
        for (ThreeDMatch match : matches) {
            if (match.getScore() <= gestureScoreThreshold) {
                if (groupedScores.containsKey(match.getLabel())) {
                    Pair<Integer, Double> pair = groupedScores.get(match.getLabel());
                    Integer count = pair.first + 1;
                    Double newScore = pair.second + match.getScore();
                    groupedScores.put(match.getLabel(), new Pair<>(count, newScore));
                } else {
                    groupedScores.put(match.getLabel(), new Pair<>(1, match.getScore()));
                }
            }
        }

        // Average the grouped scores.
        HashMap<String, Double> averagedScores = new HashMap<>();
        for (Map.Entry<String, Pair<Integer, Double>> entry : groupedScores.entrySet()) {
            Pair<Integer, Double> pair = entry.getValue();
            averagedScores.put(entry.getKey(), pair.second.doubleValue() / pair.first.doubleValue());
        }

        // Calculate total score
        Double totalScore = 0.0;
        for (Map.Entry<String, Double> entry : averagedScores.entrySet()) {
            totalScore += entry.getValue();
        }

        // Log scores
        for (Map.Entry<String, Double> entry : averagedScores.entrySet()) {
            Logger.verbose("Gesture " + entry.getKey() + " has total score " + entry.getValue() + " / " + totalScore);
        }

        // Create outcomes
        ArrayList<ContextOutcome> outcomes = new ArrayList<>();
        ArrayList<GestureConfiguration> gestureConfigurations = DatabaseHelper.getInstance(context).getAllGestureConfiguration();
        for (GestureConfiguration gestureConfiguration : gestureConfigurations) {
            for (Map.Entry<String, Double> entry : averagedScores.entrySet()) {
                String gestureLabel = entry.getKey();
                Double gestureScore = entry.getValue();

                if (gestureConfiguration.gestureId.equals(gestureLabel)) {
                    // We only have one score, so it must have a probability of 1.
                    // If we don't do that, we calculate the score, a total score and
                    // calculate the probability as (1 - score / totalScore) resulting
                    // in a probability of 0.
                    double probability;
                    if (averagedScores.size() == 1) {
                        probability = 1;
                    } else {
                        // Subtract from one. The lower the score, the better.
                        probability = 1 - (gestureScore / totalScore);
                    }

                    Logger.verbose("Gesture probability for " + gestureConfiguration.id + ": " + probability);
                    outcomes.add(new ContextOutcome(gestureConfiguration.id, probability));
                }
            }
        }

        this.outcomes = ContextOutcome.normalizeOutcomes(outcomes);
    }

    @Override
    public double getWeight() {
        return 0.6;
    }

    @Override
    public void getContext(ContextProviderListener listener) {
        listener.onContextReady(this.outcomes);
    }

    @Override
    public void cancel() { }

    /**
     * Logs the current outcomes. For debugging purposes.
     */
    private void logCurrentOutcomes() {
        for (ContextOutcome outcome : outcomes) {
            Logger.verbose(outcome.id + ": " + outcome.probability);
        }
    }
}
