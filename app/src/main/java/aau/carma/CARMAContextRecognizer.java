package aau.carma;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.UUID;

import aau.carma.ContextEngine.ContextRecognizer;
import aau.carma.ContextProviders.PositionContextProvider;


/**
 * A gesture recognizer that can easily be configured
 * with the necessary context providers.
 */
public class CARMAContextRecognizer extends ContextRecognizer {
    /**
     * The private instance of the recognizer.
     */
    private static CARMAContextRecognizer ourInstance = new CARMAContextRecognizer();

    /**
     * The shared instance of the context recognizer.
     * @return Shared instance.
     */
    public static CARMAContextRecognizer getInstance() {
        return ourInstance;
    }

    /**
     * UUID for the position context provider.
     */
    private UUID positionContextProviderUUID;

    /**
     * Initializes a context recognizer that can easily be configured
     * with the necessary context providers.
     */
    private CARMAContextRecognizer() { }

    /**
     * Adds and configures the position context provider.
     * @param context Context to monitor the rooms in. Typically the application context.
     * @param rooms Rooms to monitor.
     */
    public void addPositionContextProvider(Context context, ArrayList<Room> rooms) throws IsRecognizingException {
        if (positionContextProviderUUID != null) {
            removeProvider(positionContextProviderUUID);
            positionContextProviderUUID = null;
        }

        PositionContextProvider contextProvider = new PositionContextProvider();
        contextProvider.configure(context, rooms);
        positionContextProviderUUID = addProvider(contextProvider);

        Log.v(Configuration.Log, "Did configure position context provider");
    }

    /**
     * Gets the registered position context provider.
     * @return Registered position context provider.
     */
    public PositionContextProvider getPositionContextProvider() {
        return (PositionContextProvider)getProvider(positionContextProviderUUID);
    }
}
