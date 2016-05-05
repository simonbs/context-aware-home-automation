package aau.carmakit.ContextualInformationProviders;
import android.content.Context;

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
        Logger.verbose("Did configure PositionManager");
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
        Logger.verbose("Did receive evidence that user is in room " + room.name);
        EnteredRoomObservation observation = new EnteredRoomObservation(System.currentTimeMillis(), room);
        enteredRoomObservations.add(observation);
        removeOldEnteredRoomObservations();
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
     * Counts the number of observations for each room.
     * @return Map containing observation count. Keys are room IDs and values
     * are the number of times the user is observed in the room.
     */
    private HashMap<String, Integer> countObservations() {
        // Compute occurrences of the rooms being in each room.
        // Keys are the room identifiers and values are the number of
        // times we have observed the user in the room.
        HashMap<String, Integer> roomObservationCountMap = new HashMap<>();
        if (virtualPosition.isPresent()) {
            // We are using a virtual position, so the only room we have observed is
            // the virtual position.
            roomObservationCountMap.put(virtualPosition.value.identifier, 1);
        } else {
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
        }

        return roomObservationCountMap;
    }

    @Override
    public void getContext(ContextualInformationListener listener, BayesNet net) {
        this.listener = new Optional<>(listener);

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
            roomNode.addOutcome(uniqueRoomId);
        }

        // Set probability of states in room node.
        double[] rawRoomProbabiltiies = new double[uniqueRoomIds.size()];
        int roomCount = uniqueRoomIds.size();
        for (int r = 0; r < roomCount; r++) {
            rawRoomProbabiltiies[r] = 1.0 / (double)roomCount;
        }
        roomNode.setProbabilities(rawRoomProbabiltiies);

        BayesNode roomActionNode = net.createNode("room_action");
        // Add states to room_action node.
        for (String uniqueActionId : uniqueActionIds) {
            roomActionNode.addOutcome(uniqueActionId);
        }

        // Set parents of room_action node.
        roomActionNode.setParents(Arrays.asList(roomNode));

        // Set probabilities for room_action node.
        double[] rawRoomActionProabilities = new double[uniqueRoomIds.size() * uniqueActionIds.size()];
        for (int r = 0; r < uniqueRoomIds.size(); r++) {
            String roomId = uniqueRoomIds.get(r);
            ArrayList<String> actionIdsForRoom = roomActionsMap.get(roomId);
            for (int a = 0; a < uniqueActionIds.size(); a++) {
                String actionId = uniqueActionIds.get(a);
                int probabilityIdx = r * uniqueActionIds.size() + a;
                if (actionIdsForRoom.contains(actionId)) {
                    // The action can be triggered in the room.
                    rawRoomActionProabilities[probabilityIdx] = 1.0 / (double)actionIdsForRoom.size();
                } else {
                    // The action cannot be triggered in the room.
                    rawRoomActionProabilities[probabilityIdx] = 0.0;
                }
            }
        }
        roomActionNode.setProbabilities(rawRoomActionProabilities);

        // Add soft evidence to the room node.
        int totalObservationsCount = enteredRoomObservations.size();
        HashMap<String, Integer> countedObservations = countObservations();
        double[] softEvidence = new double[uniqueRoomIds.size()];
        for (int r = 0; r < uniqueRoomIds.size(); r++) {
            String roomId = uniqueRoomIds.get(r);
            softEvidence[r] = (double)countedObservations.get(roomId) / (double)totalObservationsCount;
        }

        ProvidedContextualInformation contextualInformation = new ProvidedContextualInformation(
                roomActionNode,
                roomNode,
                softEvidence);
        listener.onContextualInformationReady(contextualInformation);
    }

    @Override
    public void cancel() {

    }
}