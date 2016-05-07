package aau.carmakit.ContextualInformationProviders;
import android.content.Context;
import android.util.Log;

import com.android.internal.util.Predicate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import aau.carmakit.ContextEngine.ContextOutcome;
import aau.carmakit.ContextEngine.ContextualInformationListener;
import aau.carmakit.ContextEngine.ContextualInformationProvider;
import aau.carmakit.ContextEngine.ProvidedContextualInformation;
import aau.carmakit.ContextEngine.Recommender.jayes.BayesNet;
import aau.carmakit.ContextEngine.Recommender.jayes.BayesNode;
import aau.carmakit.Database.DatabaseHelper;
import aau.carmakit.GestureConfiguration;
import aau.carmakit.Utilities.Consumer;
import aau.carmakit.Utilities.Func;
import aau.carmakit.Utilities.Funcable;
import aau.carmakit.Utilities.Logger;
import aau.carmakit.Utilities.Optional;
import aau.carmakit.Utilities.Room;

/**
 * Provides contextual information related to the position of the user.
 */
public class PositionContextualInformationProvider implements ContextualInformationProvider {
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
     * Holds a reference to the listener to notify when the contextual information is ready.
     */
    private Optional<ContextualInformationListener> listener = new Optional<>();

    /**
     * Context the provider is run in.
     */
    private Context context;

    /**
     * The virtual position being used.
     */
    private Optional<Room> virtualPosition = new Optional<>();

    /**
     * Context to monitor the rooms in.
     * @param context Context to monitor the rooms and read from the database in.
     */
    public PositionContextualInformationProvider(Context context) {
        this.context = context;
    }

    /**
     * Starts monitoring the users position in the provided rooms.
     * @param rooms Rooms to listen for the users position in.
     */
    public void monitorRooms(ArrayList<Room> rooms) {
        positionManager = new PositionManager();
        positionManager.configureWithRooms(rooms);
        startMonitoringPosition();
        Logger.verbose("[PositionContextualInformationProvider] Did start monitoring rooms.");
    }

    /**
     * Starts monitoring the users position.
     */
    private void startMonitoringPosition() {
        positionManager.startMonitoring(context, new PositionManager.EventListener() {
            @Override
            public void onUserFoundInRoom(Room room) {
                didFindUserToBeInRoom(room);
            }
        });
    }

    /**
     * Sets the users position virtually. The provider will always
     * provide the specified room until the virtual positioning is
     * stopped.
     * @param room Virtual position of the user.
     */
    public void setVirtualPosition(Room room) {
        virtualPosition = new Optional<>(room);
    }

    /**
     * Stops using a virtual positioning, thus using "real"
     * positioning again.
     */
    public void stopVirtualPosition() {
        if (isUsingVirtualPosition()) {
            virtualPosition = new Optional<>();
        }
    }

    /**
     * Checks whether or not we are using a virtual position.
     * @return Whether or not we are using a virtual position.
     */
    public boolean isUsingVirtualPosition() {
        return virtualPosition.isPresent();
    }

    /**
     * Retrieves the virtual position.
     * @return Virtual position.
     */
    public Optional<Room> getVirtualPosition() {
        return virtualPosition;
    }

