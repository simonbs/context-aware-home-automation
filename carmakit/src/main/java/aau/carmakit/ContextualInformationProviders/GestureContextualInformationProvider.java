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
        Double gestureScoreThreshold = 70.0;

        Logger.verbose("[GestureContextualInformationProvider] Will update probabilities. Accepts gestures with a score of " + gestureScoreThreshold + " or higher.");

        // For debugging purposes.
        for (ThreeDMatch match : matches) {
            Logger.verbose("[GestureContextualInformationProvider] Raw gesture match: " + match.getLabel() + " with score " + match.getScore());
        }

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

        HashMap<String, Double> translatedScores = new HashMap<>();
        // Sort the scores descending in preparation of calculating translated scores.
        ArrayList<Map.Entry<String, Double>> averagedScoreEntries = new ArrayList<>(averagedScores.entrySet());
        Collections.sort(averagedScoreEntries, new Comparator<Map.Entry<String, Double>>() {
            @Override
            public int compare(Map.Entry<String, Double> lhs, Map.Entry<String, Double> rhs) {
                if (rhs.getValue() > lhs.getValue()) {
                    return 1;
                } else if (lhs.getValue() < rhs.getValue()) {
                    return -1;
                }

                return 0;
            }
        });

        // Convert scores to lowest score being the best, i.e. we must convert the lowest score to the highest
        // score in order to correctly compute the probability.
        // While computing the new scores, we must preserve the ratio between scores.
        // We also compute the total score along the way.
        Double totalScore = 0.0;
        for (int i = 0; i < averagedScoreEntries.size(); i++) {
            Map.Entry<String, Double> averagedScoreEntry = averagedScoreEntries.get(i);
            if (averagedScoreEntries.size() == 1) {
                // There's only one entry. We do not need to perform any translation.
                translatedScores.put(averagedScoreEntry.getKey(), averagedScoreEntry.getValue());
                totalScore += averagedScoreEntry.getValue();
            } else if (i == 0) {
                // We have more entries and this is the first entry, i.e. the highest. Just add it.
                translatedScores.put(averagedScoreEntry.getKey(), averagedScoreEntry.getValue());
                totalScore += averagedScoreEntry.getValue();
            } else {
                // We have more entries and this is not the first entry.
                Map.Entry<String, Double> previousEntry = averagedScoreEntries.get(i - 1);
                Double ratio = previousEntry.getValue() / averagedScoreEntry.getValue();
                Double newScore = translatedScores.get(previousEntry.getKey()) * ratio;
                translatedScores.put(averagedScoreEntry.getKey(), newScore);
                totalScore += newScore;
            }
        }

        // Log averaged scores and translated.
        for (Map.Entry<String, Double> entry : averagedScores.entrySet()) {
            Logger.verbose("[GestureContextualInformationProvider] Gesture " + entry.getKey()
                    + " has an average score "
                    + entry.getValue() + " of a total " + totalScore
                    + ", resulting in a translated score of "
                    + translatedScores.get(entry.getKey()) + ".");
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

            gestureActionProbabilities.put(uniqueGestureId, actionProbabilitiesForGesture);
        }
        this.gestureActionProbabilities = new Optional<>(gestureActionProbabilities);

        // Create evidence.
        HashMap<String, Double> evidence = new HashMap<>();
        for (String uniqueGestureId : uniqueGestureIds) {
            if (translatedScores.keySet().size() == 0) {
                // If the averaged scores are empty, we assign an equal evidence to all gestures.
                Logger.verbose("[GestureContextualInformationProvider] Gesture " + uniqueGestureId +
                        " has evidence 1.0 / " + (double)uniqueGesturesCount + " = "
                        + (1.0 / (double)uniqueGesturesCount)
                        + ", same as all others because the the set of observed gestures is empty.");
                evidence.put(uniqueGestureId, 1.0 / (double)uniqueGesturesCount);
            } else if (translatedScores.keySet().contains(uniqueGestureId)) {
                // We have observed, i.e. recognized the gesture with some probability.
                if (translatedScores.size() == 1) {
                    // We only have one score, so it must have a probability of 1.
                    Logger.verbose("[GestureContextualInformationProvider] Gesture " + uniqueGestureId
                            + " has evidence 1.0 because it was the only observed gesture in the set of recognized gestures.");
                    evidence.put(uniqueGestureId, 1.0);
                } else {
                    // Subtract from one. The lower the score, the better.
                    Double gestureScore = translatedScores.get(uniqueGestureId);
                    Logger.verbose("[GestureContextualInformationProvider] Gesture " + uniqueGestureId
                            + " has evidence " + (gestureScore / totalScore));
                    evidence.put(uniqueGestureId, gestureScore / totalScore);
                }
            } else {
                // We have not observed the gesture.
                Logger.verbose("[GestureContextualInformationProvider] Gesture " + uniqueGestureId
                        + " has evidence 0, because it was not observed in the set of recognized gestures.");
                evidence.put(uniqueGestureId, 0.0);
            }
        }
        this.evidence = new Optional<>(evidence);

        Logger.verbose("[GestureContextualInformationProvider] Did update probabilities.");
    }

    @Override
    public void getContext(ContextualInformationListener listener, BayesNet net) {
        if (!gestureProbabilities.isPresent() || !gestureActionProbabilities.isPresent() || !evidence.isPresent()) {
            this.listener = new Optional<>();
            Logger.error("[GestureContextualInformationProvider] Failed getting context. One or more values are not present.");
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
            Logger.verbose("[GestureContextualInformationProvider] Add state " + gestureId + " to gesture node.");
            gestureNode.addOutcome(gestureId);
        }

        // Add probabilities to gesture node.
        double[] rawGestureProbabilities = new double[gestureProbabilities.values().size()];
        for (int i = 0; i < rawGestureProbabilities.length; i++) {
            Logger.verbose("[GestureContextualInformationProvider] Add P(gesture=" + gestureIds.get(i) + ") = "
                    + gestureProbabilities.get(gestureIds.get(i)));
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
            Logger.verbose("[GestureContextualInformationProvider] Add state " + actionId + " to gesture_action node.");
            gestureActionNode.addOutcome(actionId);
        }

        // Add gesture node as parent.
        gestureActionNode.setParents(Arrays.asList(gestureNode));

        // Add probabilities to gesture_action node.
        double[] rawGestureActionProbabilities = new double[gestureIds.size() * actionIds.size()];
        for (int g = 0; g < gestureIds.size(); g++) {
            String gestureId = gestureIds.get(g);
            HashMap<String, Double> actionProbabilitiesForGesture = gestureActionProbabilities.value.get(gestureId);
            for (int a = 0; a < actionIds.size(); a++) {
                String actionId = actionIds.get(a);
                Logger.verbose("[GestureContextualInformationProvider] Add P(gesture_action=" + actionId + "|gesture=" + gestureId + ") = " + actionProbabilitiesForGesture.get(actionId));
                rawGestureActionProbabilities[g * actionIds.size() + a] = actionProbabilitiesForGesture.get(actionId);
            }
        }

        gestureActionNode.setProbabilities(rawGestureActionProbabilities);

        // Add evidence to gesture node.
        double[] softEvidence = new double[gestureIds.size()];
        for (int g = 0; g < gestureIds.size(); g++) {
            Logger.verbose("[GestureContextualInformationProvider] Propose evidence " + evidence.value.get(gestureIds.get(g))
                    + " to state " + gestureIds.get(g) + " of gesture node.");
            softEvidence[g] = evidence.value.get(gestureIds.get(g));
        }

        Logger.verbose("[GestureContextualInformationProvider] Did get context.");

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
