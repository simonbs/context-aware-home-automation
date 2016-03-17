package aau.carma;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.estimote.sdk.eddystone.Eddystone;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Monitors a set of rooms.
 */
public class RoomsManager {
    /**
     * Interface to conform to in order to receive events about changes
     * to the users position.
     */
    public interface EventListener {
        /**
         * Called when the manager finds the user to be in a room.
         * This may be called multiple times without the user leaving
         * the room.
         * @param room Room the user is in.
         */
        void onUserFoundInRoom(Room room);
    }

    /**
     * Rooms to monitor.
     */
    private ArrayList<Room> rooms;

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
     * Initializes a rooms manager.
     */
    public RoomsManager() { }

    /**
     * Configure the manager with a set of rooms to monitor..
     * @param rooms Set of rooms.
     */
    public void configureWithRooms(ArrayList<Room> rooms) {
        this.rooms = rooms;
    }

    /**
     * Starts monitoring the configured rooms.
     * @param context Context in which to perform the monitoring. Typically the application context.
     */
    public void startMonitoring(Context context, EventListener listener) {
        Log.v(Configuration.Log, "RoomsManager did start monitoring");

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
    public void stopMonitoring() {
        Log.v(Configuration.Log, "RoomsManager did stop monitoring");

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
    public ArrayList<Room> getRooms() {
        return rooms;
    }

    /**
     * Finds the room for a beacon with the specified namespace and instance.
     * @param namespace Namespace of the beacon.
     * @param instance Instanceof the beacon.
     * @return Room with the specified details, null if not found.
     */
    public Room getRoom(String namespace, String instance) {
        for (Room room : rooms) {
            for (aau.carma.Beacon beacon : room.beacons) {
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
     * Called when we discover a set of Eddystone beacons.
     * Note that we can discover a beacon several times.
     * Determines the anchoring beacon.
     * @param beacons Discovered beacons.
     */
    private void didDiscoverEddystoneBeacons(List<Eddystone> beacons) {
        // Check if we found beacons at all.
        if (beacons.isEmpty()) { return; }

        // Don'd do anything if no one is interested in the data.
        if (listener == null) { return; }

        // Find the eddystone with the highest RSSI.
        Collections.sort(beacons, new Comparator<Eddystone>() {
            @Override
            public int compare(Eddystone lhs, Eddystone rhs) {
                return lhs.rssi > rhs.rssi ? -1 : (lhs.rssi < rhs.rssi) ? 1 : 0;
            }
        });

        // Find the room
        Eddystone eddystone = beacons.get(0);
        Room room = getRoom(eddystone.namespace, eddystone.instance);

        // Notify the listener
        listener.onUserFoundInRoom(room);
    }
}
