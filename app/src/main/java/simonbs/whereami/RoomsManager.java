package simonbs.whereami;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.estimote.sdk.eddystone.Eddystone;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Monitors a set of rooms.
 * Created by simonbs on 09/03/2016.
 */
public class RoomsManager {
    /**
     * Interface to conform to in order to receive events about changes
     * to the users position.
     */
    public interface EventListener {
        /**
         * Called when the user enters a room.
         * @param room Room the user enters.
         */
        void onDidEnterRoom(Room room);

        /**
         * Called when the user leaves the current room.
         * Note: If the user changes to a different room,
         * onDidEnterRoom is called and this is not called.
         * This is conly called when the user leaves the room
         * and we are not able to determine which room he is
         * in in now.
         */
        void onDidLeaveRoom();
    }

    /**
     * Shared instance of the manager.
     */
    private static RoomsManager ourInstance = new RoomsManager();

    /**
     * Shared instance of the manager.
     * @return The shared instance of the manager.
     */
    public static RoomsManager getInstance() {
        return ourInstance;
    }

    /**
     * Rooms to monitor.
     */
    private Room[] rooms;

    /**
     * The room the user is currently in.
     */
    private Room currentRoom;

    /**
     * Manages the beacons, i.e. scanning for new beacons.
     */
    private BeaconManager beaconManager;

    /**
     * Identifier for the current scan of Eddystone beacons.
     */
    private String scanId;

    /**
     * Object listening for changes to the users position.
     */
    private EventListener listener;

    /**
     * The Eddystone beacon that determined the current position of the user,
     * i.e. the one we "snapped" the user to.
     */
    private Eddystone anchoringEddystone;

    /**
     * Initializes a rooms manager.
     */
    private RoomsManager() { }

    /**
     * Configure the manager with a set of rooms to monitor..
     * @param rooms Set of rooms.
     */
    void configureWithRooms(Room[] rooms) {
        this.rooms = rooms;
    }

    /**
     * Starts monitoring the configured rooms.
     * @param context Context in which to perform the monitoring. Typically the application context.
     */
    void startMonitoring(Context context, EventListener listener) {
        this.listener = listener;
        final RoomsManager roomsManager = this;
        beaconManager = new BeaconManager(context);
        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                // Monitor all configured regions
                roomsManager.scanId = beaconManager.startEddystoneScanning();
                beaconManager.setEddystoneListener(new BeaconManager.EddystoneListener() {
                    @Override
                    public void onEddystonesFound(List<Eddystone> list) {
                        roomsManager.didDiscoverEddystoneBeacons(list);
                    }
                });
            }
        });
    }

    /**
     * Stops monitoring for change in rooms.
     */
    void stopMonitoring() {
        beaconManager.setEddystoneListener(null);
        if (scanId != null) {
            beaconManager.stopEddystoneScanning(scanId);
        }

        scanId = null;
        beaconManager = null;
    }

    /**
     * Get all rooms.
     * @return All rooms.
     */
    Room[] getRooms() {
        return rooms;
    }

    /**
     * Finds the room for a beacon with the specified namespace and instance.
     * @param namespace Namespace of the beacon.
     * @param instance Instanceof the beacon.
     * @return Room with the specified details, null if not found.
     */
    Room getRoom(String namespace, String instance) {
        for (Room room : rooms) {
            for (simonbs.whereami.Beacon beacon : room.beacons) {
                Boolean isSameNamespace = beacon.namespace.toLowerCase().equals(namespace.toLowerCase());
                Boolean isSameInstance = beacon.instance.toLowerCase().equals(instance.toLowerCase());
                if (isSameNamespace && isSameInstance) {
                    return room;
                }
            }
        }

        return null;
    }

    /**
     * Gets the current room.
     * @return Current room, null if the user is not in a known room.
     */
    Room getCurrentRoom() {
        return currentRoom;
    }

    /**
     * Called when we discover a set of Eddystone beacons.
     * Note that we can discover a beacon several times.
     * Determines the anchoring beacon.
     * @param beacons Discovered beacons.
     */
    private void didDiscoverEddystoneBeacons(List<Eddystone> beacons) {
        // Find the eddystone with the highest RSSI.
        Collections.sort(beacons, new Comparator<Eddystone>() {
            @Override
            public int compare(Eddystone lhs, Eddystone rhs) {
                return lhs.rssi > rhs.rssi ? -1 : (lhs.rssi < rhs.rssi) ? 1 : 0;
            }
        });

        // Store if we currently have an anchor.
        Boolean hadAnchoredBeacon = anchoringEddystone != null;

        // Check if we found beacons at all.
        if (beacons.isEmpty()) {
            if (hadAnchoredBeacon ){
                // We found no beacons, but we just had one.
                // We must have left the room in some way,
                // i.e. we actually left it or we just lost connection
                // for some reason.
                didLeaveRoom();
            }

            return;
        }

        Eddystone eddystone = beacons.get(0);
        Room room = getRoom(eddystone.namespace, eddystone.instance);

        // Check if the beacon we are currently anchored to has disappeared.
        if (anchoringEddystone != null) {
            Boolean currentlyAnchoredEddistoneExists = beacons.contains(anchoringEddystone);
            if (!currentlyAnchoredEddistoneExists) {
                anchoringEddystone = null;
            }
        }

        // We found an unknown beacon.
        // Don't do anything if we don't have a room for this beacon.
        if (room == null) {
            Log.v(Configuration.Log, "Did not find a room for the Eddystone.");

            if (hadAnchoredBeacon) {
                // We found no rooms for the current set of beacons,
                // but we had an anchoring beacon before, so we must
                // have left the room in some way.
                didLeaveRoom();
            }

            return;
        }

        // If we do not have an anchoring Eddystone,
        // then consider this to be the one.
        if (anchoringEddystone == null) {
            Log.v(Configuration.Log, "Did find first anchoring Eddystone.");
            anchoringEddystone = eddystone;
            didEnterRoom(room);
            return;
        }

        // Check if we're closer to this room than the current room,
        // i.e. the RSSI value is higher than the beacon we're currently
        // anchored to.
        if (eddystone.rssi > anchoringEddystone.rssi) {
            Log.v(Configuration.Log, "Did find better anchoring Eddystone.");
            anchoringEddystone = eddystone;
            // Change the room if we're not already in it.
            if (room.identifier != currentRoom.identifier) {
                didEnterRoom(room);
            }
        }
    }

    /**
     * Called when the user changes his position, i.e. enters a new room.
     * @param room The room the user entered.
     */
    private void didEnterRoom(Room room) {
        // Don't do anything if it is the same room.
        if (currentRoom != null && room.identifier == currentRoom.identifier) { return; }

        Log.v(Configuration.Log, "Did enter room with identifier " + room.identifier);
        currentRoom = room;

        if (listener != null) {
            listener.onDidEnterRoom(room);
        }
    }

    /**
     * Called when the user leaves the current room.
     */
    private void didLeaveRoom() {
        // Don't do anything if we are not in a room
        if (currentRoom == null) { return; }

        Log.v(Configuration.Log, "Did leave room with identifier " + currentRoom.identifier);
        currentRoom = null;

        if (listener != null) {
            listener.onDidLeaveRoom();
        }
    }
}
