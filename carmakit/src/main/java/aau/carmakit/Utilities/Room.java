package aau.carmakit.Utilities;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Represents a room registered in openHAB.
 */
public class Room implements Parcelable {
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
     * The beacons are used for determining the users position.
     */
    public final ArrayList<aau.carmakit.Utilities.Beacon> beacons;

    /**
     * Creates a new room.
     * @param identifier Identifier for the room. Should be unique across all the users rooms.
     * @param name Name of the room.
     * @param beacons Beacons inside the room.
     */
    public Room(String identifier, String name, ArrayList<aau.carmakit.Utilities.Beacon> beacons) {
        this.identifier = identifier;
        this.name = name;
        this.beacons = beacons;
    }

    protected Room(Parcel in) {
        identifier = in.readString();
        name = in.readString();
        beacons = in.readArrayList(Beacon.class.getClassLoader());
    }

    public static final Creator<Room> CREATOR = new Creator<Room>() {
        @Override
        public Room createFromParcel(Parcel in) {
            return new Room(in);
        }

        @Override
        public Room[] newArray(int size) {
            return new Room[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(identifier);
        dest.writeString(name);
        dest.writeList(beacons);
    }
}
