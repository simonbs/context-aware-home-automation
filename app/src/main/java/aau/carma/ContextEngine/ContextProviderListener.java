package aau.carma.ContextEngine;

import android.content.Entity;

/**
 * Objects interested in receiving the context conform to this interface.
 */
public interface ContextProviderListener {
    /**
     * Called by a context provider whenever a context is ready.
     * @param entities Entities with assigned probabilities.
     */
    void onContextReady(ContextOutcome[] outcomes);

    /**
     * Called by a context provider whenever retrieval of the
     * context failed.
     */
    void onFailure();
}
