package aau.carmakit.ContextEngine;

import android.os.Handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import aau.carmakit.ContextEngine.Recommender.jayes.BayesNet;
import aau.carmakit.ContextEngine.Recommender.jayes.BayesNode;
import aau.carmakit.ContextEngine.Recommender.jayes.inference.SoftEvidenceInferrer;
import aau.carmakit.ContextEngine.Recommender.jayes.inference.jtree.JunctionTreeAlgorithm;
import aau.carmakit.Utilities.Logger;
import aau.carmakit.Utilities.Optional;

public class ContextRecognizer {
    /**
     * Thrown when an operation could not be performed because the
     * recognizer is currently recognizing.
     */
    public class IsRecognizingException extends Exception {
        public IsRecognizingException(String message) {
            super(message);
        }
    }

    /**
     * Thrown when an operation could not be performed because the
     * recognizer is not currently recognizing.
     */
    public class IsNotRecognizingException extends Exception {
        public IsNotRecognizingException(String message) {
            super(message);
        }
    }

    /**
     * Seconds after which context providers should timeout.
     */
    private static final float Timeout = 5;

    /**
     * Map holding all the available context providers.
     * Each context provider is assigned a UUID that anyone
     * can hold onto in order to retrieve the provider again
     * or remove it.
     */
    private HashMap<UUID, ContextualInformationProvider> contextualInformationProviders = new HashMap<>();

    /**
     * Listener set when starting to recognize the context.
     * The listener is called when the context has been desired
     * and the suggested events has been inferred.
     */
    private ContextRecognizerListener listener;

    /**
     * Context providers pending delivery of their context.
     * These providers may be cancelled, timeout or deliver a context.
     */
    private HashMap<UUID, ContextualInformationProvider> pendingContextualInformationProviders = new HashMap<>();

    /**
     * Contextual information provided by the contextual information providers.
     */
    private ArrayList<ProvidedContextualInformation> providedContextualInformations = new ArrayList<>();

    /**
     * Timer started when the gesture recognizer starts recognizing.
     * Timeouts recognition when fired.
     */
    private Handler timeoutHandler;

    /**
     * Whether or not the recognizer is currently recognizing.
     */
    private boolean recognizing = false;

    /**
     * Reference to the Bayesian network.
     * Currently the network is recreated every time the recognizer is started.
     * This is a minor implementation detail. The network should only be created
     * when actually needed, i.e. when gesture configurations change.
     */
    private Optional<BayesNet> net = new Optional<>();

    /**
     * Add a provider to the engine thus including its context in the recognition.
     * @param contextProvider Provider to add.
     * @return UUID Identifying the provider that was just added.
     */
    public UUID addProvider(ContextualInformationProvider contextProvider) throws IsRecognizingException {
        if (recognizing) {
            throw new IsRecognizingException("Context providers cannot be added while the recognizer is recognizing.");
        }

        UUID uuid = UUID.randomUUID();
        contextualInformationProviders.put(uuid, contextProvider);
        return uuid;
    }

    /**
     * Removes a provider with the specified UUID.
     * @param uuid UUID of provider to remove.
     */
    public void removeProvider(UUID uuid) throws IsRecognizingException {
        if (recognizing) {
            throw new IsRecognizingException("Context providers cannot be removed while the recognizer is recognizing.");
        }

        contextualInformationProviders.remove(uuid);
    }

    /**
     * Retrieves the provider with the specified UUID.
     * @param uuid UUID of provider to retrieve.
     * @return Registered provider with the specified UUID. null, if the provider is not registered.
     */
    public ContextualInformationProvider getProvider(UUID uuid) {
        return contextualInformationProviders.get(uuid);
    }

    /**
     * Starts recognizing the context using the registered context providers.
     * @param listener Listener called when the context has been recognized.
     */
    public void start(ContextRecognizerListener listener) throws IsRecognizingException {
        if (recognizing) {
            throw new IsRecognizingException("The recognition cannot be started because it is already started.");
        }

        recognizing = true;
        this.listener = listener;
        providedContextualInformations.clear();

        Logger.verbose("Context recognizer will start");

        if (contextualInformationProviders.size() > 0) {
            // We have context providers registered. Start recognizing.
            startTimeoutTimer();
            startRegisteredContextualInformationProviders();
        } else {
            // We do not have any context providers, so just tell the
            // listener that there are zero outcomes.
            if (listener != null) {
                listener.onContextReady(new ArrayList<ContextOutcome>());
            }
        }

        Logger.verbose("Context recognizer did start");
    }

