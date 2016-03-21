package aau.carma.ContextProviders;

import java.util.ArrayList;

import aau.carma.ContextEngine.ContextOutcome;
import aau.carma.ContextEngine.ContextProvider;
import aau.carma.ContextEngine.ContextProviderListener;
import aau.carma.RoomsManager;

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
        listener.onContextReady(new ArrayList<ContextOutcome>());
    }

    @Override
    public void cancel() { }
}
