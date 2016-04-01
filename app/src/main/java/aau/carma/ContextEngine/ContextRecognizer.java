package aau.carma.ContextEngine;

import android.content.Context;
import android.os.Handler;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import aau.carma.Library.Logger;

/**
 * Recognizes a context given one or more providers that
 * each contribute to the total knowledge about the context.
 */
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
     * Represents a context provided by one of the context providers.
     */
    private class ProvidedContext {
        /**
         * Weight of the context.
         */
        public final double weight;

        /**
         * Outcomes of the context.
         */
        public final ArrayList<ContextOutcome> outcomes;

        /**
         * Creates a representation of a context provided by one of the context providers.
         * @param weight Weight of the context.
         * @param outcomes Outcomes of the context, each having a probability.
         */
        ProvidedContext(double weight, ArrayList<ContextOutcome> outcomes) {
            this.weight = weight;
            this.outcomes = outcomes;
        }

        /**
         * Calculates the weighted outcomes based on the weight provided
         * by the weight property.
         * @return Weighted outcomes.
         */
        public ArrayList<ContextOutcome> calculateWeightedOutcomes() {
            ArrayList<ContextOutcome> result = new ArrayList<>();
            for (ContextOutcome outcome : outcomes) {
                Logger.verbose("Calculate weighted outcome: " + outcome.probability + " * " + weight);
                result.add(new ContextOutcome(outcome.id, outcome.probability * weight));
            }

            return result;
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
    private HashMap<UUID, ContextProvider> contextProviders = new HashMap<>();

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
    private HashMap<UUID, ContextProvider> pendingContextProviders = new HashMap<>();

    /**
     * Contexts provided by the context providers.
     */
    private ArrayList<ProvidedContext> providedContexts = new ArrayList<>();

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
     * Add a provider to the engine thus including its context in the recognition.
     * @param contextProvider Provider to add.
     * @return UUID Identifying the provider that was just added.
     */
    public UUID addProvider(ContextProvider contextProvider) throws IsRecognizingException {
        if (recognizing) {
            throw new IsRecognizingException("Context providers cannot be added while the recognizer is recognizing.");
        }

        UUID uuid = UUID.randomUUID();
        contextProviders.put(uuid, contextProvider);
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

        contextProviders.remove(uuid);
    }

    /**
     * Retrieves the provider with the specified UUID.
     * @param uuid UUID of provider to retrieve.
     * @return Registered provider with the specified UUID. null, if the provider is not registered.
     */
    public ContextProvider getProvider(UUID uuid) {
        return contextProviders.get(uuid);
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
        providedContexts.clear();

        Logger.verbose("Will start");

        if (contextProviders.size() > 0) {
            // We have context providers registered. Start recognizing.
            startTimeoutTimer();
            startRegisteredContextProviders();
        } else {
            // We do not have any context providers, so just tell the
            // listener that there are zero outcomes.
            if (listener != null) {
                listener.onContextReady(new ArrayList<ContextOutcome>());
            }
        }

        Logger.verbose("Did start");
    }

    /**
     * Starts all registered context providers.
     */
    private void startRegisteredContextProviders() {
        // Make sure we register all providers as added.
        for (final Map.Entry<UUID, ContextProvider> entry : contextProviders.entrySet()) {
            pendingContextProviders.put(entry.getKey(), null);
        }

        for (final Map.Entry<UUID, ContextProvider> entry : contextProviders.entrySet()) {
            Logger.verbose("Start context provider " + entry.getKey());

            final UUID uuid = entry.getKey();
            final ContextProvider contextProvider = entry.getValue();

            pendingContextProviders.put(entry.getKey(), contextProvider);

            entry.getValue().getContext(new ContextProviderListener() {
                @Override
                public void onContextReady(ArrayList<ContextOutcome> outcomes) {
                    for (ContextOutcome outcome : outcomes) {
                        Logger.verbose("Context ready with outcome " + outcome.id + ": " + outcome.probability);
                    }

                    Logger.verbose("Context ready for provider " + entry.getKey());
                    ProvidedContext providedContext = new ProvidedContext(contextProvider.getWeight(), outcomes);
                    providedContexts.add(providedContext);
                    pendingContextProviders.remove(uuid);
                    checkIfRecognitionCompleted();
                }

                @Override
                public void onFailure() {
                    pendingContextProviders.remove(uuid);
                    checkIfRecognitionCompleted();
                }
            });
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
        cancelPendingContextProviders();
        recognizing = false;
    }

    /**
     * Cancels all pending context providers.
     */
    private void cancelPendingContextProviders() {
        for (ContextProvider contextProvider : pendingContextProviders.values()) {
            contextProvider.cancel();
        }

        pendingContextProviders.clear();
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
        Logger.verbose("Remaining pending context providers: " + pendingContextProviders.size());
        if (pendingContextProviders.isEmpty()) {
            onRecognitionCompleted();
        }
    }

    /**
     * Called when recognition completes.
     */
    private void onRecognitionCompleted() {
        recognizing = false;

        cancelTimeoutTimer();

        ArrayList<ContextOutcome> flattenedOutcomes = flattenedOutcomes(providedContexts);

        if (flattenedOutcomes != null) {
            Logger.verbose("- - - - - - - - - - - - - - - -");
            Logger.verbose("Flattened outcomes:");
            for (ContextOutcome flattenedOutcome : flattenedOutcomes) {
                Logger.verbose("Probability: " + flattenedOutcome .id + ": " + flattenedOutcome .probability);
            }
            Logger.verbose("- - - - - - - - - - - - - - - -");
        }

        ArrayList<ContextOutcome> summedOutcomes = ContextOutcome.sumOutcomes(flattenedOutcomes);

        if (summedOutcomes != null) {
            Logger.verbose("- - - - - - - - - - - - - - - -");
            Logger.verbose("Summed outcomes:");
            for (ContextOutcome summedOutcome : summedOutcomes) {
                Logger.verbose("Probability: " + summedOutcome.id + ": " + summedOutcome.probability);
            }
            Logger.verbose("- - - - - - - - - - - - - - - -");
        }

        for (ContextOutcome summedOutcome : summedOutcomes) {
            Logger.verbose(summedOutcome.id + ": " + summedOutcome.probability);
        }

        ArrayList<ContextOutcome> normalizedOutcomes = ContextOutcome.normalizeOutcomes(summedOutcomes);

        if (listener != null) {
            listener.onContextReady(normalizedOutcomes);
        }
    }

    /**
     * Flattens the array of outcomes in all provided contexts available.
     * @param providedContexts Provided contexts to take weighted outcomes from.
     * @return All outcomes.
     */
    private ArrayList<ContextOutcome> flattenedOutcomes(ArrayList<ProvidedContext> providedContexts) {
        ArrayList<ContextOutcome> result = new ArrayList<>();
        for (ProvidedContext providedContext : providedContexts) {
            result.addAll(providedContext.calculateWeightedOutcomes());
        }

        return result;
    }
}