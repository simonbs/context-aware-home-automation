package aau.carma.ContextProviders;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import aau.carma.Configuration;
import aau.carma.ContextEngine.ContextOutcome;
import aau.carma.ContextEngine.ContextProvider;
import aau.carma.ContextEngine.ContextProviderListener;
import aau.carma.Database.DatabaseHelper;
import aau.carma.DummyData;
import aau.carma.GestureConfiguration;
import aau.carma.Library.Logger;
import aau.carma.ThreeDOneCentGestureRecognizer.recognizer.ThreeDMatch;

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
        Double gestureScoreThreshold = 40.0;

        // Group the scores as each gesture appears multiple times,
        // i.e. one per training template.
        HashMap<String, Double> groupedScores = new HashMap<>();
        for (ThreeDMatch match : matches) {
            if (match.getScore() <= gestureScoreThreshold) {
                if (groupedScores.containsKey(match.getLabel())) {
                    Double newScore = groupedScores.get(match.getLabel()) + match.getScore();
                    groupedScores.put(match.getLabel(), newScore);
                } else {
                    groupedScores.put(match.getLabel(), match.getScore());
                }
            }
        }

        // Calculate total score
        Double totalScore = 0.0;
        for (Map.Entry<String, Double> entry : groupedScores.entrySet()) {
            totalScore += entry.getValue();
        }

        // Log scores
        for (Map.Entry<String, Double> entry : groupedScores.entrySet()) {
            Logger.verbose("Gesture " + entry.getKey() + " has total score " + entry.getValue() + " / " + totalScore);
        }

        ArrayList<ContextOutcome> outcomes = new ArrayList<>();
        ArrayList<GestureConfiguration> gestureConfigurations = DatabaseHelper.getInstance(context).getAllGestureConfiguration();
        for (GestureConfiguration gestureConfiguration : gestureConfigurations) {
            for (Map.Entry<String, Double> entry : groupedScores.entrySet()) {
                String gestureLabel = entry.getKey();
                Double gestureScore = entry.getValue();

                if (gestureConfiguration.gestureId.equals(gestureLabel)) {
                    // Subtract from one. The lower the score, the better.
                    double probability = 1 - (gestureScore / totalScore);
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
