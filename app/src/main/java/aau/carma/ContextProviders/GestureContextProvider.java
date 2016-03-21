package aau.carma.ContextProviders;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;

import aau.carma.Configuration;
import aau.carma.ContextEngine.ContextOutcome;
import aau.carma.ContextEngine.ContextProvider;
import aau.carma.ContextEngine.ContextProviderListener;
import aau.carma.DummyData;
import aau.carma.GestureConfiguration;
import aau.carma.RoomsManager;
import aau.carma.ThreeDOneCentGestureRecognizer.recognizer.ThreeDMatch;

/**
 * Provides the gesture context.
 */
public class GestureContextProvider implements ContextProvider {

    /**
     * Current set of outcomes.
     */
    private ArrayList<ContextOutcome> outcomes = new ArrayList<>();

    /**
     * Configures the provider
     */
    public void configure(){}

    /**
     * Calculates the probabilities of gesture-bound actions based on the latest input gesture.
     * @param matches List of comparisons between input gesture and training templates.
     */
    public void calculateProbabilities(ArrayList<ThreeDMatch> matches){
        double totalScore = 0;
        for (ThreeDMatch match : matches){
            totalScore += match.getScore();
        }

        ArrayList<ContextOutcome> outcomes = new ArrayList<>();
        ArrayList<GestureConfiguration> gestureConfigurations = DummyData.getAllGestureConfigurations();
        for (GestureConfiguration gestureConfiguration : gestureConfigurations) {
            for(ThreeDMatch match : matches){
                if (gestureConfiguration.gestureId.equals(match.getLabel())){
                    double probability = match.getScore() / totalScore;
                    outcomes.add(new ContextOutcome(gestureConfiguration.actionId, probability));
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
        logCurrentOutcomes();
        listener.onContextReady(this.outcomes);
    }

    @Override
    public void cancel() { }

    /**
     * Logs the current outcomes. For debugging purposes.
     */
    private void logCurrentOutcomes() {
        for (ContextOutcome outcome : outcomes) {
            Log.v(Configuration.Log, outcome.id + ": " + outcome.probability);
        }
    }
}
