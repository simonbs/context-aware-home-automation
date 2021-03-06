package aau.carma.Library;

import com.android.internal.util.Predicate;

import java.util.ArrayList;

import aau.carma.OpenHABClient.OpenHABClient;
import aau.carma.OpenHABClient.Thing;
import aau.carma.RESTClient.ResultListener;

/**
 * Loads and manages the set of available rooms.
 */
public class RoomsManager {
    /**
     * Objects conforming to the interface are notified
     * when the a room update have been performed.
     */
    public interface RoomsListener {
        /**
         * Called when an update have been performed.
         * @param result Result of the update.
         */
        void onUpdate(Result<ArrayList<Room>> result);
    }

    /**
     * Our instance of the manager.
     */
    private static RoomsManager ourInstance = new RoomsManager();

    /**
     * Object listening to updates.
     */
    private Optional<RoomsListener> listener;

    /**
     * Currently loaded rooms.
     */
    private Optional<ArrayList<Room>> rooms = new Optional<>();

    /**
     * Shared instance of the manager.
     * @return Shared instance of RoomsManager.
     */
    public static RoomsManager getInstance() {
        return ourInstance;
    }

    private RoomsManager() { }

    /**
     * Triggers a reload of the rooms.
     * @param listener Listener invoked when an updated occurs.
     */
    public void reload(RoomsListener listener) {
        this.listener = new Optional<>(listener);
        update();
    }

    /**
     * Gets the currently loaded rooms.
     * @return Currently loaded rooms.
     */
    public Optional<ArrayList<Room>> getRooms() {
        return rooms;
    }

    /**
     * Updates the rooms.
     */
    private void update() {
        OpenHABClient client = new OpenHABClient();
        client.loadThings(new ResultListener<ArrayList<Thing>>() {
            @Override
            public void onResult(Result<ArrayList<Thing>> result) {
                if (result.isSuccess()) {
                    didUpdate(result.value.value);
                } else {
                    didFailUpdate(result.error.value);
                }
            }
        });
    }

    /**
     * An update succeeded.
     * @param things Things retrieved from the update.
     */
    private void didUpdate(ArrayList<Thing> things) {
        // Find things with the room type.
        Funcable<Thing> roomThings = new Funcable(things).filter(new Predicate<Thing>() {
            @Override
            public boolean apply(Thing thing) {
                return thing.thingTypeUid.equals(Thing.Type.Room.getRawValue());
            }
        });

        // Find things with the beacon type.
       final Funcable<Thing> beaconThings = new Funcable(things).filter(new Predicate<Thing>() {
            @Override
            public boolean apply(Thing thing) {
                return thing.thingTypeUid.equals(Thing.Type.Beacon.getRawValue());
            }
        });

        // Map things with the room type to Room objects.
        Funcable<Room> rooms = roomThings.flatMap(new Consumer<Thing, Optional<Room>>() {
            @Override
            public Optional<Room> consume(Thing roomThing) {
                final String roomUid = roomThing.uid;
                // Find beacons in this room.
                Funcable<Thing> beaconThingsInRoom = beaconThings.filter(new Predicate<Thing>() {
                    @Override
                    public boolean apply(Thing beaconThing) {
                        Optional<String> thingRoomUid = beaconThing.configuration.getValue("roomUID");
                        if (thingRoomUid.isPresent()) {
                            return thingRoomUid.value.equals(roomUid);
                        }

                        return false;
                    }
                });

                // Map beacon things to Beacon objects.
                Funcable<Beacon> beacons = beaconThingsInRoom.flatMap(new Consumer<Thing, Optional<Beacon>>() {
                    @Override
                    public Optional<Beacon> consume(Thing beaconThing) {
                        Optional<String> namespace = beaconThing.configuration.getValue("namespace");
                        Optional<String> instance = beaconThing.configuration.getValue("instance");
                        if (namespace.isPresent() && instance.isPresent()) {
                            return new Optional<>(new Beacon(namespace.value, instance.value));
                        }

                        return new Optional<>();
                    }
                });

                // Create the room.
                Room room = new Room(roomUid, roomThing.label, beacons.getValue());
                return new Optional<>(room);
            }
        });

        // Store the rooms for later use.
        this.rooms = new Optional<>(rooms.getValue());

        // Notify the listener.
        if (listener.isPresent()) {
            listener.value.onUpdate(Result.Success(rooms.getValue()));
        }
    }

    /**
     * An update failed.
     * @param error Error the update failed with.
     */
    private void didFailUpdate(Exception error) {
        if (listener.isPresent()) {
            listener.value.onUpdate(Result.Failure(error));
        }
    }

    /**
     * Retrieve a specific room.
     * @param roomId ID of room to retrieve.
     * @return Matching room.
     */
    public Optional<Room> getRoom(final String roomId) {
        if (!this.rooms.isPresent()) {
            return new Optional<>();
        }

        ArrayList<Room> rooms = this.rooms.value;
        Funcable<Room> filteredRooms = new Funcable<>(rooms).filter(new Predicate<Room>() {
            @Override
            public boolean apply(Room room) {
                return room.identifier.equals(roomId);
            }
        });

        if (filteredRooms.getValue().size() > 0) {
            return new Optional<>(filteredRooms.getValue().get(0));
        }

        return new Optional<>();
    }
}