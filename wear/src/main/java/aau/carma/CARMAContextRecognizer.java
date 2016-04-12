package aau.carma;

import android.content.Context;

import java.util.ArrayList;
import java.util.UUID;

import aau.carma.ContextProviders.GestureContextProvider;
import aau.carma.ContextProviders.PositionContextProvider;
import aau.carmakit.ContextEngine.ContextRecognizer;
import aau.carmakit.Utilities.Logger;
import aau.carmakit.Utilities.Room;


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

        PositionContextProvider contextProvider = new PositionContextProvider(context);
        contextProvider.configure(context, rooms);
        positionContextProviderUUID = addProvider(contextProvider);

        Logger.verbose("Did configure position context provider");
    }

    /**
     * UUID for the position context provider.
     */
    private UUID gestureContextProviderUUID;

    /**
     * Adds and configures the gesture context provider.
     * @param context Context to recognize gestures in. Typically the application context.
     */
    public void addGestureContextProvider(Context context) throws ContextRecognizer.IsRecognizingException {
        if (gestureContextProviderUUID != null) {
            removeProvider(gestureContextProviderUUID);
            gestureContextProviderUUID = null;
        }

        GestureContextProvider contextProvider = new GestureContextProvider(context);
        gestureContextProviderUUID = addProvider(contextProvider);

        Logger.verbose("Did configure gesture context provider");
    }

    /**
     * Gets the registered position context provider.
     * @return Registered position context provider.
     */
    public PositionContextProvider getPositionContextProvider() {
        return (PositionContextProvider)getProvider(positionContextProviderUUID);
    }

    /**
     * Gets the registered gesture context provider
     * @return Registered gesture context provider
     */
    public GestureContextProvider getGestureContextProvider() {
        return (GestureContextProvider)getProvider(gestureContextProviderUUID);
    }
}
