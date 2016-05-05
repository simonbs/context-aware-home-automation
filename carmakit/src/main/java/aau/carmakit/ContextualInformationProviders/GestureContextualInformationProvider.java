package aau.carmakit.ContextualInformationProviders;

import android.content.Context;
import android.gesture.Gesture;
import android.util.Pair;

import com.android.internal.util.Predicate;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import aau.carmakit.ContextEngine.ContextOutcome;
import aau.carmakit.ContextEngine.ContextualInformationListener;
import aau.carmakit.ContextEngine.ContextualInformationProvider;
import aau.carmakit.ContextEngine.ProvidedContextualInformation;
import aau.carmakit.ContextEngine.Recommender.jayes.BayesNet;
import aau.carmakit.ContextEngine.Recommender.jayes.BayesNode;
import aau.carmakit.Database.DatabaseHelper;
import aau.carmakit.GestureConfiguration;
import aau.carmakit.ThreeDOneCentGestureRecognizer.recognizer.ThreeDMatch;
import aau.carmakit.Utilities.Consumer;
import aau.carmakit.Utilities.Func;
import aau.carmakit.Utilities.Funcable;
import aau.carmakit.Utilities.Logger;
import aau.carmakit.Utilities.Optional;

/**
 * Provides contextual information related to the gesture performed by the user.
 */
public class GestureContextualInformationProvider implements ContextualInformationProvider {
    /**
     * Holds a reference to the listener to notify when the contextual information is ready.
     */
    private Optional<ContextualInformationListener> listener = new Optional<>();

    /**
     * Latest calculated probabilities for the gesture node.
     * Keys are the gesture IDs and values are the probabilities.
     */
    private Optional<HashMap<String, Double>> gestureProbabilities = new Optional<>();

    /**
     * Latest calculated probabilities for the gesture_action node.
     * Outer keys are the gesture IDs. Inner keys are the action IDs
     * and inner values are the probabilities.
     */
    private Optional<HashMap<String, HashMap<String, Double>>> gestureActionProbabilities = new Optional<>();

    /**
     * Computed evidence. Keys are gesture IDs and values are the probability for each action.
     */
    private Optional<HashMap<String, Double>> evidence = new Optional<>();

    /**
     * Context to read from the database in.
     */
    private final Context context;

    /**
     * Provides contextual information related to the gesture performed by the user.
     * @param context Context to read from the database in.
     */
    public GestureContextualInformationProvider(Context context) {
        this.context = context;
    }

    /**
     * Updates the probabilities based on a set of matched gestures.
     * @param matches Matched gestures.
     */
    public void updateProbabilities(ArrayList<ThreeDMatch> matches) {
        // A gesture score must be lower than or equal to the threshold in order to be considered.
        Double gestureScoreThreshold = 40.0;

        // Group the scores as each gesture appears multiple times,
        // i.e. one per training template.
        // Hash map where keys are the label of gesture templates.
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

        // Compute total score.
        Double totalScore = 0.0;
        for (Map.Entry<String, Double> entry : averagedScores.entrySet()) {
            totalScore += entry.getValue();
        }

        // Log averaged scores.
        for (Map.Entry<String, Double> entry : averagedScores.entrySet()) {
            Logger.verbose("Gesture " + entry.getKey() + " has a total score of " + entry.getValue() + " / " + totalScore);
        }

        // Find all unique configured gestures and actions.
        ArrayList<GestureConfiguration> gestureConfigurations = DatabaseHelper.getInstance(context).getAllGestureConfiguration();
        ArrayList<String> uniqueGestureIds = new ArrayList<>();
        ArrayList<String> uniqueActionIds = new ArrayList<>();
        for (GestureConfiguration gestureConfiguration : gestureConfigurations) {
            if (!uniqueGestureIds.contains(gestureConfiguration.gestureId)) {
                uniqueGestureIds.add(gestureConfiguration.gestureId);
            }

            if (!uniqueActionIds.contains(gestureConfiguration.actionId)) {
                uniqueActionIds.add(gestureConfiguration.actionId);
            }
        }

        // Create map of actions each gesture can trigger.
        // Keys are gesture IDs, values are arrays of action IDs.
        HashMap<String, ArrayList<String>> gestureActionsMap = new HashMap<>();
        for (final String uniqueGestureId : uniqueGestureIds) {
            Funcable<GestureConfiguration> configs = new Funcable(gestureConfigurations).filter(new Predicate<GestureConfiguration>() {
                @Override
                public boolean apply(GestureConfiguration gestureConfiguration) {
                    return gestureConfiguration.gestureId.equals(uniqueGestureId);
                }
            });

            Funcable<String> actionIds = configs.flatMap(new Consumer<GestureConfiguration, Optional<String>>() {
                @Override
                public Optional<String> consume(GestureConfiguration gestureConfiguration) {
                    return new Optional<>(gestureConfiguration.actionId);
                }
            });

            gestureActionsMap.put(uniqueGestureId, actionIds.getValue());
        }

        // Create probabilities for the gesture node.
        HashMap<String, Double> gestureProbabilities = new HashMap<>();
        int uniqueGesturesCount = uniqueGestureIds.size();
        for (int i = 0; i < uniqueGesturesCount; i++) {
            gestureProbabilities.put(uniqueGestureIds.get(i), 1.0 / (double) uniqueGesturesCount);
        }
        this.gestureProbabilities = new Optional<>(gestureProbabilities);

        // Create probabilities for the gesture action node.
        // Outer keys are the gesture IDs. Inner keys are the action IDs
        // and inner values are the probabilities.
        HashMap<String, HashMap<String, Double>> gestureActionProbabilities = new HashMap<>();
        for (String uniqueGestureId : uniqueGestureIds) {
            HashMap<String, Double> actionProbabilitiesForGesture = new HashMap<>();
            ArrayList<String> actionIdsForGesture = gestureActionsMap.get(uniqueGestureId);
            int actionsForGestureCount = actionIdsForGesture.size();
            for (String uniqueActionId : uniqueActionIds) {
                if (actionIdsForGesture.contains(uniqueActionId)) {
                    // The action can be triggered using the gesture.
                    actionProbabilitiesForGesture.put(uniqueActionId, 1.0 / (double)actionsForGestureCount);
                } else {
                    // The action cannot be triggered using the gesture.
                    actionProbabilitiesForGesture.put(uniqueActionId, 0.0);
                }
            }
        }
        this.gestureActionProbabilities = new Optional<>(gestureActionProbabilities);

        // Create evidence.
        HashMap<String, Double> evidence = new HashMap<>();
        Set<String> observedGestures = groupedScores.keySet(); // Gesture IDs of gestures we have recognized.
        for (String uniqueGestureId : uniqueGestureIds) {
            if (observedGestures.contains(uniqueGestureId)) {
                // We have observed, i.e. recognized the gesture with some probability.
                Double gestureScore = averagedScores.get(uniqueGestureId);
                if (averagedScores.size() == 1) {
                    // We only have one score, so it must have a probability of 1.
                    // If we don't do that, we calculate the score, a total score and
                    // calculate the probability as (1 - score / totalScore) resulting
                    // in a probability of 0.
                    evidence.put(uniqueGestureId, 1.0);
                } else {
                    // Subtract from one. The lower the score, the better.
                    evidence.put(uniqueGestureId, 1.0 - (gestureScore / totalScore));
                }
            } else {
                // We have not observed the gesture.
                evidence.put(uniqueGestureId, 0.0);
            }
        }
        this.evidence = new Optional<>(evidence);
    }

