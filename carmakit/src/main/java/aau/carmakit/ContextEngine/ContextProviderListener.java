package aau.carmakit.ContextEngine;

import android.content.Context;
import android.content.Entity;

import java.util.ArrayList;

/**
 * Objects interested in receiving the context conform to this interface.
 */
public interface ContextProviderListener {
    /**
     * Called by a context provider whenever a context is ready.
     * @param entities Entities with assigned probabilities.
     */
    void onContextReady(ArrayList<ContextOutcome> outcomes);

    /**
     * Called by a context provider whenever retrieval of the
     * context failed.
     */
    void onFailure();
}
