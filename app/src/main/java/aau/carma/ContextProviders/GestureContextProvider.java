package aau.carma.ContextProviders;

import aau.carma.ContextEngine.ContextOutcome;
import aau.carma.ContextEngine.ContextProvider;
import aau.carma.ContextEngine.ContextProviderListener;

/**
 * Provides the gesture context.
 */
public class GestureContextProvider implements ContextProvider {
    @Override
    public double getWeight() {
        return 0.6;
    }

    @Override
    public void getContext(ContextProviderListener listener) {
        listener.onContextReady(new ContextOutcome[] {
                new ContextOutcome(1, 0.1),
                new ContextOutcome(2, 0.6),
                new ContextOutcome(3, 0.2),
                new ContextOutcome(4, 0.1),
        });
    }

    @Override
    public void cancel() {

    }
}