    /**
     * Starts all registered context providers.
     */
    private void startRegisteredContextualInformationProviders() {
        BayesNet net = new BayesNet();
        this.net = new Optional<>(net);

        // Make sure we register all providers as added.
        for (final Map.Entry<UUID, ContextualInformationProvider> entry : contextualInformationProviders.entrySet()) {
            pendingContextualInformationProviders.put(entry.getKey(), null);
        }

        for (final Map.Entry<UUID, ContextualInformationProvider> entry : contextualInformationProviders.entrySet()) {
            Logger.verbose("Start context provider " + entry.getKey());

            final UUID uuid = entry.getKey();
            final ContextualInformationProvider contextualInformationProvider= entry.getValue();

            pendingContextualInformationProviders.put(entry.getKey(), contextualInformationProvider);

            ContextualInformationListener listener = new ContextualInformationListener() {
                @Override
                public void onContextualInformationReady(ProvidedContextualInformation providedContextualInformation) {
                    // Sanity check to ensure we do not have null outcomes.
                    if (providedContextualInformation != null) {
                        providedContextualInformations.add(providedContextualInformation);
                    }

                    pendingContextualInformationProviders.remove(uuid);
                    checkIfRecognitionCompleted();
                }

                @Override
                public void onFailure() {
                    pendingContextualInformationProviders.remove(uuid);
                    checkIfRecognitionCompleted();
                }
            };

            entry.getValue().getContext(listener, net);
        }
    }

    /**
     * Starts timer triggered when the recognition times out.
     */
    private void startTimeoutTimer() {
        Runnable timeoutRunnable = new Runnable() {
            @Override
            public void run() {
                onTimeout();
            }
        };

        timeoutHandler = new Handler();
        timeoutHandler.postDelayed(timeoutRunnable, (long)(ContextRecognizer.Timeout * 1000));
    }

    /**
     * Cancels the timeout timer.
     */
    private void cancelTimeoutTimer() {
        if (timeoutHandler != null) {
            timeoutHandler.removeCallbacksAndMessages(null);
        }
    }

    /**
     * Cancels recognizing the context.
     */
    public void cancel() throws IsNotRecognizingException {
        if (!recognizing) {
            throw new IsNotRecognizingException("Recognition could not be cancelled because the recognizer is not started.");
        }

        listener = null;
        cancelPendingContextualInformationProviders();
        recognizing = false;
    }

    /**
     * Cancels all pending context providers.
     */
    private void cancelPendingContextualInformationProviders() {
        for (ContextualInformationProvider contextualInformationProvider: pendingContextualInformationProviders.values()) {
            contextualInformationProvider.cancel();
        }

        pendingContextualInformationProviders.clear();
    }

    /**
     * Called when recognition times out.
     */
    private void onTimeout() {
        try {
            cancel();
        } catch (IsNotRecognizingException e) { }

        if (listener != null) {
            listener.onContextRecognitionTimeout();
        }
    }

    /**
     * Checks whether or not the recognizer is currently recognizing.
     * @return Whether or not the recognizer is currently recognizing.
     */
    public boolean isRecognizing() {
        return recognizing;
    }

    /**
     * Checks if recognition has completed by checking if we
     * have more pending context providers.
     */
    private void checkIfRecognitionCompleted() {
        Logger.verbose("Remaining pending context providers: " + pendingContextualInformationProviders.size());
        if (pendingContextualInformationProviders.isEmpty()) {
            onRecognitionCompleted();
        }
    }

