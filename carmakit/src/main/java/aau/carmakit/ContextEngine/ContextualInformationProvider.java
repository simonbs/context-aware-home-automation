package aau.carmakit.ContextEngine;

import aau.carmakit.ContextEngine.Recommender.jayes.BayesNet;

public interface ContextualInformationProvider {
    /**
     * Called when the engine retrieves the context.
     * The provider should ideally be ready to return a context
     * at any time.
     * @param listener The context engines listener that must be called whenever the context has been retrieved and is ready.
     * @param net Network to create nodes in.
     */
    void getContext(ContextualInformationListener listener, BayesNet net);

    /**
     * Cancel retrieving the context. Called by the context recognize
     * when it is no longer interested in the context, e.g. because
     * the provider was too slow to provide the context.
     */
    void cancel();
}
