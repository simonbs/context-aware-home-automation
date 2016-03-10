package simonbs.whereami;

import android.os.Parcel;
import android.os.Parcelable;
import com.estimote.sdk.Region;

import java.util.UUID;

/**
 * Created by simonbs on 09/03/2016.
 */
public class Room implements Parcelable {
    /**
     * Identifier of the room.
     */
    final String identifier;

    /**
     * Name of the room.
     */
    final String name;

    /**
     * Beacons inside the room.
     * The beacons are used for determing the users position.
     */
    final simonbs.whereami.Beacon[] beacons;

    /**
     * Creates a new room.
     * @param identifier Identifier for the room. Should be unique across all the users rooms.
     * @param name Name of the room.
     * @param beacons Beacons inside the room.
     */
    Room(String identifier, String name, Beacon[] beacons) {
        this.identifier = identifier;
        this.name = name;
        this.beacons = beacons;
    }

    Room(Parcel in) {
        identifier = in.readString();
        name = in.readString();
        beacons = (simonbs.whereami.Beacon[])in.readArray(simonbs.whereami.Beacon.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(identifier);
        dest.writeString(name);
        dest.writeArray(beacons);
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Room createFromParcel(Parcel in) {
            return new Room(in);
        }

        public Room[] newArray(int size) {
            return new Room[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }
}
