package aau.carmakit.ContextualInformationProviders;

import android.content.Context;

import com.android.internal.util.Predicate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import aau.carmakit.ContextEngine.ContextOutcome;
import aau.carmakit.Database.DatabaseHelper;
import aau.carmakit.GestureConfiguration;
import aau.carmakit.Utilities.Funcable;
import aau.carmakit.Utilities.Logger;
import aau.carmakit.Utilities.Optional;
import aau.carmakit.Utilities.Room;

/**
 * Provides the position context.
 */
//public class PositionContextProvider {
//    /**
//     * Represents an observation of the user entering a room at a certain time.
//     */
//    class EnteredRoomObservation {
//        /**
//         * Timestamp at which the user entered the room.
//         */
//        final long timestamp;
//
//        /**
//         * The room the user entered.
//         */
//        final Room room;
//
//        /**
//         * Initializes an observation.
//         * @param timestamp Timestamp at which the user entered the room.
//         * @param room The room the user entered.
//         */
//        EnteredRoomObservation(long timestamp, Room room) {
//            this.timestamp = timestamp;
//            this.room = room;
//        }
//    }
//
//    /**
//     * Listens for changes to the users position.
//     */
//    private PositionManager positionManager;
//
//    /**
//     * All room observations registered within the previous amount of seconds
//     * specified by the enteredRoomObservartionsTimeToLive property.
//     */
//    private ArrayList<EnteredRoomObservation> enteredRoomObservations = new ArrayList();
//
//    /**
//     * Number of seconds to consider each entered room observation.
//     */
//    private long enteredRoomObservationsTimeToLive = 30;
//
//    /**
//     * Current set of outcomes.
//     */
//    private ArrayList<ContextOutcome> outcomes;
//
//    /**
//     * Context the provider is run in.
//     */
//    private Context context;
//
//    /**
//     * The virtual position being used.
//     */
//    private Optional<Room> virtualPosition = new Optional<>();
//
//    /**
//     * Configures the provider to listen for the users position in the given rooms.
//     * @param context Context to monitor the rooms in.
//     * @param rooms Rooms to listen for the users position in.
//     */
//    public void configure(Context context, ArrayList<Room> rooms) {
//        this.context = context;
//        positionManager = new PositionManager();
//        positionManager.configureWithRooms(rooms);
//        startMonitoringPosition();
//        Logger.verbose("Did configure PositionManager");
//    }
//
//    /**
//     * Starts monitoring the users position.
//     */
//    private void startMonitoringPosition() {
//        positionManager.startMonitoring(context, new PositionManager.EventListener() {
//            @Override
//            public void onUserFoundInRoom(Room room) {
//                didFindUserToBeInRoom(room);
//            }
//        });
//    }
//
//    /**
//     * Sets the users position virtually. The provider will always
//     * provide the specified room until the virtual positioning is
//     * stopped.
//     * @param room Virtual position of the user.
//     */
//    public void setVirtualPosition(Room room) {
//        virtualPosition = new Optional<>(room);
//    }
//
//    /**
//     * Stops using a virtual positioning, thus using "real"
//     * positioning again.
//     */
//    public void stopVirtualPosition() {
//        if (isUsingVirtualPosition()) {
//            virtualPosition = new Optional<>();
//        }
//    }
//
//    /**
//     * Checks whether or not we are using a virtual position.
//     * @return Whether or not we are using a virtual position.
//     */
//    public boolean isUsingVirtualPosition() {
//        return virtualPosition.isPresent();
//    }
//
//    /**
//     * Retrieves the virtual position.
//     * @return Virtual position.
//     */
//    public Optional<Room> getVirtualPosition() {
//        return virtualPosition;
//    }
//
//    /**
//     * Called when the user is found to be in a room.
//     * @param room Room the user entered.
//     */
//    private void didFindUserToBeInRoom(Room room) {
//        Logger.verbose("Did receive evidence that user is in room " + room.name);
//        EnteredRoomObservation observation = new EnteredRoomObservation(System.currentTimeMillis(), room);
//        enteredRoomObservations.add(observation);
//        removeOldEnteredRoomObservations();
//
//        calculateProbabilities();
//        logCurrentOutcomes();
//    }
//
//    /**
//     * Removes all entered room observations that are too old.
//     * The time is specified by the enteredRoomObservationsTimeToLive property.
//     */
//    private void removeOldEnteredRoomObservations() {
//        long maxAgeTimestamp = System.currentTimeMillis() - enteredRoomObservationsTimeToLive * 1000;
//        ArrayList<EnteredRoomObservation> result = new ArrayList<>();
//        for (EnteredRoomObservation enteredRoomObservation : enteredRoomObservations) {
//            if (enteredRoomObservation.timestamp > maxAgeTimestamp) {
//                result.add(enteredRoomObservation);
//            }
//        }
//
//        enteredRoomObservations = result;
//    }
//
//    /**
//     * Calculates the probabilities based on all
//     * observations in enteredRoomObservations.
//     */
//    private void calculateProbabilities() {
//        // Calculate occurrences
//        int totalObservationsCount = enteredRoomObservations.size();
//        HashMap<String, Integer> roomObservationCountMap = new HashMap<>();
//        if (virtualPosition.isPresent()) {
//            // We are using a virtual position, so the only room we have observed is
//            // the virtual position.
//            roomObservationCountMap.put(virtualPosition.value.identifier, 1);
//        } else {
//            for (EnteredRoomObservation enteredRoomObservation : enteredRoomObservations) {
//                String roomIdentifier = enteredRoomObservation.room.identifier;
//                if (roomObservationCountMap.containsKey(roomIdentifier)) {
//                    // We have seen the room identifier before.
//                    int newCount = roomObservationCountMap.get(roomIdentifier) + 1;
//                    roomObservationCountMap.put(roomIdentifier, newCount);
//                } else {
//                    // It is the first time we see the room identifier.
//                    roomObservationCountMap.put(roomIdentifier, 1);
//                }
//            }
//        }
//
//        // Map to outcomes
//        ArrayList<GestureConfiguration> gestureConfigurations = DatabaseHelper.getInstance(context).getAllGestureConfiguration();
//        ArrayList<ContextOutcome> outcomes = new ArrayList<>();
//        for (Map.Entry<String, Integer> entry : roomObservationCountMap.entrySet()) {
//            final String roomIdentifier = entry.getKey();
//            Integer observationsCount = entry.getValue();
//
//            ArrayList<GestureConfiguration> gestureConfigurationsForRoom = new Funcable(gestureConfigurations).filter(new Predicate<GestureConfiguration>() {
//                @Override
//                public boolean apply(GestureConfiguration gestureConfiguration) {
//                    return gestureConfiguration.roomId.equals(roomIdentifier);
//                }
//            }).getValue();
//
//            // Add a probability for each action in the room.
//            for (GestureConfiguration gestureConfiguration : gestureConfigurationsForRoom) {
//                double probability = (double)observationsCount / (double)totalObservationsCount;
//                Logger.verbose("Position probability for " + gestureConfiguration.id + ": " + probability);
//                outcomes.add(new ContextOutcome(gestureConfiguration.id, probability));
//            }
//        }
//
//        this.outcomes = ContextOutcome.normalizeOutcomes(outcomes);
//    }
//
//    /**
//     * Logs the current outcomes. For debugging purposes.
//     */
//    private void logCurrentOutcomes() {
//        if (outcomes != null) {
//            for (ContextOutcome outcome : outcomes) {
//                Logger.verbose(outcome.id + ": " + outcome.probability);
//            }
//        }
//    }
//
//    @Override
//    public double getWeight() {
//        return 0.4;
//    }
//
//    @Override
//    public void getContext(ContextProviderListener listener) {
//        logCurrentOutcomes();
//        listener.onContextReady(outcomes);
//    }
//
//    @Override
//    public void cancel() { }
//}