    @Override
    public void getContext(ContextualInformationListener listener, BayesNet net) {
        if (!gestureProbabilities.isPresent() || !gestureActionProbabilities.isPresent() || !evidence.isPresent()) {
            this.listener = new Optional<>();
            listener.onFailure();
            return;
        }

        this.listener = new Optional<>(listener);

        // Use sorted keys for gesture probabilities to ensure the same ordering.
        HashMap<String, Double> gestureProbabilities = this.gestureProbabilities.value;
        ArrayList<String> gestureIds = new ArrayList<>(gestureProbabilities.keySet());
        Collections.sort(gestureIds, new Comparator<String>() {
            @Override
            public int compare(String lhs, String rhs) {
                return lhs.compareToIgnoreCase(rhs);
            }
        });

        BayesNode gestureNode = net.createNode("gesture");
        // Add states to gesture node.
        for (String gestureId : gestureIds) {
            gestureNode.addOutcome(gestureId);
        }

        // Add probabilities to gesture node.
        double[] rawGestureProbabilities = new double[gestureProbabilities.values().size()];
        for (int i = 0; i < rawGestureProbabilities.length; i++) {
            rawGestureProbabilities[i] = gestureProbabilities.get(gestureIds.get(i));
        }
        gestureNode.setProbabilities(rawGestureProbabilities);

        // Get all action IDs.
        ArrayList<String> actionIds;
        // Sanity check to ensure we have probabilities for gesture_action node.
        if (gestureActionProbabilities.value.size() > 0) {
            String firstGestureId = gestureActionProbabilities.value.keySet().iterator().next();
            actionIds = new ArrayList<>(gestureActionProbabilities.value.get(firstGestureId).keySet());
        } else {
            actionIds = new ArrayList<>();
        }

        // Always use sorted action IDs to ensure uniformity.
        Collections.sort(actionIds, new Comparator<String>() {
            @Override
            public int compare(String lhs, String rhs) {
                return lhs.compareToIgnoreCase(rhs);
            }
        });

        BayesNode gestureActionNode = net.createNode("gesture_action");
        // Add states to gesture_action node.
        for (String actionId : actionIds) {
            gestureActionNode.addOutcome(actionId);
        }

        // Add gesture node as parent.
        gestureActionNode.setParents(Arrays.asList(gestureNode));

        // Add probabilities to gesture_action node.
        double[] rawGestureActionProbabilities = new double[gestureIds.size() * actionIds.size()];
        for (int g = 0; g < gestureIds.size(); g++) {
            String gestureId = gestureIds.get(g);
            HashMap<String, Double> actionsForGesture = gestureActionProbabilities.value.get(gestureId);
            for (int a = 0; a < actionIds.size(); a++) {
                String actionId = actionIds.get(a);
                rawGestureActionProbabilities[g * actionIds.size() + a] = actionsForGesture.get(actionId);
            }
        }
        gestureActionNode.setProbabilities(rawGestureProbabilities);

        // Add evidence to gesture node.
        double[] softEvidence = new double[gestureIds.size()];
        for (int g = 0; g < gestureIds.size(); g++) {
            softEvidence[g] = evidence.value.get(gestureIds.get(g));
        }
        ProvidedContextualInformation contextualInformation = new ProvidedContextualInformation(
                gestureActionNode,
                gestureNode,
                softEvidence);
        listener.onContextualInformationReady(contextualInformation);
    }

    @Override
    public void cancel() {

    }
}