    /**
     * Called when recognition completes.
     */
    private void onRecognitionCompleted() {
        recognizing = false;
        cancelTimeoutTimer();

        // No information was provided.
        if (providedContextualInformations.size() == 0) {
            listener.onFailedRecognizingContext();
            return;
        }

        if (net.isPresent()) {
            // Create hash sets of states for all nodes in order to check if all nodes have the same states.
            ArrayList<HashSet<String>> nodeStatesHashSets = new ArrayList<>();
            for (ProvidedContextualInformation providedContextualInformation : providedContextualInformations) {
                ArrayList<String> states = new ArrayList<>(providedContextualInformation.actionParentNode.getOutcomes());
                nodeStatesHashSets.add(new HashSet(states));
            }

            // Check if all nodes have the same states.
            for (int i = 0; i < nodeStatesHashSets.size(); i++) {
                for (int n = 0; n < nodeStatesHashSets.size(); n++) {
                    if (!nodeStatesHashSets.get(i).equals(nodeStatesHashSets.get(n))) {
                        Logger.error("Action nodes does not have same states!");
                        listener.onFailedRecognizingContext();
                        return;
                    }
                }
            }

            // All nodes have the same states. Take the states from one of the nodes.
            List<String> states = providedContextualInformations.get(0).actionParentNode.getOutcomes();

            BayesNode actionNode = net.value.createNode("action");
            for (String state : states) {
                actionNode.addOutcomes(state);
            }

            // Add parents to the action node.
            ArrayList<BayesNode> actionParents = new ArrayList<>();
            for (ProvidedContextualInformation providedContextualInformation : providedContextualInformations) {
                actionParents.add(providedContextualInformation.actionParentNode);
            }
            actionNode.setParents(actionParents);

            // Create the probabilities.
            int nodeCount = providedContextualInformations.size();
            CrossedStatesCalculator crossedStatesCalculator = new CrossedStatesCalculator(new ArrayList<>(states), nodeCount);
            actionNode.setProbabilities(crossedStatesCalculator.calculateProbabilities());

            SoftEvidenceInferrer inference = new SoftEvidenceInferrer(new JunctionTreeAlgorithm());
            inference.setNetwork(net.value);
            for (ProvidedContextualInformation providedContextualInformation : providedContextualInformations) {
                inference.addSoftEvidence(providedContextualInformation.evidenceNode, providedContextualInformation.softEvidence);
            }

            ArrayList<ContextOutcome> contextOutcomes = new ArrayList<>();
            BayesNode inferredActionNode = net.value.getNode("action");
            double[] actionBeliefs = inference.getBeliefs(inferredActionNode);
            for (int i = 0; i < actionBeliefs.length; i++) {
                contextOutcomes.add(new ContextOutcome(inferredActionNode.getOutcomeName(i), actionBeliefs[i]));
            }

            listener.onContextReady(contextOutcomes);

//            double[] gestureBeliefs = inference.getBeliefs(net.value.getNode("gesture"));
//            Logger.verbose("Gesture beliefs:");
//            for (int i = 0; i < gestureBeliefs.length; i++) {
//                Logger.verbose(" - " + net.value.getNode("gesture").getOutcomeName(i) + ": " + gestureBeliefs[i] * 100);
//            }
//
//            double[] roomBeliefs = inference.getBeliefs(net.value.getNode("room"));
//            Logger.verbose("Room beliefs:");
//            for (int i = 0; i < roomBeliefs.length; i++) {
//                Logger.verbose(" - " + net.value.getNode("room").getOutcomeName(i) + ": " + roomBeliefs[i] * 100);
//            }
//
//            double[] gestureActionBeliefs = inference.getBeliefs(net.value.getNode("gesture_action"));
//            Logger.verbose("Gesture action beliefs:");
//            for (int i = 0; i < gestureActionBeliefs.length; i++) {
//                Logger.verbose(" - " + net.value.getNode("gesture_action").getOutcomeName(i) + ": " + gestureActionBeliefs[i] * 100);
//            }
//
//            double[] roomActionBeliefs = inference.getBeliefs(net.value.getNode("room_action"));
//            Logger.verbose("Room action beliefs:");
//            for (int i = 0; i < roomActionBeliefs.length; i++) {
//                Logger.verbose(" - " + net.value.getNode("room_action").getOutcomeName(i) + ": " + roomActionBeliefs[i] * 100);
//            }
//
//            double[] actionBeliefs = inference.getBeliefs(net.value.getNode("action"));
//            Logger.verbose("Action beliefs:");
//            for (int i = 0; i < actionBeliefs.length; i++) {
//                Logger.verbose(" - " + net.value.getNode("action").getOutcomeName(i) + ": " + actionBeliefs[i] * 100);
//            }
        }
    }
}
