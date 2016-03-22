package aau.carma.ContextProviders;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import aau.carma.Configuration;
import aau.carma.ContextEngine.ContextOutcome;
import aau.carma.ContextEngine.ContextProvider;
import aau.carma.ContextEngine.ContextProviderListener;
import aau.carma.DummyData;
import aau.carma.GestureConfiguration;
import aau.carma.Library.Room;

/**
 * Provides the position context.
 */
public class PositionContextProvider implements ContextProvider {
    /**
     * Represents an observation of the user entering a room at a certain time.
     */
    class EnteredRoomObservation {
        /**
         * Timestamp at which the user entered the room.
         */
        final long timestamp;

        /**
         * The room the user entered.
         */
        final Room room;

        /**
         * Initializes an observation.
         * @param timestamp Timestamp at which the user entered the room.
         * @param room The room the user entered.
         */
        EnteredRoomObservation(long timestamp, Room room) {
            this.timestamp = timestamp;
            this.room = room;
        }
    }

    /**
     * Listens for changes to the users position.
     */
    private PositionManager positionManager;

    /**
     * All room observations registered within the previous amount of seconds
     * specified by the enteredRoomObservartionsTimeToLive property.
     */
    private ArrayList<EnteredRoomObservation> enteredRoomObservations = new ArrayList();

    /**
     * Number of seconds to consider each entered room observation.
     */
    private long enteredRoomObservationsTimeToLive = 30;

    /**
     * Current set of outcomes.
     */
    private ArrayList<ContextOutcome> outcomes;

    /**
     * Initializes the position context provider.
     */
    public PositionContextProvider() { }

    /**
     * Configures the provider to listen for the users position in the given rooms.
     * @param context Context to monitor the rooms in.
     * @param rooms Rooms to listen for the users position in.
     */
    public void configure(Context context, ArrayList<Room> rooms) {
        positionManager = new PositionManager();
        positionManager.configureWithRooms(rooms);
        positionManager.startMonitoring(context, new PositionManager.EventListener() {
            @Override
            public void onUserFoundInRoom(Room room) {
                didFindUserToBeInRoom(room);
            }
        });

        Log.v(Configuration.Log, "Did configure PositionManager");
    }

    /**
     * Called when the user is found to be in a room.
     * @param room Room the user entered.
     */
    private void didFindUserToBeInRoom(Room room) {
        EnteredRoomObservation observation = new EnteredRoomObservation(System.currentTimeMillis(), room);
        enteredRoomObservations.add(observation);
        removeOldEnteredRoomObservations();

        calculateProbabilities();
        logCurrentOutcomes();
    }

    /**
     * Removes all entered room observations that are too old.
     * The time is specified by the enteredRoomObservationsTimeToLive property.
     */
    private void removeOldEnteredRoomObservations() {
        long maxAgeTimestamp = System.currentTimeMillis() - enteredRoomObservationsTimeToLive * 1000;
        ArrayList<EnteredRoomObservation> result = new ArrayList<>();
        for (EnteredRoomObservation enteredRoomObservation : enteredRoomObservations) {
            if (enteredRoomObservation.timestamp > maxAgeTimestamp) {
                result.add(enteredRoomObservation);
            }
        }

        enteredRoomObservations = result;
    }

    /**
     * Calculates the probabilities based on all
     * observations in enteredRoomObservations.
     */
    private void calculateProbabilities() {
        // Calculate occurrences
        int totalObservationsCount = enteredRoomObservations.size();
        HashMap<String, Integer> roomObservationCountMap = new HashMap<>();
        for (EnteredRoomObservation enteredRoomObservation : enteredRoomObservations) {
            String roomIdentifier = enteredRoomObservation.room.identifier;
            if (roomObservationCountMap.containsKey(roomIdentifier)) {
                // We have seen the room identifier before.
                int newCount = roomObservationCountMap.get(roomIdentifier) + 1;
                roomObservationCountMap.put(roomIdentifier, newCount);
            } else {
                // It is the first time we see the room identifier.
                roomObservationCountMap.put(roomIdentifier, 1);
            }
        }

        // Map to outcomes
        ArrayList<ContextOutcome> outcomes = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : roomObservationCountMap.entrySet()) {
            String roomIdentifier = entry.getKey();
            Integer observationsCount = entry.getValue();
            ArrayList<GestureConfiguration> gestureConfigurations = DummyData.gestureConfigurationsForRoomWithIdentifier(roomIdentifier);
            // Add a probability for each action in the room.
            for (GestureConfiguration gestureConfiguration : gestureConfigurations) {
                double probability = (double)observationsCount / (double)totalObservationsCount;
                outcomes.add(new ContextOutcome(gestureConfiguration.actionId, probability));
            }
        }

        this.outcomes = ContextOutcome.normalizeOutcomes(outcomes);
    }

    /**
     * Logs the current outcomes. For debugging purposes.
     */
    private void logCurrentOutcomes() {
        for (ContextOutcome outcome : outcomes) {
            Log.v(Configuration.Log, outcome.id + ": " + outcome.probability);
        }
    }

    @Override
    public double getWeight() {
        return 0.4;
    }

    @Override
    public void getContext(ContextProviderListener listener) {
        calculateProbabilities();
        logCurrentOutcomes();
        listener.onContextReady(outcomes);
    }

    @Override
    public void cancel() { }
}
