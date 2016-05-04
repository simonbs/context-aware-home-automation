package aau.carma.ContextEngine;

import java.util.ArrayList;

import aau.carmakit.ContextEngine.ContextOutcome;

/**
 * Object interested in being notified when contextual information is ready.
 */
public interface ContextualInformationListener {
    /**
     * Called by a context provider whenever a context is ready.
     * @param entities Entities with assigned probabilities.
     */
    void onContextualInformationReady(ProvidedContextualInformation information);

    /**
     * Called by a context provider whenever retrieval of the
     * context failed.
     */
    void onFailure();
}
