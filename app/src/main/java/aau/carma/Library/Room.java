package aau.carma.Library;

import java.util.ArrayList;

/**
 * Represents a room registered in openHAB.
 */
public class Room {
    /**
     * Identifier of the room.
     */
    public final String identifier;

    /**
     * Name of the room.
     */
    public final String name;

    /**
     * Beacons inside the room.
     * The beacons are used for determing the users position.
     */
    public final ArrayList<aau.carma.Library.Beacon> beacons;

    /**
     * Creates a new room.
     * @param identifier Identifier for the room. Should be unique across all the users rooms.
     * @param name Name of the room.
     * @param beacons Beacons inside the room.
     */
    public Room(String identifier, String name, ArrayList<aau.carma.Library.Beacon> beacons) {
        this.identifier = identifier;
        this.name = name;
        this.beacons = beacons;
    }
}