    /**
     * Called when the user is found to be in a room.
     * @param room Room the user entered.
     */
    private void didFindUserToBeInRoom(Room room) {
        Logger.verbose("[PositionContextualInformationProvider] Did receive evidence that the user is in room " + room.name + " (" + room.identifier + ").");
        EnteredRoomObservation observation = new EnteredRoomObservation(System.currentTimeMillis(), room);
        enteredRoomObservations.add(observation);
        removeOldEnteredRoomObservations();

        // For debugging purposes only.
        HashMap<String, Integer> roomObservationCountMap = new HashMap<>();
        if (virtualPosition.isPresent()) {
            // We are using a virtual position, so the only room we have observed is
            // the virtual position.
            roomObservationCountMap.put(virtualPosition.value.identifier, 1);
        } else {
            for (EnteredRoomObservation enteredRoomObservation : enteredRoomObservations) {
                final String roomId = enteredRoomObservation.room.identifier;
                Funcable<EnteredRoomObservation> observationWithIdentifier = new Funcable<>(enteredRoomObservations).filter(new Predicate<EnteredRoomObservation>() {
                    @Override
                    public boolean apply(EnteredRoomObservation enteredRoomObservation) {
                        return enteredRoomObservation.room.identifier.equals(roomId);
                    }
                });

                int observationCount = observationWithIdentifier.getValue().size();
                roomObservationCountMap.put(roomId, observationCount);
            }
        }

        // Count total observations.
        int totalObservationsCount = 0;
        for (Integer roomObservationCount : roomObservationCountMap.values()) {
            totalObservationsCount += roomObservationCount;
        }

        String probabiltiesLogMsg = "";
        for (Map.Entry<String, Integer> entry : roomObservationCountMap.entrySet()) {
            probabiltiesLogMsg += entry.getKey() + " " + entry.getValue() + " time(s) (" + (double)entry.getValue() / (double)totalObservationsCount + ") ";
        }

        Logger.verbose("[PositionContextualInformationProvider] User was observed in rooms " + probabiltiesLogMsg);
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
                Logger.verbose("[PositionContextualInformationProvider] An observation in room " + enteredRoomObservation.room.name + " (" + enteredRoomObservation.room.identifier + ") was removed. It was too old.");
                result.add(enteredRoomObservation);
            }
        }

        enteredRoomObservations = result;
    }

    @Override
    public void getContext(ContextualInformationListener listener, BayesNet net) {
        this.listener = new Optional<>(listener);

        Logger.verbose("[PositionContextualInformationProvider] Will get context.");

        // Find all unique configured rooms and actions.
        ArrayList<GestureConfiguration> gestureConfigurations = DatabaseHelper.getInstance(context).getAllGestureConfiguration();
        ArrayList<String> uniqueRoomIds = new ArrayList<>();
        ArrayList<String> uniqueActionIds = new ArrayList<>();
        for (GestureConfiguration gestureConfiguration : gestureConfigurations) {
            if (!uniqueRoomIds.contains(gestureConfiguration.roomId)) {
                uniqueRoomIds.add(gestureConfiguration.roomId);
            }

            if (!uniqueActionIds.contains(gestureConfiguration.actionId)) {
                uniqueActionIds.add(gestureConfiguration.actionId);
            }
        }

        // Count observed rooms.
        HashMap<String, Integer> roomObservationCountMap = new HashMap<>();
        if (virtualPosition.isPresent()) {
            // We are using a virtual position, so the only room we have observed is
            // the virtual position.
            roomObservationCountMap.put(virtualPosition.value.identifier, 1);
        } else {
            for (final String uniqueRoomId : uniqueRoomIds) {
                Funcable<EnteredRoomObservation> observationWithIdentifier = new Funcable<>(enteredRoomObservations).filter(new Predicate<EnteredRoomObservation>() {
                    @Override
                    public boolean apply(EnteredRoomObservation enteredRoomObservation) {
                        return enteredRoomObservation.room.identifier.equals(uniqueRoomId);
                    }
                });

                int observationCount = observationWithIdentifier.getValue().size();
                roomObservationCountMap.put(uniqueRoomId, observationCount);
            }
        }

        // Count total observations.
        int totalObservationsCount = 0;
        for (Integer roomObservationCount : roomObservationCountMap.values()) {
            totalObservationsCount += roomObservationCount;
        }

        // Sort room IDs to ensure uniformity.
        Collections.sort(uniqueRoomIds, new Comparator<String>() {
            @Override
            public int compare(String lhs, String rhs) {
                return lhs.compareToIgnoreCase(rhs);
            }
        });

        // Sort action IDs to ensure uniformity.
        Collections.sort(uniqueActionIds, new Comparator<String>() {
            @Override
            public int compare(String lhs, String rhs) {
                return lhs.compareToIgnoreCase(rhs);
            }
        });

        // Find all actions that can be triggered in each room.
        HashMap<String, ArrayList<String>> roomActionsMap = new HashMap<>();
        for (final String uniqueRoomId : uniqueRoomIds) {
            Funcable<GestureConfiguration> configs = new Funcable(gestureConfigurations).filter(new Predicate<GestureConfiguration>() {
                @Override
                public boolean apply(GestureConfiguration gestureConfiguration) {
                    return gestureConfiguration.roomId.equals(uniqueRoomId);
                }
            });

            Funcable<String> actionIds = configs.flatMap(new Consumer<GestureConfiguration, Optional<String>>() {
                @Override
                public Optional<String> consume(GestureConfiguration gestureConfiguration) {
                    return new Optional<>(gestureConfiguration.actionId);
                }
            });

            roomActionsMap.put(uniqueRoomId, actionIds.getValue());
        }

        // Add states to room node.
        BayesNode roomNode = net.createNode("room");
        for (String uniqueRoomId : uniqueRoomIds) {
            Logger.verbose("[PositionContextualInformationProvider] Add state " + uniqueRoomId + " to room node.");
            roomNode.addOutcome(uniqueRoomId);
        }

        // Set probability of states in room node.
        double[] rawRoomProbabilities = new double[uniqueRoomIds.size()];
        int roomCount = uniqueRoomIds.size();
        for (int r = 0; r < roomCount; r++) {
            Logger.verbose("[PositionContextualInformationProvider] P(room=" + uniqueRoomIds.get(r) + ") = " + (1.0 / (double)roomCount) + ".");
            rawRoomProbabilities[r] = 1.0 / (double)roomCount;
        }
        roomNode.setProbabilities(rawRoomProbabilities);

        BayesNode roomActionNode = net.createNode("room_action");
        // Add states to room_action node.
        for (String uniqueActionId : uniqueActionIds) {
            roomActionNode.addOutcome(uniqueActionId);
        }

        // Set parents of room_action node.
        roomActionNode.setParents(Arrays.asList(roomNode));

        // Set probabilities for room_action node.
        double[] rawRoomActionProbabilities = new double[uniqueRoomIds.size() * uniqueActionIds.size()];
        for (int r = 0; r < uniqueRoomIds.size(); r++) {
            String roomId = uniqueRoomIds.get(r);
            ArrayList<String> actionIdsForRoom = roomActionsMap.get(roomId);
            for (int a = 0; a < uniqueActionIds.size(); a++) {
                String actionId = uniqueActionIds.get(a);
                int probabilityIdx = r * uniqueActionIds.size() + a;
                if (actionIdsForRoom.size() == 0) {
                    // No actions can be triggered in this room. Use same probability.
                    Logger.verbose("[PositionContextualInformationProvider] P(room_action=" + actionId + "|room=" + roomId + ") = " + (1.0 / (double)uniqueActionIds.size()) + ", same as others because no action can be triggered in the room.");
                    rawRoomActionProbabilities[probabilityIdx] = 1.0 / (double)uniqueActionIds.size();
                } else if (actionIdsForRoom.contains(actionId)) {
                    // The action can be triggered in the room.
                    Logger.verbose("[PositionContextualInformationProvider] P(room_action=" + actionId + "|room=" + roomId + ") = " + (1.0 / (double)actionIdsForRoom.size()) + ".");
                    rawRoomActionProbabilities[probabilityIdx] = 1.0 / (double)actionIdsForRoom.size();
                } else {
                    // The action cannot be triggered in the room.
                    Logger.verbose("[PositionContextualInformationProvider] P(room_action=" + actionId + "|room=" + roomId + ") = 0 because the action cannot be triggered in the room.");
                    rawRoomActionProbabilities[probabilityIdx] = 0.0;
                }
            }
        }
        roomActionNode.setProbabilities(rawRoomActionProbabilities);

        // Add soft evidence to the room node.
        double[] softEvidence = new double[uniqueRoomIds.size()];
        for (int r = 0; r < uniqueRoomIds.size(); r++) {
            String roomId = uniqueRoomIds.get(r);
            int observationCount = roomObservationCountMap.get(roomId);
            if (totalObservationsCount == 0) {
                // We have not observed any rooms. Evidence should be equal.
                Logger.verbose("[PositionContextualInformationProvider] Propose evidence " + (1.0 / (double) uniqueRoomIds.size()) + " to state " + roomId + " of room node. This is the same as all others because we have not observed any rooms.");
                softEvidence[r] = 1.0 / (double) uniqueRoomIds.size();
            } else if (observationCount == 0) {
                // We have not observed the user in this room, but we have observed him in other rooms.
                // There's no probability that he his here.
                Logger.verbose("[PositionContextualInformationProvider] Propose evidence 0 to state " + roomId + " of room node because we have not observed the user in this room.");
                softEvidence[r] = 0.0;
            } else {
                // We have observed the user in rooms.
                Logger.verbose("[PositionContextualInformationProvider] Propose evidence " + ((double)observationCount / (double)totalObservationsCount) + " to state " + roomId + " of room node.");
                softEvidence[r] = (double) observationCount / (double) totalObservationsCount;
            }
        }

        Logger.verbose("[PositionContextualInformationProvider] Did get context.");

        ProvidedContextualInformation contextualInformation = new ProvidedContextualInformation(
                roomActionNode,
                roomNode,
                softEvidence);
        if (this.listener.isPresent()) {
            this.listener.value.onContextualInformationReady(contextualInformation);
        }
    }

    @Override
    public void cancel() {

    }
}
