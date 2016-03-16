package aau.carma.ContextEngine;

/**
 * Objects that can provide knowledge about the context
 * should conform to this protocol. The recognizer will
 * ask each provider for their knowledge about the context
 * when the recognizer is started.
 */
public interface ContextProvider {
    /**
     * The weight is used when calculating probabilities.
     * @return Weight of the context returned from the provider.
     */
    double getWeight();

    /**
     * Called when the engine retrieves the context.
     * The provider should ideally be ready to return a context
     * at any time.
     * @param listener The context engines listener that must be called whenever the context has been retrieved and is ready.
     */
    void getContext(ContextProviderListener listener);

    /**
     * Cancel retrieving the context. Called by the context recognize
     * when it is no longer interested in the context, e.g. because
     * the provider was too slow to provide the context.
     */
    void cancel();
}
