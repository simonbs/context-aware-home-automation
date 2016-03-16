package aau.carma.ContextProviders;

import aau.carma.ContextEngine.ContextOutcome;
import aau.carma.ContextEngine.ContextProvider;
import aau.carma.ContextEngine.ContextProviderListener;

/**
 * Provides the position context.
 */
public class PositionContextProvider implements ContextProvider {
    @Override
    public double getWeight() {
        return 0.4;
    }

    @Override
    public void getContext(ContextProviderListener listener) {
        listener.onContextReady(new ContextOutcome[] {
                new ContextOutcome(1, 0.4),
                new ContextOutcome(2, 0.4),
                new ContextOutcome(3, 0.2),
                new ContextOutcome(4, 0.2),
        });
    }

    @Override
    public void cancel() {

    }
}
