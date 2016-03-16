package aau.carma.ContextEngine;

/**
 * Implemented by objects interested in receiving events when a context recognizer completes.
 */
public interface ContextRecognizerListener {
    /**
     * Called by the context recognizer when it has recognized the context.
     * The recognizer provides a set of possible outcomes each with probabilities.
     * @param entities Entities with assigned probabilities.
     */
    void onContextReady(ContextOutcome[] outcomes);

    /**
     * Called by the context recognizer when recognizing the context fails.
     */
    void onFailedRecognizingContext();

    /**
     * Called by the context recognizer when recognizing the context times out.
     */
    void onContextRecognitionTimeout();
}
