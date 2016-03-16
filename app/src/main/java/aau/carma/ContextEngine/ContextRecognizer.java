package aau.carma.ContextEngine;

import android.os.Handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
        private double weight;

        /**
         * Outcomes of the context.
         */
        private ContextOutcome[] outcomes;

        /**
         * Creates a representation of a context provided by one of the context providers.
         * @param weight Weight of the context.
         * @param outcomes Outcomes of the context, each having a probability.
         */
        ProvidedContext(double weight, ContextOutcome[] outcomes) {
            this.weight = weight;
            this.outcomes = outcomes;
        }

        /**
         * Weight of the context.
         * @return Weight.
         */
        public double getWeight() {
            return weight;
        }

        /**
         * Outcomes of the context.
         * @return Outcomes.
         */
        public ContextOutcome[] getOutcomes() {
            return outcomes;
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
    private ArrayList<ProvidedContext> providedContexts = new ArrayList<ProvidedContext>();

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
    public void start(ContextRecognizerListener  listener) throws IsRecognizingException {
        if (recognizing) {
            throw new IsRecognizingException("The recognition cannot be started because it is already started.");
        }

        recognizing = true;
        this.listener = listener;
        providedContexts.clear();
        startRegisteredContextProviders();
        startTimeoutTimer();
    }

    /**
     * Starts all registered context providers.
     */
    private void startRegisteredContextProviders() {
        for (Map.Entry<UUID, ContextProvider> entry : contextProviders.entrySet()) {
            final UUID uuid = entry.getKey();
            final ContextProvider contextProvider = entry.getValue();

            entry.getValue().getContext(new ContextProviderListener() {
                @Override
                public void onContextReady(ContextOutcome[] outcomes) {
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

            pendingContextProviders.put(uuid, contextProvider);
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
        if (pendingContextProviders.isEmpty()) {
            onRecognitionCompleted();
        }
    }

    /**
     * Called when recognition completes.
     */
    private void onRecognitionCompleted() {



        if (listener != null) {
            listener.onContextReady(new ContextOutcome[] {

            });
        }
    }
}