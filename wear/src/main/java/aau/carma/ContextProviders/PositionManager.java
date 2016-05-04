package aau.carma.ContextProviders;

import android.content.Context;

import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.eddystone.Eddystone;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import aau.carmakit.Utilities.Logger;
import aau.carmakit.Utilities.Optional;
import aau.carmakit.Utilities.Room;

/**
 * Monitors a set of rooms.
 */
public class PositionManager {
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
     * Context we started monitoring in.
     * Typically the application context.
     */
    private Context context;

    /**
     * Object listening for changes to the users position.
     */
    private EventListener listener;

    /**
     * Initializes a rooms manager.
     */
    public PositionManager() { }

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
        Logger.verbose("PositionManager did start monitoring");
        this.context = context;
        this.listener = listener;
        startBeaconManager();
    }

    /**
     * Starts the beacon manager and thereby starts
     * scanning for nearby beacons.
     */
    private void startBeaconManager() {
        Logger.verbose("PositionManager did start BeaconManager");

        final PositionManager positionManager = this;
        beaconManager = new BeaconManager(context);
        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                // Monitor all configured regions
                positionManager.scanId = beaconManager.startEddystoneScanning();
                beaconManager.setEddystoneListener(new BeaconManager.EddystoneListener() {
                    @Override
                    public void onEddystonesFound(List<Eddystone> list) {
                        positionManager.didDiscoverEddystoneBeacons(list);
                    }
                });
            }
        });
    }

    /**
     * Stops monitoring for change in rooms.
     */
    public void stopMonitoring() {
        Logger.verbose("PositionManager did stop monitoring");

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
     * @param instance Instance of the beacon.
     * @return Room with the specified details, null if not found.
     */
    public Optional<Room> getRoom(String namespace, String instance) {
        for (Room room : rooms) {
            for (aau.carmakit.Utilities.Beacon beacon : room.beacons) {
                Boolean isSameNamespace = beacon.namespace.toLowerCase().equals(namespace.toLowerCase());
                Boolean isSameInstance = beacon.instance.toLowerCase().equals(instance.toLowerCase());
                if (isSameNamespace && isSameInstance) {
                    return new Optional(room);
                }
            }
        }

        return new Optional<>();
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
        Optional<Room> room = getRoom(eddystone.namespace, eddystone.instance);

        // Notify the listener
        if (room.isPresent()) {
            listener.onUserFoundInRoom(room.value);
        }
    }
}
